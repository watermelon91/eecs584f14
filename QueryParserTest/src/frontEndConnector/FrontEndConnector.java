package frontEndConnector;

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
	
	public String initializeSQLConnection()
	{
		return pdbConnector.initalizeConnector(dbIP, dbName, userName, password);
	}
	
	public String closeDBConnection()
	{
		return pdbConnector.closeConnector();
	}
}
