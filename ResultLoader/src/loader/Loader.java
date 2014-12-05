package loader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Loader {

	Connection connection = null;
	
	public Loader()
	{
		String status = initalizeConnector("eecs484.eecs.umich.edu", "yjtang", "yjtang", "admin484postgres");
		if(status.equals(""))
		{
			System.out.println("OK");
		}
		else
		{
			System.out.println(status);
		}
		
		
		
		closeConnector();
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
