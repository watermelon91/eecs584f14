package frontEndConnector;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import databaseConnector.PostgresDBConnector;
import databaseConnector.PostgresDBConnector.InputQueryNotSELECTALL;
import databaseConnector.PostgresDBConnector.Pair;
import databaseConnector.PostgresDBConnector.QueryAttrNumNotMatch;

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
	
	public Pair executeQuerySeparateResult(String query, int LIMIT) throws SQLException
	{
		Pair rstPair = pdbConnector.executeQuerySeparateResult(query, LIMIT);		
		return rstPair;
	}
	
	
	public String closeDBConnection()
	{
		return pdbConnector.closeConnector();
	}

}
