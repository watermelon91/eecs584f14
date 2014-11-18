package queryReconstructor;

import java.util.Iterator;

import queryParser.QueryParser;
import queryParser.QueryProcessingUtilities;

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
		generateTempQuery(pr.topLevelNode, null);
		// so, at the bottom level it will generate a query and name the resulting temp table
		// then return to its parent
		// once all children have been processed, the parent can use the child temp tables in its own query
	}
	
	
	// TODO: may want to separate the important logic from the rest
	// we'll need to support subplans, which is why this is coming up
	// fuck, actually, we'll need a separate one that does the same thing except recurses differently. damn
	String generateTempQuery(JSONObject curNode, JSONObject parentNode)
	{
		String tmpTableName = pr.getNewTableName(curNode);
		String query = "create table " + tmpTableName + " as ";
/*		JSONArray outputAttrs = pr.getOutputAttributes(curNode);
		String filter = pr.getFilter(curNode);
	*/	
		JSONArray childrenNodes = pr.getChildren(curNode);// qParser.getChildrenPlanNodes(curNode);
		// iterate through all the children plans of the top level node
		if (!pr.getSubplanName(curNode).equals("")) {
			System.out.println("found subplan");
			return generateTempQuerySubplan(curNode);
		}
		if (pr.getType(curNode).equals("scan")) {
			// add output attributes to select statement
			// check for subplan :'(
			// processChildren should check to see if any children are subplans
			// if children are subplans, replace name of subplan in filter with query from subplan child
			String[] ar = processChildren(childrenNodes, curNode);
			if (ar != null) {
				pr.setFilter(curNode, QueryProcessingUtilities.replaceSubplanNameWithQuery(ar[1], pr.getFilter(curNode), ar[0]));
			}
			query = query + generateQueryForScan(curNode);
		} else if (pr.getType(curNode).equals("join")) {
			String[] ar = processChildren(childrenNodes, curNode);
			// TODO: can we guarantee there will be only one subplan?
			if (ar != null) {
				pr.setFilter(curNode, QueryProcessingUtilities.replaceSubplanNameWithQuery(ar[1], pr.getFilter(curNode), ar[0]));
			}
			query = query + generateQueryForJoin(curNode);
		} else if (pr.getType(curNode).equals("aggregate")) { 
			String[] ar = processChildren(childrenNodes, curNode);
			if (ar != null) {
				pr.setFilter(curNode, QueryProcessingUtilities.replaceSubplanNameWithQuery(ar[1], pr.getFilter(curNode), ar[0]));
			}
			query = query + generateQueryForAggregate(curNode);
		} /*else if (pr.getType(curNode).equals("subplan")) {
			processChildrenSubplan(childrenNodes, curNode);
			query = query + generateQueryForSubplan(curNode);
		}*/
		
		System.out.println(query);
		curNode.put("query", query);
		return query;
	}
	
	String generateTempQuerySubplan(JSONObject curNode) {
		String tmpTableName = pr.getNewTableName(curNode);
		String query = "";
/*		JSONArray outputAttrs = pr.getOutputAttributes(curNode);
		String filter = pr.getFilter(curNode);
	*/	
		JSONArray childrenNodes = pr.getChildren(curNode);// qParser.getChildrenPlanNodes(curNode);
		// iterate through all the children plans of the top level node
		if (pr.getType(curNode).equals("scan")) {
			// add output attributes to select statement
			// check for subplan :'(
			// processChildren should check to see if any children are subplans
			// if children are subplans, replace name of subplan in filter with query from subplan child
			String[] ar = processChildrenSubplan(childrenNodes, curNode);
			if (ar != null) {
				pr.setFilter(curNode, QueryProcessingUtilities.replaceSubplanNameWithQuery(ar[1], pr.getFilter(curNode), ar[0]));
			}
			query = query + generateQueryForScan(curNode);
		} else if (pr.getType(curNode).equals("join")) {
			processChildrenSubplan(childrenNodes, curNode);
			query = query + generateQueryForJoin(curNode);
		} else if (pr.getType(curNode).equals("aggregate")) { 
			processChildrenSubplan(childrenNodes, curNode);
			query = query + generateQueryForAggregate(curNode);
		} /*else if (pr.getType(curNode).equals("subplan")) {
			query = query + generateQueryForSubplan(curNode);
		}*/
		
		System.out.println(query);
		curNode.put("query", query);
		return query;
		
	}
	
	String[] processChildrenSubplan(JSONArray childrenNodes, JSONObject parent) {
		String[] ar = null;
		if (childrenNodes != null) {
			Iterator<JSONObject> citerator = childrenNodes.iterator();	
			while(citerator.hasNext())
			{
				JSONObject curChild = citerator.next();	
				String tmpTableName = pr.getNewTableName(curChild);
				String tmp = generateTempQuerySubplan(curChild);
				// if child is subplan, return the query generated by the child
				if (pr.getType(curChild) == "subplan") {
					ar = new String[2];
					ar[0] = "(" + tmp + ")"  + " as " + tmpTableName;
					ar[1] = pr.getSubplanName(curChild);
				}
				// set tableName of parent to this new query
				// this could either be inputTable name (for scans), or the
				// newTableName of the child (aggregates and joins)
				if (pr.getType(parent).equals("scan")) {
					pr.setInputTable(parent, "(" + tmp + ")" + " as " + tmpTableName);
				} else {
					pr.setNewTableName(curChild,  "(" + tmp + ")" + " as " + tmpTableName);
				}
			}
		}
		return ar;		
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
		if (pr.getSortKey(curNode) != null) {
			query = query + " order by ";
			JSONArray sortKey = pr.getSortKey(curNode);
			Iterator<String> skit = sortKey.iterator();
			while (skit.hasNext()) {
				query = query + " " + skit.next() + ",";
			}
			// remove last comma
			query = query.substring(0, query.length() - 1);
		}
		
		return query;
	}
	
	/*
	String generateQueryForSubplan(JSONObject curNode) {
		
	}
	*/
	
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

		if (pr.getSortKey(curNode) != null) {
			query = query + " order by ";
			JSONArray sortKey = pr.getSortKey(curNode);
			Iterator<String> skit = sortKey.iterator();
			while (skit.hasNext()) {
				query = query + " " + it.next() + ",";
			}
			// remove last comma
			query = query.substring(0, query.length() - 1);
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

		if (pr.getSortKey(curNode) != null) {
			query = query + " order by ";
			JSONArray sortKey = pr.getSortKey(curNode);
			Iterator<String> skit = sortKey.iterator();
			while (skit.hasNext()) {
				query = query + " " + it.next() + ",";
			}
			// remove last comma
			query = query.substring(0, query.length() - 1);
		}
		
		return query;
	}
	
	String[] processChildren(JSONArray childrenNodes, JSONObject parent) {
		String[] ar = null;
		if (childrenNodes != null) {
		//		System.out.println(childrenNodes.toJSONString());
			Iterator<JSONObject> citerator = childrenNodes.iterator();	
			while(citerator.hasNext())
			{
				JSONObject curChild = citerator.next();	
				String tmp = generateTempQuery(curChild, parent);
				// if child is subplan, return the query generated by the child
				
				if (!pr.getSubplanName(curChild).equals("")) {
					ar = new String[2];
					ar[0] = "(" + tmp + ")";
					ar[1] = pr.getSubplanName(curChild);
				}
				
			}
		}
		return ar;
	}
	
	public static void main(String [ ] args) throws Exception 
	{
		// input file containing the returned query plan
	//	String inputFilePath = "/Users/watermelon/Dropbox/EECS584/Project/code/eecs584f14/TestingData/QueryPlan1_verbose.txt";
		String inputFilePath = "/afs/umich.edu/user/d/a/daneliza/dwtemp/F14/eecs584/eecs584f14/TestingData/QueryPlan6_project37a.txt";//QueryPlan9_BuggyQuery1.txt";//QueryPlan8_BuggyQuery2.txt";//QueryPlan7_subplan.txt";//QueryPlan1_verbose.txt";//QueryPlan4_nested.txt";//QueryPlan2_aggregation.txt";//QueryPlan3_groupby.txt";//QueryPlan5_nested2.txt";//
		
		// create a new query parser for this query plan
		QueryParser qParser = new QueryParser(inputFilePath);	
		// get the top level node
		PlanReducer p = new PlanReducer(qParser);
		QueryReconstructor qr = new QueryReconstructor(p);
	}

}
