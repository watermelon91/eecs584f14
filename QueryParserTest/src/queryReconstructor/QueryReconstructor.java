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
		
		// so, at the bottom level it will generate a query and name the resulting temp table
		// then return to its parent
		// once all children have been processed, the parent can use the child temp tables in its own query
	}
	
	void generateTempQuery(JSONObject curNode)
	{
		JSONArray childrenNodes = qParser.getChildrenPlanNodes(curNode);
		// iterate through all the children plans of the top level node
		if (childrenNodes == null) 
		{
			// process leaf node
			
			// okay, now to figure out how to actually generate a query from the plan
			// what do we need?
			
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
				
				// print some attributes of this node
				System.out.println("Node Type: " + qParser.getNodeType(curChild));
				System.out.println("Alias: " + qParser.getAlias(curChild));
				System.out.println("Parent Relationship: " + qParser.getParentRelationship(curChild));
				System.out.println("Hash Condition: " + qParser.getHashCond(curChild));
				System.out.println("Join Type: " + qParser.getJoinType(curChild));
				System.out.println("---------end of one node-----------");
			}
		}
		// then, once children have been processed, we can use them
		
		
		// assign temp table name to the result of this node
		// put it somewhere we can use it later (like the JSONObject, maybe add an attribute)
	}
}
