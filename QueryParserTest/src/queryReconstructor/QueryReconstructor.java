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
	public QueryReconstructor(QueryParser qp)  
	{
		qParser = qp;
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
			
			// node type: if it's a join type (hash, sort merge, inl, nl), we'll do a join
			// we'll look at the join conditions and stick them in the where clause
			// may be some complications with aliasing/figuring out what relation an attribute comes from
			// possible solution: keep a set of aliases that came from the children of a node, consult if necessary
			
			// the filter attribute will have any selection conditions
			
			// for index scan nodes, the filter condition will be in the index cond
			
			// looks like bitmap scans sometimes cause interesting things to happen (a condition checked at two different points in the plan)
			// notably, there are bitmap heap scan nodes, which have a "recheck cond" instead of a regular filter. 
			// we may be able to ignore this by having the initial index cond take care of it
			// and ignoring the "recheck cond" since it will already be done.
			
			// hmm, there are limit nodes. this is definitely a place where a lot of optimization can take place (if we combine the nodes)
			
			// nested loop joins are funny. the first child has a normal condition, but the join condition looks like it's actually in
			// the index cond of the second child
			// also, there is something called a join filter, which has additional join conditions
			// HOWEVER, the join filter won't necessarily eliminate rows in an outer join (may be null extended),
			// while the filter conditions will definitely eliminate rows
			
			
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
