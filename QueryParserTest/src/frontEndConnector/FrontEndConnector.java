package frontEndConnector;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import binaryTree.LinkedBinaryTreeNode;
import queryParser.QueryParser;
import queryParser.QueryParser.MultipleTopLevelNodeException;
import queryReconstructor.PlanReducer;
import queryReconstructor.QueryReconstructor;
import databaseConnector.PostgresDBConnector;
import databaseConnector.PostgresDBConnector.InputQueryNotSELECTALL;
import databaseConnector.PostgresDBConnector.Pair;
import databaseConnector.PostgresDBConnector.QueryAttrNumNotMatch;
import frontEndConnector.BinaryTreeConverter.MoreThanTwoChildrenException;
import frontEndConnector.DataPlanConstructor.rowDataAndAttributeMismatchException;
import frontEndConnector.DataPlanTreeNode.BlankAttributesException;
import frontEndConnector.DataPlanTreeNode.NonMatchingAttrCountAndValueCountException;

public class FrontEndConnector {
	
	private String dbIP = "";
	private String dbName = "";
	private String userName = "";
	private String password = "";
	private PostgresDBConnector pdbConnector = null;
	private List<String> tmpTableNames = null;
	private HashMap<String, String> abbToFullTmpTableMap = new HashMap<String, String>();
		
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
	public LinkedBinaryTreeNode<QueryPlanTreeNode>  debugQuery(String query) throws SQLException, MoreThanTwoChildrenException, ParseException, MultipleTopLevelNodeException 
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
		abbToFullTmpTableMap = converter.getTmpTableNameMap();
		
		return treeRoot;
	}
	
	public LinkedBinaryTreeNode<QueryPlanTreeNode> updateTreeWhyIsHere(
			LinkedBinaryTreeNode<QueryPlanTreeNode> completePlanTreeRoot,
			LinkedBinaryTreeNode<QueryPlanTreeNode> planNode, 
			String[] rowData
			) throws SQLException, NonMatchingAttrCountAndValueCountException, BlankAttributesException
	{
		DataPlanConstructor constructor = null;
		try {
			constructor = new DataPlanConstructor(completePlanTreeRoot, planNode, rowData, pdbConnector);
		} catch (rowDataAndAttributeMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(constructor != null)
		{
			LinkedBinaryTreeNode<QueryPlanTreeNode> root = null;
			root = constructor.build();
			return root;
		}
		else
		{
			return null;
		}
	}
	
	public LinkedBinaryTreeNode<QueryPlanTreeNode> updateTreeWhyNotHere(
			LinkedBinaryTreeNode<QueryPlanTreeNode> completePlanTreeRoot,
			LinkedBinaryTreeNode<QueryPlanTreeNode> planNode, 
			String[] rowData
			) throws SQLException, NonMatchingAttrCountAndValueCountException, BlankAttributesException
	{
		DataPlanConstructor constructor = null;
		try {
			constructor = new DataPlanConstructor(completePlanTreeRoot, planNode, rowData, pdbConnector);
		} catch (rowDataAndAttributeMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(constructor != null)
		{
			LinkedBinaryTreeNode<QueryPlanTreeNode> root = constructor.build();
			return root;
		}
		else
		{
			return null;
		}
	}
	
	/*
	 * Input: The table name a user wants to retrieve data from.
	 *        This table needs to be one of the tables that appears in one 
	 *        of the nodes returned in JSONObject from debugQuery()
	 * Output: Sample data. See executeTestQuery() for detail.
	 */
	public Pair getSampleData(String tableName)
	{
		try {
			return executeQuerySeparateResultAddQuotes("SELECT * FROM " + tableName + " LIMIT 20", 20);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public Pair getAllSampleData(String tableName)
	{
		try {
			return executeQuerySeparateResultAddQuotes("SELECT * FROM " + tableName, Integer.MAX_VALUE);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
	public Pair executeTestQuery(String query) throws SQLException
	{
		return executeQuerySeparateResultAddQuotes(fixTableName(query), 20);
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
	public Pair executeTestQueryAll(String query) throws SQLException
	{
		return executeQuerySeparateResultAddQuotes(fixTableName(query), Integer.MAX_VALUE);
	}
	
	private String fixTableName(String inQuery)
	{
		for(String key : abbToFullTmpTableMap.keySet())
		{
			if(inQuery.contains(key))
			{
				inQuery = inQuery.replaceAll(key, abbToFullTmpTableMap.get(key));
			}
		}
		return inQuery;
	}
		
	private Pair executeQuerySeparateResultAddQuotes(String query, int LIMIT) throws SQLException 
	{
		Pair result = pdbConnector.executeQuerySeparateResult(query, LIMIT);
		if((LIMIT != Integer.MAX_VALUE) && (result.data != null) && (result.data.size() > 0))
		{
			int numCol = result.data.get(0).length;
			result.data.add(makeExtraDataReminderString(numCol));
		}
		
		return result;
	}
	
	private String[] makeExtraDataReminderString(int colNum)
	{
		String[] rst = new String[colNum];
		for(int i = 0; i < colNum; i++)
		{
			rst[i] = "...";
		}
		return rst;
	}
	
	public String executeQuery(String query) throws SQLException
	{
		List<String> result = result = pdbConnector.executeQuery(query);
		String resultStr = result.toString();
		
		// use substring to remove the extra pair of [] added by List.toString()
		return resultStr.substring(1, resultStr.length()-1);
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
