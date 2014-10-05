package QueryParser;

import java.io.FileReader;
import java.io.IOException;
import java.lang.System;
import java.util.HashMap;
import java.util.Iterator;

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
	
	public JSONObject toplevelNode;
		
	public QueryParser(String inputFilePath)
	{
		try {
			
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader(inputFilePath));
	 
			// returns top level object {} enclosed in top level []
			JSONArray jsonArray = (JSONArray) obj; 	
			// there can only be one top level object, since the query plan has to converge to one eventually
			if(jsonArray.size() != 1)
			{
				throw new MultipleTopLevelNodeException();
			}
			// get the top level plan
			JSONObject topLevelPlan = null;
			Iterator<JSONObject>iterator = jsonArray.iterator();	// returns the top level "plan"
			while(iterator.hasNext())
			{
				topLevelPlan = iterator.next();
				System.out.println(topLevelPlan.get("Plan").toString());
				toplevelNode = (JSONObject)topLevelPlan.get("Plan");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (MultipleTopLevelNodeException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public JSONArray getChildrenPlanNodes(JSONObject currentNode)
	{
		// returns the Plans in Children level 
		return (JSONArray)currentNode.get("Plans");
	}
	
	public String getNodeType(JSONObject currentNode)
	{
		// return the node type of the current node
		return currentNode.get("Node Type").toString();
	}
	
	// exception class
	@SuppressWarnings("serial")
	private class MultipleTopLevelNodeException extends Exception{}
	
	// TODO
	// 1. create enum and a map mapping enum to string for each attribute that
	// can be returned in the query plan
	// 2. create get functions for each attribtue
	private enum QUERY_PLAN_ATTRS {actual_loops,          
		actual_rows,           
		actual_time_first,     
		actual_time_last,      
		estimated_rows,        
		estimated_row_width,    
		estimated_startup_cost, 
		estimated_total_cost,   
		extra_info,             
		initplans,              
		never_executed,         
		scan_on,                
		sub_nodes,              
		subplans,               
		type,                   
		ctes,                   
		cte_order};
	private HashMap<QUERY_PLAN_ATTRS, String> queryPlanAttrDictionary = new HashMap<QUERY_PLAN_ATTRS, String>(){
		
	};

}
