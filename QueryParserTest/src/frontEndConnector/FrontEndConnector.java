package frontEndConnector;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
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
	
	public class Pair{
		public String[] attributes;
		public List<String[]> data;
		
		public Pair(String[] _attr, List<String[]> _data){
			attributes = _attr;
			data = _data;
		}
	}
	
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
	public Pair getSampleData(String tableName)
	{
		return executeTestQuery("SELECT * FROM " + tableName + " LIMIT 10");	
	}
	
	public Pair getAllSampleData(String tableName)
	{
		return executeTestQuery("SELECT * FROM " + tableName);
	}
	
	/*
	 * Input: A query that the user wants to run on the test data 
	 * 		  generated using getSampleData()
	 * Output: Query result limit to top 10.
	 * 		   The query result is stored by row into a List<String> and then 
	 *		   converted into a string use the toString() method.
	 *         Use string.split(",") to convert it back to an array. 
	 *         See QueryParserTest for example. 
	 */
	public Pair executeTestQuery(String query)
	{
		int LIMIT = 10;
		
		try 
		{
			List<String[]> result = pdbConnector.executeQuerySeparateResult(query, LIMIT);
			String[] attrs = getReturnedAttr(query);
			
			Pair rstPair = new Pair(attrs, result);			
			return rstPair;
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}		
	}
	
	/*
	 * Input: A query that the user wants to run on the test data 
	 * 		  generated using getSampleData()
	 * Output: Query result ALL.
	 * 		   The query result is stored by row into a List<String> and then 
	 *		   converted into a string use the toString() method.
	 *         Use string.split(",") to convert it back to an array. 
	 *         See QueryParserTest for example. 
	 */
	public Pair executeTestQueryAll(String query)
	{
		try 
		{
			List<String[]> result = pdbConnector.executeQuerySeparateResult(query);
			String[] attrs = getReturnedAttr(query);
			
			Pair rstPair = new Pair(attrs, result);			
			return rstPair;
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	private String[] getReturnedAttr(String query)
	{
		String queryPlanStr = executeQuery("EXPLAIN (VERBOSE TRUE, FORMAT JSON) " + query);
		QueryParser qParser = new QueryParser(queryPlanStr, true);
		JSONArray attrs = qParser.getOutputAttributes(qParser.topLevelNode);
		System.out.println(attrs.toJSONString());
		
		String[] attrStrArray = new String[attrs.size()];
		Iterator<String> aiterator = attrs.iterator();
		int count = 0;
		while(aiterator.hasNext() && count < attrs.size())
		{
			attrStrArray[count] = aiterator.next();
			count++;
		}
		
		return attrStrArray;
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
