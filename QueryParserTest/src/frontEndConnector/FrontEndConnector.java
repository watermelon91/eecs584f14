package frontEndConnector;

import java.sql.SQLException;
import java.util.List;

import org.json.simple.JSONObject;

import binaryTree.LinkedBinaryTreeNode;
import queryParser.QueryParser;
import queryReconstructor.PlanReducer;
import queryReconstructor.QueryReconstructor;
import databaseConnector.PostgresDBConnector;

public class FrontEndConnector {
	
	private String dbIP = "";
	private String dbName = "";
	private String userName = "";
	private String password = "";
	private PostgresDBConnector pdbConnector = null;
	private List<String> tmpTableNames = null;
	
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
		String queryPlanStr = executeQuery("EXPLAIN (VERBOSE TRUE, FORMAT JSON) " + query);
		QueryParser qParser = new QueryParser(queryPlanStr, false);
		
		// reduce plan
		PlanReducer pReducer = new PlanReducer(qParser);
		// construct CREATE TMP TABLE queries in the reduced plan
		QueryReconstructor qReconstructor = new QueryReconstructor(pReducer);
		
		// convert to UI tree format
		BinaryTreeConverter converter = new BinaryTreeConverter(qReconstructor);
		LinkedBinaryTreeNode<QueryPlanTreeNode>  treeRoot =  converter.convertToTree();	
		initializeAllTempTables(converter.getAllTempTableCreateStatements());
		tmpTableNames = converter.getAllTempTableNames();
		
		return treeRoot;
	}
	
	/*
	 * Input: The table name a user wants to retrieve data from.
	 *        This table needs to be one of the tables that appears in one 
	 *        of the nodes returned in JSONObject from debugQuery()
	 * Output: Sample data. See executeTestQuery() for detail.
	 */
	public List<String[]> getSampleData(String tableName)
	{
		return executeTestQuery("SELECT * FROM " + tableName + " LIMIT 10");	
	}
	
	public List<String[]> getAllSampleData(String tableName)
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
	public List<String[]> executeTestQuery(String query)
	{
		List<String[]> result = null;
		
		try 
		{
			result = pdbConnector.executeQuerySeparateResult(query);
			return result;
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	public String executeQuery(String query)
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
	
	public String dropAllTmpTables()
	{
		String errMsg = "";
		System.out.println("DROP ALL TMP TABLES");
		
		if(tmpTableNames == null)
		{
			// nothing has initialized yet.
			return "";
		}
		
		for(int i = 0; i < tmpTableNames.size(); i++)
		{
			try {
				System.out.println("DROP TABLE " + tmpTableNames.get(i));
				pdbConnector.executeNonSelectQuery("DROP TABLE " + tmpTableNames.get(i));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				errMsg = errMsg + "\n" + e.getStackTrace().toString();
			}
		}
		
		return "";
	}
	
	public String closeDBConnection()
	{
		dropAllTmpTables();
		return pdbConnector.closeConnector();
	}
	
	private void initializeAllTempTables(List<String> createStatements)
	{
		System.out.println("START TMP TABLE CREATION...");
		for (int i = createStatements.size() - 1; i >= 0; i--)
		{
			try 
			{
				System.out.println(createStatements.get(i));
				pdbConnector.executeNonSelectQuery(createStatements.get(i));
			} 
			catch (SQLException e) 
			{
				// TODO Auto-generated catch block
				System.out.println(createStatements.get(i) + " FAILED");
				e.printStackTrace();
			}
		}
	}
}
