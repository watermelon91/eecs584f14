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
	
	public List<String[]> executeQuerySeparateResult(String query, int LIMIT, String tableName) throws SQLException, InputQueryNotSELECTALL, QueryAttrNumNotMatch
	{
		// the input query has to start with "SELECT * FROM", otherwise difficulty to track attr types
		if(!query.startsWith("SELECT * FROM"))
		{
			throw new InputQueryNotSELECTALL();
		}
		
		// get query result
		List<String[]> results = executeQuerySeparateResult(query, LIMIT);
		
		if(results.size() == 0)
		{
			return results;
		}
		
		// get column types of the attributes
		List<String[]> dataTypes = null;
		try 
		{
			dataTypes = executeQuerySeparateResult(" select data_type from information_schema.columns where table_name = '" + tableName + "'", Integer.MAX_VALUE);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		int numColumn = results.get(0).length;
		if(numColumn != dataTypes.size())
		{
			throw new QueryAttrNumNotMatch();
		}
		// flatten column types
		String[] columnTypes = new String[numColumn];
		for(int i = 0; i < dataTypes.size(); i++)
		{
			columnTypes[i] = dataTypes.get(i)[0];
		}
		
		// add '' to non-numeric types
		for(int i = 0; i < results.size(); i++)
		{
			String[] curRow = results.get(i);
			for(int j = 0; j < curRow.length; j++)
			{
				if(needsQuote(columnTypes[j]))
				{
					curRow[j] = "'" + curRow[j] + "'";
				}
			}
			results.set(i, curRow);
		}
		
		return results;
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
