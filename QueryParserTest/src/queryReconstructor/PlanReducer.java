package queryReconstructor;

import queryParser.QueryParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PlanReducer {
	
	// this class will simplify the query plan
	
	// for example, it will reduce all hash join, merge join, etc into join nodes
	// it will eliminate unnecessary seq scan nodes
	
	QueryParser qParser;
	static int curTmp = 0;
	
	// will return:
	// a tree of JSONObjects
	// node type (join, scan, or aggregate)
	// output fields
	// conditions
	// tables to select from
	// temp table name
	// later aggregate information
	
	public PlanReducer(QueryParser qp)  
	{
		qParser = qp;
	}

	JSONObject ReduceNode(JSONObject curNode) {
		JSONObject reducedCurNode = new JSONObject();
		switch (getEnumFromNodeType(qParser.getNodeType(curNode))) 
		{
		// joins
		case HASH_JOIN:
			break;
		case MERGE_JOIN:
			break;
		case NESTED_LOOP:
			break;
			// need other join types as well (left joins, etc)
			
		// intermediate type things
		case HASH:
			break;
		case SORT:
			break;
		// scans
			
		case INDEX_SCAN:
			break;
		case SEQ_SCAN:
			break;
		case BITMAP_HEAP_SCAN:
			break;
		case BITMAP_INDEX_SCAN:
			break;
		//misc
		case LIMIT:
			break;
		case UNDEFINED:
			break;
		}
		
		// update reducedParentNode
		
		// return the new reduced node
		return reducedCurNode;
	}
	
	JSONObject ReduceHashJoinNode(JSONObject curNode) {
		JSONObject reducedNode = new JSONObject();
		JSONArray children = qParser.getChildrenPlanNodes(curNode);
		
		// NOTE: not sure if casting to JSONObject will work
		JSONObject reducedFirstChild = ReduceNode((JSONObject) children.get(0));
		JSONObject reducedSecondChild = ReduceNode((JSONObject) children.get(1));
		
		String joinCond = qParser.getHashCond(curNode);
		
		reducedNode = makeJoinNode(joinCond, reducedFirstChild, reducedSecondChild);
		
		return reducedNode;
	}
	
	JSONObject ReduceMergeJoinNode(JSONObject curNode) {
		JSONObject reducedNode = new JSONObject();
		JSONArray children = qParser.getChildrenPlanNodes(curNode);
		
		// NOTE: not sure if casting to JSONObject will work
		JSONObject reducedFirstChild = ReduceNode((JSONObject) children.get(0));
		JSONObject reducedSecondChild = ReduceNode((JSONObject) children.get(1));
		
		String joinCond = qParser.getMergeCond(curNode);
		
		reducedNode = makeJoinNode(joinCond, reducedFirstChild, reducedSecondChild);
		
		return reducedNode;
	}

	JSONObject ReduceNestedLoopJoinNode(JSONObject curNode) {
		JSONObject reducedNode = new JSONObject();		
		JSONArray children = qParser.getChildrenPlanNodes(curNode);	
		// NOTE: not sure if casting to JSONObject will work
		JSONObject reducedFirstChild = ReduceNode((JSONObject) children.get(0));
		JSONObject reducedSecondChild = ReduceNode((JSONObject) children.get(1));
		
		// join cond is in second child's index condition
		// and also possibly joinFilter attribute if multiple conditions
		String joinFilter = qParser.getJoinFilter(curNode);
		String joinCond = qParser.getIndexCond(reducedSecondChild);
		String alias = qParser.getAlias(reducedSecondChild);
		// may need to get alias from index scan node and apply it 
		// to the index cond 
		// need
		
		reducedNode = makeJoinNode(joinCond, reducedFirstChild, reducedSecondChild);
		
		return reducedNode;
	}

	
	JSONObject makeJoinNode(String joinCond, JSONObject reducedFirstChild, JSONObject reducedSecondChild) 
	{
		
		// things required to make a join tmp table:
		// input table names
		// join conditions (all)
		// output table name
		// other filters? I think those will typically be applied in scan nodes if they're not join conditions
		
		JSONObject reducedJoinNode = new JSONObject();
		JSONArray children = new JSONArray();
		children.add(reducedFirstChild);
		children.add(reducedSecondChild);
		reducedJoinNode.put("children", children);
		reducedJoinNode.put("joinCondition", joinCond);
		reducedJoinNode.put("tableName", "tmp" + curTmp);
		
		// join node should contain:
		// array of children
		// its tmp tablename
		// possibly names of child tables (or could be acquired from child nodes)
		// join condition
		// anything else?
		
		// 
		
		return reducedJoinNode;
	}
		
	
	JSONObject makeScanNode(String filterCond, String inputTable) {
		// things required to make a scan tmp table
		// when do we actually want a scan node vs. directly incorporating it into the parent node?
		// e.g., if it's literally nothing more than a sequential scan, then we don't want to make a node for it
		// maybe we should have a boolean indicating whether it should be kept as a node
		
		// keep the old alias
		
		
		JSONObject reducedScanNode = new JSONObject();
		
		// 
		
		return reducedScanNode;
	}
		// node type: if it's a join type (hash, sort merge, inl, nl), we'll make a join node
		// we'll look at the join conditions and stick them in the where clause
		// may be some complications with aliasing/figuring out what relation an attribute comes from
		// possible solution: keep a set of aliases that came from the children of a node, consult if necessary
		
		// nested loop joins are funny. the first child has a normal condition, but the join condition looks like it's actually in
		// the index cond of the second child
		// also, there is something called a join filter, which has additional join conditions
		// HOWEVER, the join filter won't necessarily eliminate rows in an outer join (may be null extended),
		// while the filter conditions will definitely eliminate rows
		
		// the filter attribute will have selection conditions
		
		// for index scan nodes, the filter condition will be in the index cond
		
		// looks like bitmap scans sometimes cause interesting things to happen (a condition checked at two different points in the plan)
		// notably, there are bitmap heap scan nodes, which have a "recheck cond" instead of a regular filter. 
		// we may be able to ignore this by having the initial index cond take care of it
		// and ignoring the "recheck cond" since it will already be done.
		
		// hmm, there are limit nodes. this is definitely a place where a lot of optimization can take place (if we combine the nodes)
		
		
		// if a child is any sort of scan with no filters, we can probably just ignore it
		// in the parent, just use the table it was sequential scanning over 
		// could implement this by just naming the temp table member to be the table being scanned
		
		// any sort of scan node can possibly be ignored by putting the conditions in the parent node
		// or we could just do the filter and use it as input
		// will be faster to put conditions on parent node
		
		// a hash node probably doesn't need any processing - just use the result of its children
		// same thing for sort
	
	private enum NODE_TYPE {
		// may still need other types of left join
		// may also need right join and possibly full join? not sure about what types of nodes there are
		// there may also be materialize nodes?
		HASH_JOIN,
		HASH_LEFT_JOIN,
		MERGE_JOIN,
		MERGE_LEFT_JOIN,
		NESTED_LOOP,
		NESTED_LOOP_LEFT_JOIN,
		HASH,
		SORT,
		INDEX_SCAN,
		SEQ_SCAN,
		BITMAP_HEAP_SCAN,
		BITMAP_INDEX_SCAN,
		LIMIT,
		UNDEFINED;
	};
		
		private NODE_TYPE getEnumFromNodeType(String nodeType)
		{
			if (nodeType == "Hash Join") { return NODE_TYPE.HASH_JOIN; }
			if (nodeType == "Hash Left Join") { return NODE_TYPE.HASH_LEFT_JOIN; }
			if (nodeType == "Merge Join") { return NODE_TYPE.MERGE_JOIN; }
			if (nodeType == "Merge Left Join") { return NODE_TYPE.MERGE_LEFT_JOIN; }
			if (nodeType == "Nested Loop") { return NODE_TYPE.NESTED_LOOP; }
			if (nodeType == "Nested Loop Left Join") { return NODE_TYPE.NESTED_LOOP_LEFT_JOIN; }
			if (nodeType == "Hash") { return NODE_TYPE.HASH; }
			if (nodeType == "Sort") { return NODE_TYPE.SORT; }
			if (nodeType == "Index Scan") { return NODE_TYPE.INDEX_SCAN; }
			if (nodeType == "Seq Scan") { return NODE_TYPE.SEQ_SCAN; }
			if (nodeType == "Bitmap Heap Scan") { return NODE_TYPE.BITMAP_HEAP_SCAN; }
			if (nodeType == "Bitmap Index Scan") { return NODE_TYPE.BITMAP_INDEX_SCAN; }
			return NODE_TYPE.UNDEFINED;
		}

}
