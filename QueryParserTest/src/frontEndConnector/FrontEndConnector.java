package frontEndConnector;

import java.sql.SQLException;
import java.util.List;

import org.json.simple.JSONObject;

import binaryTree.LinkedBinaryTreeNode;
import queryParser.QueryParser;
import queryReconstructor.PlanReducer;
import databaseConnector.PostgresDBConnector;

public class FrontEndConnector {
	
	private String dbIP = "";
	private String dbName = "";
	private String userName = "";
	private String password = "";
	private PostgresDBConnector pdbConnector = null;
	
	public FrontEndConnector(String _dbIP, String _dbName, String _userName, String _password)
	{
		dbIP = _dbIP;
		dbName = _dbName;
		userName = _userName;
		password = _password;
		
		pdbConnector = new PostgresDBConnector();	
	}
	
	/* 
	 * Given the user info passed into the constructor, establish a 
	 * SQL connection to the database
	 */
	public String initializeSQLConnection()
	{
		return pdbConnector.initalizeConnector(dbIP, dbName, userName, password);
	}
	
	/*
	 * Input: a query the user wants to debug
	 * Output: a JSONObject representing the execution plan of the query
	 */
	public LinkedBinaryTreeNode<QueryPlanTreeNode>  debugQuery(String query) throws Exception
	{
		// get the query plan in string
		String queryPlanStr = executeTestQuery("EXPLAIN (VERBOSE TRUE, FORMAT JSON) " + query);
		QueryParser qParser = new QueryParser(queryPlanStr, false);
		PlanReducer pReducer = new PlanReducer(qParser);
		
		// TODO
		// call Dana's QueryReducer to get the JSONObject with reduced plan
		
		BinaryTreeConverter converter = new BinaryTreeConverter(pReducer);
		LinkedBinaryTreeNode<QueryPlanTreeNode>  treeRoot =  converter.convertToTree();
		
		// return the unreduced node for now
		return treeRoot;
	}
	
	/*
	 * Input: The table name a user wants to retrieve data from.
	 *        This table needs to be one of the tables that appears in one 
	 *        of the nodes returned in JSONObject from debugQuery()
	 * Output: Sample data. See executeTestQuery() for detail.
	 */
	public String getSampleData(String tableName)
	{
		return executeTestQuery("SELECT * FROM " + tableName);	
	}
	
	/*
	 * Input: A query that the user wants to run on the test data 
	 * 		  generated using getSampleData()
	 * Output: Query result. 
	 * 		   The query result is stored by row into a List<String> and then 
	 *		   converted into a string use the toString() method.
	 *         Use string.split(",") to convert it back to an array. 
	 *         See QueryParserTest for example. 
	 */
	public String executeTestQuery(String query)
	{
		List<String> result = null;
		try 
		{
			result = pdbConnector.executeQuery(query);
			String resultStr = result.toString();
			
			// use substring to remove the extra pair of [] added by List.toString()
			return resultStr.substring(1, resultStr.length()-1);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	public String closeDBConnection()
	{
		return pdbConnector.closeConnector();
	}
}
