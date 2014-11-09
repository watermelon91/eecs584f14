package queryParser;

import java.io.FileReader;
import java.io.IOException;
import java.lang.System;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class QueryParser {
	/*
	 * Query parser for a query plan returned from SQL
	 * 
	 * A query parser instance is bind to one query plan. Create
	 * multiple query parsers if there are multiple query plans.
	 */
	
	public JSONObject topLevelNode;
		
	// constructor for read-in query plan from file
	public QueryParser(String inputFilePath)
	{
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader(inputFilePath));
	 
			JSONArray topLevelArray = (JSONArray) obj; 	// top level object "{}" enclosed in top level "[]"
			initializeTopLevelNode(topLevelArray);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (MultipleTopLevelNodeException e) {
			e.printStackTrace();
			return;
		}
	}
	
	// constructor for read-in query plan from string
	public QueryParser(String queryPlanJsonString, Boolean dummy)
	{
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(queryPlanJsonString);
	 
			JSONArray topLevelArray = (JSONArray) obj; 	// top level object "{}" enclosed in top level "[]"
			initializeTopLevelNode(topLevelArray);
			
		}catch (ParseException e) {
			e.printStackTrace();
		} catch (MultipleTopLevelNodeException e) {
			e.printStackTrace();
			return;
		}
	}
	
	// get functions for all children plan
	public JSONArray getChildrenPlanNodes(JSONObject currentNode)
	{
		// returns the Plans in Children level 
		// returns NULL if no children
		return (JSONArray)currentNode.get(queryPlanAttrMapping.get(QUERY_PLAN_ATTRS.PLANS));
	}
	
	public JSONArray getOutputAttributes(JSONObject currentNode)
	{
		return (JSONArray)currentNode.get(queryPlanAttrMapping.get(QUERY_PLAN_ATTRS.OUTPUT));
	}
	
	//--------------------------- get functions for different attributes returned by EXPLAIN ---------------------------
	public String getAlias(JSONObject currentNode)
	{
		// return the alias of this node
		Object rst = currentNode.get(queryPlanAttrMapping.get(QUERY_PLAN_ATTRS.ALIAS));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}
	
	public String getFilter(JSONObject currentNode)
	{
		// return the filter of this node
		Object rst = currentNode.get(queryPlanAttrMapping.get(QUERY_PLAN_ATTRS.FILTER));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return QueryProcessingUtilities.removeParenthesis(rst.toString());
	}
	
	public String getHashCond(JSONObject currentNode)
	{
		// return the hash condition of this node
		Object rst = currentNode.get(queryPlanAttrMapping.get(QUERY_PLAN_ATTRS.HASH_COND));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		
		return QueryProcessingUtilities.removeParenthesis(rst.toString());
	}
	
	public String getIndexCond(JSONObject currentNode)
	{
		// return the index condition of this node
		Object rst = currentNode.get(queryPlanAttrMapping.get(QUERY_PLAN_ATTRS.INDEX_COND));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		
		return QueryProcessingUtilities.removeParenthesis(rst.toString());
	}
	
	public String getIndexName(JSONObject currentNode)
	{
		// return the index condition of this node
		Object rst = currentNode.get(queryPlanAttrMapping.get(QUERY_PLAN_ATTRS.INDEX_NAME));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}
	
	public String getJoinType(JSONObject currentNode)
	{
		// return the join type of this node
		Object rst = currentNode.get(queryPlanAttrMapping.get(QUERY_PLAN_ATTRS.JOIN_TYPE));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}
	
	public String getJoinFilter(JSONObject currentNode)
	{
		Object rst = currentNode.get(queryPlanAttrMapping.get(QUERY_PLAN_ATTRS.JOIN_FILTER));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return QueryProcessingUtilities.removeParenthesis(rst.toString());
	}
	
	public String getMergeCond(JSONObject currentNode)
	{
		Object rst = currentNode.get(queryPlanAttrMapping.get(QUERY_PLAN_ATTRS.MERGE_COND));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return QueryProcessingUtilities.removeParenthesis(rst.toString());
	}
	
	public String getNodeType(JSONObject currentNode)
	{
		// return the node type of the current node
		Object rst = currentNode.get(queryPlanAttrMapping.get(QUERY_PLAN_ATTRS.NODE_TYPE));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}
	
	public String getParentRelationship(JSONObject currentNode)
	{
		// return the parent relationship of this node
		Object rst = currentNode.get(queryPlanAttrMapping.get(QUERY_PLAN_ATTRS.PARENT_RELATIONSHIP));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}
	
	public String getPlanRows(JSONObject currentNode)
	{
		// return the number of rows in this node
		Object rst = currentNode.get(queryPlanAttrMapping.get(QUERY_PLAN_ATTRS.PLAN_ROWS));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}
	
	public String getPlanWidth(JSONObject currentNode)
	{
		// return the plan width in this node
		Object rst = currentNode.get(queryPlanAttrMapping.get(QUERY_PLAN_ATTRS.PLAN_WIDTH));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}
	
	public String getRecheckCond(JSONObject currentNode)
	{
		// return the recheck condition in this node
		Object rst = currentNode.get(queryPlanAttrMapping.get(QUERY_PLAN_ATTRS.RECHECH_COND));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return QueryProcessingUtilities.removeParenthesis(rst.toString());
	}
	
	public String getRelationName(JSONObject currentNode)
	{
		// return the relation name of this node
		Object rst = currentNode.get(queryPlanAttrMapping.get(QUERY_PLAN_ATTRS.RELATION_NAME));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}
	
	public String getScanDirection(JSONObject currentNode)
	{
		// return the scan direction of this node
		Object rst = currentNode.get(queryPlanAttrMapping.get(QUERY_PLAN_ATTRS.SCAN_DIRECTION));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}
	
	public String getSchema(JSONObject currentNode)
	{
		// return the schema of this node
		Object rst = currentNode.get(queryPlanAttrMapping.get(QUERY_PLAN_ATTRS.SCHEMA));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}
	
	public String getStartupCost(JSONObject currentNode)
	{
		// return the startup cost of this node
		Object rst = currentNode.get(queryPlanAttrMapping.get(QUERY_PLAN_ATTRS.STARTUP_COST));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}
	
	public String getTotalCost(JSONObject currentNode)
	{
		// return the total cost of this node
		Object rst = currentNode.get(queryPlanAttrMapping.get(QUERY_PLAN_ATTRS.TOTAL_COST));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}
	
	// ------------------------------------ private functions --------------------------------------------------
	// given the topLevelArray, initialize the topLevelNode
	private void initializeTopLevelNode(JSONArray topLevelArray) throws MultipleTopLevelNodeException
	{
		// there can only be one top level object, since the query plan has to converge to one eventually
		if(topLevelArray.size() != 1)
		{
			throw new MultipleTopLevelNodeException();
		}
		// get the top level plan
		JSONObject topLevelPlan = null;
		Iterator<JSONObject>iterator = topLevelArray.iterator();	// returns the top level "plan"
		while(iterator.hasNext())
		{
			topLevelPlan = iterator.next();
			topLevelNode = (JSONObject) topLevelPlan.get(queryPlanAttrMapping.get(QUERY_PLAN_ATTRS.PLAN));
		}
		
		return;
	}

	// ------------------------------------ private class members --------------------------------------------------
	// TODO: add more attrs as we see more
	// enum of all attributes EXPLAIN can return
	private enum QUERY_PLAN_ATTRS {
		ALIAS,
		FILTER,
		HASH_COND,
		INDEX_COND,
		INDEX_NAME,
		JOIN_TYPE,
		JOIN_FILTER,
		MERGE_COND,
		NODE_TYPE, 
		OUTPUT,
		PARENT_RELATIONSHIP,
		PLAN,
		PLANS,
		PLAN_ROWS,
		PLAN_WIDTH,
		RECHECH_COND,
		RELATION_NAME,
		SCHEMA,
		SCAN_DIRECTION,
		STARTUP_COST,
		TOTAL_COST
		};
	// mapping between enum and the actual returned string for all attributes EXPLAIN can return
	private static final Map<QUERY_PLAN_ATTRS, String> queryPlanAttrMapping = queryPlanAttrMappingInitializer();
	private static Map<QUERY_PLAN_ATTRS, String> queryPlanAttrMappingInitializer()
	{
		 Map<QUERY_PLAN_ATTRS, String> map = new HashMap<QUERY_PLAN_ATTRS, String>();
		 map.put(QUERY_PLAN_ATTRS.ALIAS, "Alias");
		 map.put(QUERY_PLAN_ATTRS.FILTER, "Filter");
		 map.put(QUERY_PLAN_ATTRS.HASH_COND, "Hash Cond");
		 map.put(QUERY_PLAN_ATTRS.INDEX_COND, "Index Cond");
		 map.put(QUERY_PLAN_ATTRS.INDEX_NAME, "Index Name");
		 map.put(QUERY_PLAN_ATTRS.JOIN_TYPE, "Join Type");
		 map.put(QUERY_PLAN_ATTRS.JOIN_FILTER, "Join Filter");
		 map.put(QUERY_PLAN_ATTRS.MERGE_COND, "Merge Cond");
		 map.put(QUERY_PLAN_ATTRS.NODE_TYPE, "Node Type");
		 map.put(QUERY_PLAN_ATTRS.OUTPUT, "Output");
		 map.put(QUERY_PLAN_ATTRS.PARENT_RELATIONSHIP, "Parent Relationship");
		 map.put(QUERY_PLAN_ATTRS.PLAN, "Plan");
		 map.put(QUERY_PLAN_ATTRS.PLANS, "Plans");
		 map.put(QUERY_PLAN_ATTRS.PLAN_ROWS, "Plan Rows");
		 map.put(QUERY_PLAN_ATTRS.PLAN_WIDTH, "Plan Width");
		 map.put(QUERY_PLAN_ATTRS.RECHECH_COND, "Recheck Cond");
		 map.put(QUERY_PLAN_ATTRS.RELATION_NAME, "Relation Name");
		 map.put(QUERY_PLAN_ATTRS.SCAN_DIRECTION, "Scan Direction");
		 map.put(QUERY_PLAN_ATTRS.SCHEMA, "Schema");
		 map.put(QUERY_PLAN_ATTRS.STARTUP_COST, "Startup Cost");
		 map.put(QUERY_PLAN_ATTRS.TOTAL_COST, "Total Cost");
		 return Collections.unmodifiableMap(map);
	}
	private static final String EMPTY_STRING = "";

	// exception class
	@SuppressWarnings("serial")
	private class MultipleTopLevelNodeException extends Exception{}
	
}
