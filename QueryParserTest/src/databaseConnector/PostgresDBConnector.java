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
	public class InputQueryNotSELECTALL extends Exception{}
	public class QueryAttrNumNotMatch extends Exception{}
	
	public class Pair{
		public String[] attributes;
		public List<String[]> data;
		
		public Pair(String[] _attr, List<String[]> _data){
			attributes = _attr;
			data = _data;
		}
	}
	
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
	
	private boolean needsQuote(String attrType)
	{
		if(attrType.equals("bigint") ||
				attrType.equals( "bigserial") ||
				attrType.equals( "boolean") ||
				attrType.equals( "double precision") ||
				attrType.equals( "integer") ||
				attrType.equals( "numeric") ||
				attrType.equals( "real") ||
				attrType.equals( "smallint") ||
				attrType.equals( "smallserial") ||
				attrType.equals( "serial") ||
				attrType.equals( "int8") ||
				attrType.equals( "serial8") ||
				attrType.equals( "bool") ||
				attrType.equals( "float8") ||
				attrType.equals( "int") ||
				attrType.equals( "int4") ||
				attrType.equals( "decimal") ||
				attrType.equals( "float4") ||
				attrType.equals( "int2") ||
				attrType.equals( "serial2") ||
				attrType.equals( "serial4"))
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public Pair executeQuerySeparateResult(String query, int LIMIT) throws SQLException
	{
		Statement stmt = null;
		ResultSet rst = null;
	
		stmt = connection.createStatement();
		rst = stmt.executeQuery(query);
		ResultSetMetaData metadata = rst.getMetaData();
		
		int numCol = metadata.getColumnCount();
		List<String[]> queryResult = new ArrayList<String[]>(); 
		String[] attrTypes = new String[numCol];
		String[] attrNames = new String[numCol];
		
		for(int i = 1; i <= numCol; i++)
		{
			attrTypes[i-1] = metadata.getColumnTypeName(i);
			attrNames[i-1] = metadata.getColumnLabel(i);
		}
		
		int count = 0;
		while(rst.next() && count < LIMIT)
		{
			String[] curRow = new String[numCol];
			for(int idx = 1; idx <= numCol; idx++)
			{
				curRow[idx-1] = rst.getString(idx);

				if(needsQuote(attrTypes[idx-1]))
				{
					curRow[idx-1] = "'" + curRow[idx-1] + "'";
				}
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
		
		return new Pair(attrNames, queryResult);
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
