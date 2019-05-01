/**
 * 
 */
package edu.brown.cs.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manages user login, updating player information, and 
 * handling user accounts
 */

public class PongDatabase {
	private static Connection conn;
	private String path;
	private static PreparedStatement prep;
	
	/**
	 * 
	 */
	public PongDatabase(String dbPath) {
		try {
			this.establishConnection(dbPath);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void establishConnection(String dbPath) throws ClassNotFoundException, SQLException {
		//TODO: validate the database and path entered. favor try/catch
		Class.forName("org.sqlite.JDBC");
	      String url = "jdbc:sqlite:" + dbPath;
	      conn = DriverManager.getConnection(url);
	      Statement stat = conn.createStatement();
	        stat.executeUpdate("PRAGMA foreign_keys = ON;");
	      stat.close();
	      dbPath = path;
	}

}
