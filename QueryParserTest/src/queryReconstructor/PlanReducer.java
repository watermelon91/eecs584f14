package queryReconstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
	public JSONObject topLevelNode;
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
		topLevelNode = ReduceNode(qp.topLevelNode);
	}

	JSONObject ReduceNode(JSONObject curNode) {
		JSONObject reducedCurNode = new JSONObject();
		switch (getEnumFromNodeType(qParser.getNodeType(curNode))) 
		{
		// joins
		case HASH_JOIN:
			reducedCurNode = ReduceHashJoinNode(curNode);
			break;
		case MERGE_JOIN:
			reducedCurNode = ReduceMergeJoinNode(curNode);
			break;
		case NESTED_LOOP:
			reducedCurNode = ReduceNestedLoopJoinNode(curNode);
			break;
			// need other join types as well (left joins, etc)
			
		// intermediate type things
		case HASH:
			reducedCurNode = ReduceSortOrHashNode(curNode);
			break;
		case SORT:
			reducedCurNode = ReduceSortOrHashNode(curNode);
			break;
		// scans
			
		case INDEX_SCAN:
			reducedCurNode = ReduceIndexScanNode(curNode);
			break;
		case SEQ_SCAN:
			reducedCurNode = ReduceSeqScanNode(curNode);
			break;
		case BITMAP_HEAP_SCAN:
			reducedCurNode = ReduceBitmapHeapScanNode(curNode);
			break;
		case BITMAP_INDEX_SCAN:
			// don't think we should ever run into one of these, 
			// since I believe it will always be a child of a bitmap heap scan.
			break;
			
		//misc
		case LIMIT:
			// probably not going to do anything about this yet.
			break;
		case UNDEFINED:
			reducedCurNode = null;
			break;
		}
		
		// update reducedParentNode
		
		// return the new reduced node
		System.out.println(reducedCurNode.toJSONString());
		return reducedCurNode;
	}
	
	// TODO: all join nodes: need alias set from children so we can resolve the outputAttrs alias stuff.
	
	JSONObject ReduceHashJoinNode(JSONObject curNode) {
		JSONObject reducedNode = new JSONObject();
		JSONArray children = qParser.getChildrenPlanNodes(curNode);
		
		// NOTE: not sure if casting to JSONObject will work
		JSONObject reducedFirstChild = ReduceNode((JSONObject) children.get(0));
		JSONObject reducedSecondChild = ReduceNode((JSONObject) children.get(1));
		
		String joinCond = qParser.getHashCond(curNode);
		String filter = qParser.getFilter(curNode);
		JSONArray outputAttrs = qParser.getOutputAttributes(curNode);
		
		reducedNode = MakeJoinNode(joinCond, filter, reducedFirstChild, reducedSecondChild, outputAttrs);
		
		return reducedNode;
	}
	
	JSONObject ReduceMergeJoinNode(JSONObject curNode) {
		JSONObject reducedNode = new JSONObject();
		JSONArray children = qParser.getChildrenPlanNodes(curNode);
		
		JSONObject reducedFirstChild = ReduceNode((JSONObject) children.get(0));
		JSONObject reducedSecondChild = ReduceNode((JSONObject) children.get(1));
		
		String joinCond = qParser.getMergeCond(curNode);
		String filter = qParser.getFilter(curNode);
		JSONArray outputAttrs = qParser.getOutputAttributes(curNode);
		
		reducedNode = MakeJoinNode(joinCond, filter, reducedFirstChild, reducedSecondChild, outputAttrs);
		
		return reducedNode;
	}

	JSONObject ReduceNestedLoopJoinNode(JSONObject curNode) {
		JSONObject reducedNode = new JSONObject();		
		JSONArray children = qParser.getChildrenPlanNodes(curNode);	
		JSONObject reducedFirstChild = ReduceNode((JSONObject) children.get(0));
		JSONObject reducedSecondChild = ReduceNode((JSONObject) children.get(1));
		
		// join cond is in second child's index condition
		// and also possibly joinFilter attribute if multiple conditions
		String joinFilter = qParser.getJoinFilter(curNode);
		String joinCond = qParser.getIndexCond(reducedSecondChild);
		String alias = qParser.getAlias(reducedSecondChild);
		String filter = qParser.getFilter(curNode);
		JSONArray outputAttrs = qParser.getOutputAttributes(curNode);
		
		if (joinFilter != "") {
			joinCond = joinCond + " AND " + joinFilter;
		}
		
		// need to combine joinFilter and joinCond
		// also all of these should handle filters
		
		reducedNode = MakeJoinNode(joinCond, filter, reducedFirstChild, reducedSecondChild, outputAttrs);
		
		return reducedNode;
	}

	JSONObject ReduceSortOrHashNode(JSONObject obj) {
		// sort and hash nodes don't do anything (at least in non-JSON formatted query plans)
		JSONArray children = qParser.getChildrenPlanNodes(obj);
		//TODO: may want this to take the output attributes from this node instead? probably not, but maybe
		JSONObject reducedNode = ReduceNode((JSONObject) children.get(0));
		// maybe should get outputAttributes from this node though
		
		return reducedNode;
	}

	JSONObject ReduceBitmapHeapScanNode(JSONObject curNode) {
		JSONObject reducedNode = new JSONObject();
		
		// we know this node will have a recheck cond, which is what we want for the selection condition.
		// we can ignore everything below it, because all of the conditions will be index conditions
		// that are re-checked in recheckCond.
		String indexCond = qParser.getRecheckCond(curNode);
		String filter = qParser.getFilter(curNode);
		String inputTable = qParser.getRelationName(curNode);
		String alias = qParser.getAlias(curNode);
		JSONArray outputAttrs = qParser.getOutputAttributes(curNode);
		reducedNode = MakeScanNode(indexCond, inputTable, alias, outputAttrs);
		
		// need to use filter
		
		return reducedNode;
	}
	
	JSONObject ReduceIndexScanNode(JSONObject curNode) {
		JSONObject reducedNode = new JSONObject();
		
		// may have index cond AND/OR filter
		// won't necessarily have either
		
		String indexCond = qParser.getIndexCond(curNode);
		String filter = qParser.getFilter(curNode);
		String inputTable = qParser.getRelationName(curNode);
		String alias = qParser.getAlias(curNode);
		JSONArray outputAttrs = qParser.getOutputAttributes(curNode);
		
		//TODO
		
		reducedNode = MakeScanNode(indexCond, inputTable, alias, outputAttrs);		
		
		return reducedNode;
	}
	
	JSONObject ReduceSeqScanNode(JSONObject curNode) {
		JSONObject reducedNode = new JSONObject();
		
		// may or may not have anything in Filter
		
		String filter = qParser.getFilter(curNode);
		String inputTable = qParser.getRelationName(curNode);
		String alias = qParser.getAlias(curNode);
		JSONArray outputAttrs = qParser.getOutputAttributes(curNode);
		reducedNode = MakeScanNode(filter, inputTable, alias, outputAttrs);		
		// may not have a condition - should check
		
		return reducedNode;
	}
	
	JSONObject MakeJoinNode(String joinCond, String filter, JSONObject reducedFirstChild, JSONObject reducedSecondChild, JSONArray outputAttrs) 
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
		// get aliasSets from children.
		// what happens if we don't alias tables?
		
		JSONArray aliasSet = concatArrays(getAliasSet(reducedFirstChild), getAliasSet(reducedSecondChild));

		reducedJoinNode.put("type", "join");
		reducedJoinNode.put("children", children);
		reducedJoinNode.put("filter", filter);
		reducedJoinNode.put("joinCondition", joinCond);
		reducedJoinNode.put("outputAttrs", outputAttrs);
		reducedJoinNode.put("aliasSet", aliasSet);
		reducedJoinNode.put("newTableName", "tmp" + curTmp);
		curTmp++;
		
		// join node should contain:
		// array of children
		// its tmp tablename
		// possibly names of child tables (or could be acquired from child nodes)
		// join condition
		// anything else?
		
		// 
		
		return reducedJoinNode;
	}
		
	
	JSONObject MakeScanNode(String filterCond, String inputTable, String alias, JSONArray outputAttrs) {
		// scan node can have children or no children, right?
		
		// things required to make a scan tmp table
		// when do we actually want a scan node vs. directly incorporating it into the parent node?
		// e.g., if it's literally nothing more than a sequential scan, then we don't want to make a node for it
		// maybe we should have a boolean indicating whether it should be kept as a node
		// for now, though, make all nodes
		
		JSONObject reducedScanNode = new JSONObject();
		JSONArray aliasSet = new JSONArray();
		aliasSet.add(alias);
		reducedScanNode.put("type", "scan");
		reducedScanNode.put("filter", filterCond);
		reducedScanNode.put("aliasSet", aliasSet);
		reducedScanNode.put("inputTable", inputTable);
		reducedScanNode.put("outputAttrs", outputAttrs);
		reducedScanNode.put("newTableName", "tmp" + curTmp);
		curTmp++;
		
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
		// OR we could ignore the child scan nodes and use only the recheck cond, since it will have both
		
		// hmm, there are limit nodes. this is definitely a place where a lot of optimization can take place (if we combine the nodes)
		
		
		// if a child is any sort of scan with no filters, we can probably just ignore it
		// in the parent, just use the table it was sequential scanning over 
		// could implement this by just naming the temp table member to be the table being scanned
		
		// any sort of scan node can possibly be ignored by putting the conditions in the parent node
		// or we could just do the filter and use it as input
		// will be faster to put conditions on parent node
		
		// a hash node probably doesn't need any processing - just use the result of its children
		// same thing for sort
	
	
		// HMMMM explain analyze does cool things (e.g. tells you how many rows were removed by a condition)
	
	JSONArray concatArrays(JSONArray a1, JSONArray a2) {
		JSONArray newAr = new JSONArray();
		Iterator<JSONObject> it = a1.iterator();	
		while(it.hasNext())
		{
			newAr.add(it.next());
		}
		
		it = a2.iterator();	
		while(it.hasNext())
		{
			newAr.add(it.next());
		}
 
		return newAr;
	}
	
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
		System.out.println(nodeType);
		if (nodeType.equals("Hash Join")) { return NODE_TYPE.HASH_JOIN; }
		if (nodeType.equals("Hash Left Join")) { return NODE_TYPE.HASH_LEFT_JOIN; }
		if (nodeType.equals("Merge Join")) { return NODE_TYPE.MERGE_JOIN; }
		if (nodeType.equals("Merge Left Join")) { return NODE_TYPE.MERGE_LEFT_JOIN; }
		if (nodeType.equals("Nested Loop")) { return NODE_TYPE.NESTED_LOOP; }
		if (nodeType.equals("Nested Loop Left Join")) { return NODE_TYPE.NESTED_LOOP_LEFT_JOIN; }
		if (nodeType.equals("Hash")) { return NODE_TYPE.HASH; }
		if (nodeType.equals("Sort")) { return NODE_TYPE.SORT; }
		if (nodeType.equals("Index Scan")) { return NODE_TYPE.INDEX_SCAN; }
		if (nodeType.equals("Seq Scan")) { return NODE_TYPE.SEQ_SCAN; }
		if (nodeType.equals("Bitmap Heap Scan")) { return NODE_TYPE.BITMAP_HEAP_SCAN; }
		if (nodeType.equals("Bitmap Index Scan")) { return NODE_TYPE.BITMAP_INDEX_SCAN; }
		System.out.println("undefined");
		return NODE_TYPE.UNDEFINED;
	}

	private enum REDUCED_PLAN_ATTRS {
		TYPE,
		ALIAS_SET,
		FILTER,
		INPUT_TABLE,
		NEW_TABLE_NAME,
		JOIN_CONDITION,
		OUTPUT_ATTRS,
		CHILDREN
		};
		
	// mapping between enum and the actual returned string for all attributes EXPLAIN can return
	private static final Map<REDUCED_PLAN_ATTRS, String> reducedPlanAttrMapping = reducedPlanAttrMappingInitializer();
	private static Map<REDUCED_PLAN_ATTRS, String> reducedPlanAttrMappingInitializer()
	{
		 Map<REDUCED_PLAN_ATTRS, String> map = new HashMap<REDUCED_PLAN_ATTRS, String>();
		 map.put(REDUCED_PLAN_ATTRS.TYPE, "type");
		 map.put(REDUCED_PLAN_ATTRS.ALIAS_SET, "aliasSet");
		 map.put(REDUCED_PLAN_ATTRS.FILTER, "filter");
		 map.put(REDUCED_PLAN_ATTRS.INPUT_TABLE, "inputTable");
		 map.put(REDUCED_PLAN_ATTRS.NEW_TABLE_NAME, "newTableName");
		 map.put(REDUCED_PLAN_ATTRS.JOIN_CONDITION, "joinCondition");
		 map.put(REDUCED_PLAN_ATTRS.OUTPUT_ATTRS, "outputAttrs");
		 map.put(REDUCED_PLAN_ATTRS.CHILDREN, "children");
		 return Collections.unmodifiableMap(map);
	}
	
	public String getType(JSONObject currentNode)
	{
		Object rst = currentNode.get(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.TYPE));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}

	public JSONArray getAliasSet(JSONObject currentNode)
	{
		return (JSONArray)currentNode.get(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.ALIAS_SET));
	}
	
	public String getFilter(JSONObject currentNode)
	{
		Object rst = currentNode.get(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.FILTER));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}

	public String getInputTable(JSONObject currentNode)
	{
		Object rst = currentNode.get(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.INPUT_TABLE));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}
	
	public String getNewTableName(JSONObject currentNode)
	{
		Object rst = currentNode.get(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.NEW_TABLE_NAME));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}
	
	public String getJoinCondition(JSONObject currentNode)
	{
		Object rst = currentNode.get(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.JOIN_CONDITION));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}

	public JSONArray getOutputAttributes(JSONObject currentNode)
	{
		return (JSONArray)currentNode.get(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.OUTPUT_ATTRS));
	}
	
	public JSONArray getChildren(JSONObject currentNode)
	{
		return (JSONArray)currentNode.get(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.CHILDREN));
	}

	private static final String EMPTY_STRING = "";

	public static void main(String [ ] args) throws Exception 
	{
	
		// input file containing the returned query plan
	//	String inputFilePath = "/Users/watermelon/Dropbox/EECS584/Project/code/eecs584f14/TestingData/QueryPlan1_verbose.txt";
		String inputFilePath = "/afs/umich.edu/user/d/a/daneliza/dwtemp/F14/eecs584/eecs584f14/TestingData/QueryPlan1_verbose.txt";
		
		// create a new query parser for this query plan
		QueryParser qParser = new QueryParser(inputFilePath);	
		// get the top level node
		System.out.println("\n---------Parsed query---------");
		PlanReducer pr = new PlanReducer(qParser);
		
		// previous code -- ignore
		/*BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
		String line = "", jsonString = "";
		while ((line = reader.readLine()) != null) {
		    jsonString = jsonString + line;
		}
		System.out.println(jsonString);*/
	}
	
}
