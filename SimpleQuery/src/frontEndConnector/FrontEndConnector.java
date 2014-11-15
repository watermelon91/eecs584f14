package frontEndConnector;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import databaseConnector.PostgresDBConnector;
import databaseConnector.PostgresDBConnector.InputQueryNotSELECTALL;
import databaseConnector.PostgresDBConnector.QueryAttrNumNotMatch;

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
	
	public Pair executeQuerySeparateResult(String query, int LIMIT)
	{
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
	
	
	public String closeDBConnection()
	{
		return pdbConnector.closeConnector();
	}
	
	private String[] getReturnedAttr(String query)
	{
		query = query.toLowerCase();
		String attrStr = query.substring(query.indexOf("select") + 6, query.indexOf("from"));
		if(attrStr.contains("*"))
		{
			return null;
		}
		else
		{
			return attrStr.split(",");
		}
	}


}
