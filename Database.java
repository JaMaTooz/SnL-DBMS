package matuszak;

import java.sql.*;
import javax.sql.*;
import java.io.File;

public class Database {
	
	// File Path
	private final String URL = "jdbc:h2:file:./ShedsAndLordsDB";
	
	// Driver
    static final String DRIVER = "org.h2.Driver";
	
    // User name and password, required for creating and loading database
    static private final String USERNAME = "Jacob";
    static private final String PASSWORD = "123";
    
    // SQL commands stored in this string before being executed
 	private String sql;

 	// Statement is conn.createStatement() - for executing SQL statements
 	private Statement state;

 	// Connection handles the driver
 	private Connection conn;
 	
 	private ResultSet returnSet;
	
 	// Creates the new database class
	public Database() {
		System.out.print("Connecting to Database");
		try {
			System.out.print(".");
			Class.forName(DRIVER);
			System.out.print(".");
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			System.out.print(".");
			state = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			System.out.println("Connected to Database");
			
			//state.execute(sql);
		} catch (ClassNotFoundException ex) {
			System.out.println(ex);
		} catch (SQLException ex) {
			System.out.println(ex);
		}
	}
	
	public void exSQL(String sql) {
		try {
			state = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			state.execute(sql);
			System.out.println("Statement: " + sql + " Successful");
		} catch (SQLException ex) {
			System.out.println(ex);
		}
	}
	
	public ResultSet query(String sql) {
		try {
			state = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			returnSet = state.executeQuery(sql);
			return returnSet;
		} catch (SQLException ex) {
			System.out.println(ex);
		}
		return returnSet;
	}
	
	public void closeDB() {
		try {
			conn.close();
			System.out.println("Database Closed");
		} catch (SQLException ex) {
			System.out.println(ex);
		}
	}
	
	public boolean isClosed() {
		try {
			return conn.isClosed();
		} catch (SQLException ex) {
			return true;
		}
	}
}
