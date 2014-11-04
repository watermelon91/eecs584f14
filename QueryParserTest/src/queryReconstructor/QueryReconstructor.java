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
	QueryParser qParser;
	PlanReducer pr;
	
	public QueryReconstructor(QueryParser qp)  
	{
		// TODO later: this should actually take a PlanReducer and operate on a simplified version of the execution plan
		qParser = qp;
		pr = new PlanReducer(qParser);
		// this will take the topLevelNode JSONObject of the queryParser
		// it will recursively generate queries for each level
		generateTempQuery(pr.topLevelNode);
		// so, at the bottom level it will generate a query and name the resulting temp table
		// then return to its parent
		// once all children have been processed, the parent can use the child temp tables in its own query
	}
	
	void generateTempQuery(JSONObject curNode)
	{
		String tmpTableName = pr.getNewTableName(curNode);
		String query = "create table " + tmpTableName + " as select ";
		JSONArray outputAttrs = pr.getOutputAttributes(curNode);
		String filter = pr.getFilter(curNode);
		
		// okay, attributes will always have aliases (if not specified by user, will be added by system)
		
		JSONArray childrenNodes = pr.getChildren(curNode);// qParser.getChildrenPlanNodes(curNode);
		// iterate through all the children plans of the top level node
		if (childrenNodes == null) 
		{
			// process leaf node
			
			// node with no children should either be a scan node or aggregation node
			// right now, only scan nodes are supported
			if (pr.getType(curNode).equals("scan")) {
				// add output attributes to select statement
				Iterator<String> it = outputAttrs.iterator();
				while (it.hasNext()) {
					String attr = it.next();
					query = query + " " + attr + ",";
				}
				// remove last comma
				query = query.substring(0, query.length() - 1);
				
				query = query + " from " + pr.getInputTable(curNode) + " " + pr.getAliasSet(curNode).get(0);
				// get table name and alias, add from clause
				// check for where conditions, add where clause if necessary
				
				if (!filter.equals("")) {
					// has filter, needs where clause.
					query = query + " where " + filter;
				}
				
			} else if (pr.getType(curNode).equals("")) { // TODO: fill in condition here. also implement
				
			}
			
		} else 
		{
			// process children
			Iterator<JSONObject> citerator = childrenNodes.iterator();	
			while(citerator.hasNext())
			{
				// first, recurse on any child nodes
				JSONObject curChild = citerator.next();	
				System.out.println(curChild.toJSONString());
				generateTempQuery(curChild);
				
			}
			// then, once children have been processed, we can use them
			/*
			JSONArray aliasC1 = pr.getAliasSet((JSONObject)childrenNodes.get(0));
			JSONArray aliasC2 = pr.getAliasSet((JSONObject)childrenNodes.get(1));
			*/
			String tmpC1 = pr.getNewTableName((JSONObject)childrenNodes.get(0));
			String tmpC2 = pr.getNewTableName((JSONObject)childrenNodes.get(1));
			String joinCond = pr.getJoinCondition(curNode);
			
			// join node also needs select statement, but aliases will have to be matched with child nodes and replaced with child table names
			// sooo..
			Iterator<String> it = outputAttrs.iterator();
			while (it.hasNext()) {
				/*
				String attr = it.next();
				String[] attrParts = attr.split("\\.");
				String newAttr = "";
				// check alias, replace with name of child
				if (searchJSONArrayForString(aliasC1, attrParts[0])) {
					newAttr = tmpC1 + "." + attrParts[1];
				} else if (searchJSONArrayForString(aliasC2, attrParts[0])) {
					newAttr = tmpC2 + "." + attrParts[1];
				} else {
					// throw exception
				}
				query = query + " " + newAttr + ",";
				*/
				query = query + " " + it.next() + ",";
			}
			// remove last comma
			query = query.substring(0, query.length() - 1);

			
			// from clause will have join statement, use join filters
			
			
			// not guaranteed to have a join condition, take cross product
			if (joinCond == "") {
				query = query + " from " + tmpC1 + " , " + tmpC2;
			} else {
				query = query + " from " + tmpC1 + " inner join " + tmpC2 + " on " + joinCond;
			}
			
			// possible where clause
			if (!filter.equals("")) {
				// has filter, needs where clause.
				// TODO: need to replace any aliases with tmp names
				query = query + " where " + filter;
			}
			
		}
		System.out.println(query);

	}
	
	public static void main(String [ ] args) throws Exception 
	{
	
		// input file containing the returned query plan
	//	String inputFilePath = "/Users/watermelon/Dropbox/EECS584/Project/code/eecs584f14/TestingData/QueryPlan1_verbose.txt";
		String inputFilePath = "/afs/umich.edu/user/d/a/daneliza/dwtemp/F14/eecs584/eecs584f14/TestingData/QueryPlan1_verbose.txt";
		
		// create a new query parser for this query plan
		QueryParser qParser = new QueryParser(inputFilePath);	
		// get the top level node
		System.out.println("\n---------Parsed query---------");
		//PlanReducer pr = new PlanReducer(qParser);
		
		QueryReconstructor qr = new QueryReconstructor(qParser);
		// previous code -- ignore
		/*BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
		String line = "", jsonString = "";
		while ((line = reader.readLine()) != null) {
		    jsonString = jsonString + line;
		}
		System.out.println(jsonString);*/
	}

}
