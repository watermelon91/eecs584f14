package queryReconstructor;

import java.util.Iterator;

import queryParser.QueryParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class QueryReconstructor {
	/*
	 * Query re-constructor for given query plan.
	 * 
	 */
	
	// NOTES:
	// - if a user references a view, it won't necessarily appear in the execution plan - may be broken down into component parts and reordered.
	
	int tempTableNum;
	PlanReducer pr;
	
	public PlanReducer getPlanReducer()
	{
		return pr;
	}
	
	public QueryReconstructor(PlanReducer p)  
	{
		// TODO later: this should actually take a PlanReducer and operate on a simplified version of the execution plan
		pr = p;
		// this will take the topLevelNode JSONObject of the queryParser
		// it will recursively generate queries for each level
		generateTempQuery(pr.topLevelNode);
		// so, at the bottom level it will generate a query and name the resulting temp table
		// then return to its parent
		// once all children have been processed, the parent can use the child temp tables in its own query
	}
	
	
	// TODO: may want to separate the important logic from the rest
	// we'll need to support subplans, which is why this is coming up
	// fuck, actually, we'll need a separate one that does the same thing except recurses differently. damn
	void generateTempQuery(JSONObject curNode)
	{
		String tmpTableName = pr.getNewTableName(curNode);
		String query = "create table " + tmpTableName + " as ";
		JSONArray outputAttrs = pr.getOutputAttributes(curNode);
		String filter = pr.getFilter(curNode);
		
		JSONArray childrenNodes = pr.getChildren(curNode);// qParser.getChildrenPlanNodes(curNode);
		// iterate through all the children plans of the top level node
		if (pr.getType(curNode).equals("scan")) {
			// add output attributes to select statement
			// check for subplan :'(
			processChildren(childrenNodes);
			query = query + generateQueryForScan(curNode);
		} else if (pr.getType(curNode).equals("join")) {
			processChildren(childrenNodes);
			query = query + generateQueryForJoin(curNode);
		} else if (pr.getType(curNode).equals("aggregate")) { 
			processChildren(childrenNodes);
			query = query + generateQueryForAggregate(curNode);
		}
		
		System.out.println(query);
		curNode.put("query", query);
	}
	
	String generateQueryForScan(JSONObject curNode) {
		String tmpTableName = pr.getNewTableName(curNode);
		// TODO: depending on whether there are subplans or not, maybe should make table or view
		String query = "select ";
		JSONArray outputAttrs = pr.getOutputAttributes(curNode);
		String filter = pr.getFilter(curNode);
		
		JSONArray childrenNodes = pr.getChildren(curNode);// qParser.getChildrenPlanNodes(curNode);
		
		// add output attributes to select statement
		Iterator<String> it = outputAttrs.iterator();
		while (it.hasNext()) {
			String attr = it.next();
			query = query + " " + attr + ",";
		}
		// remove last comma
		query = query.substring(0, query.length() - 1);
		
		query = query + " from " + pr.getInputTable(curNode) + " " + pr.getAliasSet(curNode).get(0);
		if (!filter.equals("")) {
			// has filter, needs where clause.
			query = query + " where " + filter;
		}

		return query;
	}
	
	String generateQueryForJoin(JSONObject curNode) {
		String query = "select ";
		String tmpTableName = pr.getNewTableName(curNode);
		JSONArray outputAttrs = pr.getOutputAttributes(curNode);
		String filter = pr.getFilter(curNode);
		
		JSONArray childrenNodes = pr.getChildren(curNode);// qParser.getChildrenPlanNodes(curNode);
	
		Iterator<JSONObject> citerator = childrenNodes.iterator();	
		String tmpC1 = pr.getNewTableName((JSONObject)childrenNodes.get(0));
		String tmpC2 = pr.getNewTableName((JSONObject)childrenNodes.get(1));
		String joinType = pr.getJoinType(curNode);
		String joinCond = pr.getJoinCondition(curNode);
		
		// join node also needs select statement, but aliases will have to be matched with child nodes and replaced with child table names
		// sooo..
		Iterator<String> it = outputAttrs.iterator();
		while (it.hasNext()) {
			query = query + " " + it.next() + ",";
		}
		// remove last comma
		query = query.substring(0, query.length() - 1);

		// not guaranteed to have a join condition, take cross product if not
		if (joinCond == "") {
			query = query + " from " + tmpC1 + " , " + tmpC2;
		} else {
			query = query + " from " + tmpC1 + " " + joinType + " join " + tmpC2 + " on " + joinCond;
		}
		
		// possible where clause
		if (!filter.equals("")) {
			// has filter, needs where clause.
			query = query + " where " + filter;
		}

		return query;
	}
	
	String generateQueryForAggregate(JSONObject curNode) {
		String tmpTableName = pr.getNewTableName(curNode);
		String query = "select ";
		JSONArray outputAttrs = pr.getOutputAttributes(curNode);
		String filter = pr.getFilter(curNode);
		
		JSONArray childrenNodes = pr.getChildren(curNode);// qParser.getChildrenPlanNodes(curNode);
		String tmpC1 = pr.getNewTableName((JSONObject)childrenNodes.get(0));
		
		Iterator<String> it = outputAttrs.iterator();
		while (it.hasNext()) {
			query = query + " " + it.next() + ",";
		}
		// remove last comma
		query = query.substring(0, query.length() - 1);
		
		query = query + " from " + tmpC1;
		
		JSONArray groupByAttrs = pr.getGroupByAttributes(curNode);
		if (groupByAttrs != null) {
			query = query + " group by ";
			
			Iterator<String> gbit = groupByAttrs.iterator();
			while (gbit.hasNext()) {
				query = query + " " + gbit.next() + ",";
			}
			// remove last comma
			query = query.substring(0, query.length() - 1);
		}
		
		if (!filter.equals("")) {
			query = query + " having " + filter;
		}
		
		return query;
	}
	
	void processChildren(JSONArray childrenNodes) {
		if (childrenNodes != null) {
			Iterator<JSONObject> citerator = childrenNodes.iterator();	
			while(citerator.hasNext())
			{
				JSONObject curChild = citerator.next();	
				generateTempQuery(curChild);				
			}
		}
	}
	
	public static void main(String [ ] args) throws Exception 
	{
		// input file containing the returned query plan
	//	String inputFilePath = "/Users/watermelon/Dropbox/EECS584/Project/code/eecs584f14/TestingData/QueryPlan1_verbose.txt";
		String inputFilePath = "/afs/umich.edu/user/d/a/daneliza/dwtemp/F14/eecs584/eecs584f14/TestingData/QueryPlan5_nested2.txt";//QueryPlan4_nested.txt";//QueryPlan2_aggregation.txt";//QueryPlan3_groupby.txt";//QueryPlan1_verbose.txt";//
		
		// create a new query parser for this query plan
		QueryParser qParser = new QueryParser(inputFilePath);	
		// get the top level node
		PlanReducer p = new PlanReducer(qParser);
		QueryReconstructor qr = new QueryReconstructor(p);
		//System.out.println(qr.pr.getQuery(qr.pr.topLevelNode));
	}

}
