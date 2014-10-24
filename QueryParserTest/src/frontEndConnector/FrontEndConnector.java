package frontEndConnector;

import org.json.simple.JSONObject;

import databaseConnector.PostgresDBConnector;

public class FrontEndConnector {
	
	String dbIP = "";
	String dbName = "";
	String userName = "";
	String password = "";
	PostgresDBConnector pdbConnector = null;
	
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
	public JSONObject debugQuery(String query)
	{
		// TODO
		return null;
	}
	
	/*
	 * Input: the table name a user wants to retrieve data from.
	 *        this table needs to be one of the tables that appears in one 
	 *        of the nodes returned in JSONObject from debugQuery.
	 * Output: sample data
	 */
	public String getSampleData(String tableName)
	{
		// TODO
		return "";
	}
	
	/*
	 * Input: a query that the user wants to run on the test data 
	 * 		  generated using getSampleData()
	 * Output: query result
	 */
	public String executeTestQuery(String query)
	{
		// TODO
		return "";
	}
	
	public String closeDBConnection()
	{
		return pdbConnector.closeConnector();
	}
}
