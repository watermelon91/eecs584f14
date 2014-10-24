package databaseConnector;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

public class PostgresDBConnector {

	Connection connection = null;
	
	/*
	 * Example input: IP: 127.0.0.1
	 * 				  dbName: eecs584
	 * 				  userName: postgres
	 * 				  password: dbPassword
	 */
	public PostgresDBConnector(){}
	
	public String initalizeConnector(String dbIP, String dbName, String userName, String password)
	{

		// locate the driver
		try 
		{
			Class.forName("org.postgresql.Driver");
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
			return "postgresql driver not found";
		}
		
		// set up the connection
		try 
		{
			connection = DriverManager.getConnection(
					"jdbc:postgresql://" + dbIP + ":5432/" + dbName, 
					userName,
					password);
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
			return "connection setup failed";
		}
		
		//
		if(connection == null)
		{
			return "SQL connection failed";
		}
		else
		{
			return "";
		}
	}
	
	public String closeConnector()
	{
		try 
		{
			connection.close();
			return "";
		}
		catch (SQLException e) 
		{
			return e.getMessage();
		}
	}
}
