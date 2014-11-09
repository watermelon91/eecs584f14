package databaseConnector;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
		
		// check if connection is successful
		if(connection == null)
		{
			return "SQL connection failed";
		}
		else
		{
			return "";
		}
	}
	
	public void executeNonSelectQuery(String query) throws SQLException
	{
		Statement stmt = null;
		stmt = connection.createStatement();
		stmt.executeUpdate(query);
		
		if(stmt != null)
		{
			stmt.close();
		}
	}
	
	public List<String> executeQuery(String query) throws SQLException
	{
		Statement stmt = null;
		ResultSet rst = null;
	
		stmt = connection.createStatement();
		rst = stmt.executeQuery(query);
		ResultSetMetaData metadata = rst.getMetaData();
		
		int numCol = metadata.getColumnCount();
		List<String> queryResult = new ArrayList<String>(); 
		
		while(rst.next())
		{
			String curRow = "";
			for(int idx = 1; idx <= numCol; idx++)
			{
				curRow = curRow + rst.getString(idx) + "\t";
			}
			queryResult.add(curRow);
		}
		
		if (stmt != null)
		{
			stmt.close();
		}
		if (rst != null)
		{
			rst.close();
		}
		
		return queryResult;
	}
	
	
	public List<String[]> executeQuerySeparateResult(String query) throws SQLException
	{
		Statement stmt = null;
		ResultSet rst = null;
	
		stmt = connection.createStatement();
		rst = stmt.executeQuery(query);
		ResultSetMetaData metadata = rst.getMetaData();
		
		int numCol = metadata.getColumnCount();
		List<String[]> queryResult = new ArrayList<String[]>(); 
		
		while(rst.next())
		{
			String[] curRow = new String[numCol];
			for(int idx = 1; idx <= numCol; idx++)
			{
				curRow[idx-1] = rst.getString(idx);
			}
			queryResult.add(curRow);
		}
		
		if (stmt != null)
		{
			stmt.close();
		}
		if (rst != null)
		{
			rst.close();
		}
		
		return queryResult;
	}
	
	public List<String[]> executeQuerySeparateResult(String query, int LIMIT) throws SQLException
	{
		Statement stmt = null;
		ResultSet rst = null;
	
		stmt = connection.createStatement();
		rst = stmt.executeQuery(query);
		ResultSetMetaData metadata = rst.getMetaData();
		
		int numCol = metadata.getColumnCount();
		List<String[]> queryResult = new ArrayList<String[]>(); 
		
		int count = 0;
		while(rst.next() && count < LIMIT)
		{
			String[] curRow = new String[numCol];
			for(int idx = 1; idx <= numCol; idx++)
			{
				curRow[idx-1] = rst.getString(idx);
			}
			queryResult.add(curRow);
			count++;
		}
		
		if (stmt != null)
		{
			stmt.close();
		}
		if (rst != null)
		{
			rst.close();
		}
		
		return queryResult;
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
