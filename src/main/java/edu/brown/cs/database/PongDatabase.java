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
import java.util.Random;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Manages user login, updating player information, and 
 * handling user accounts
 */

public class PongDatabase {
	private static Connection conn;
	private String path;
	private static PreparedStatement prep;
	private static final Random RANDOM = new SecureRandom();

	
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
	
	public Boolean validateUser(String username) throws SQLException {
		
	    prep = conn
		        .prepareStatement("SELECT * FROM usr_pass "
		            + "WHERE usr = ?;");
	    
	   prep.setString(1, username);
	   ResultSet rs = prep.executeQuery();
	   
	   while (rs.next()) {
		   return true;
	   }
	   return false;
	}

	public Boolean validatePassword(String username, String password) throws SQLException {
		boolean valid = false;
	    prep = conn
		        .prepareStatement("SELECT salt, pass FROM usr_pass "
		            + "WHERE usr = ?;");
	    
	   prep.setString(1, username);
	   ResultSet rs = prep.executeQuery();
	   
	   while (rs.next()) {
		   if (this.hashPassword(password, rs.getString(1)).equals(rs.getString(2))) {
			   valid = true;
		   }
	   }
	   rs.close();
	   
	   return valid;
	}
	
	//password will be hashed in the future
	public void createAccount(String username, String password) throws SQLException {
	    prep = conn
		        .prepareStatement("INSERT INTO usr_pass (usr, pass, salt) VALUES "
		            + "(?, ?, ?);");
	    String salt = this.generateSalt();
		prep.setString(1, username);
		prep.setString(2, this.hashPassword(password, salt));
		prep.setString(3, salt);
		
		prep.addBatch();
		prep.executeBatch();

		prep = conn.prepareStatement("INSERT INTO usr_stats (usr, total_games, elo, winrate) VALUES (?, 0, 0, 0);");
		prep.setString(1, username);
		prep.addBatch();
		prep.executeBatch();
	}

	public List<String> getLeaderboardData() {
		List<String> leaderboardData = new ArrayList<String>();
		try {
			prep = conn.prepareStatement("SELECT usr, total_games, elo, winrate from usr_stats ORDER BY winrate DESC;");
			ResultSet rs = prep.executeQuery();
			int i = 0;
			while (rs.next() && i < 4) {
				leaderboardData.add(rs.getString(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3) + "\t" + rs.getString(4));
				i++;
			}
		} catch (SQLException e) {
			leaderboardData.add("ERROR");
		}
		return leaderboardData;
	}
	
	//password stuff should probably be put into its own class later...like a pass
	//manager or w/e
	//https://stackoverflow.com/questions/33085493/how-to-hash-a-password-with-sha-512-in-java
	public String hashPassword(String password, String salt) {
		String generatedPassword = null;
	    try {
	         MessageDigest md = MessageDigest.getInstance("SHA-512");
	         md.update(salt.getBytes(StandardCharsets.UTF_8));
	         byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
	         StringBuilder sb = new StringBuilder();
	         for(int i=0; i< bytes.length ;i++){
	            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
	         }
	         generatedPassword = sb.toString();
	        } 
	       catch (NoSuchAlgorithmException e){
	        e.printStackTrace();
	       }
	    return generatedPassword;
	}
	
	public String generateSalt() {
		byte[] salt = new byte[16];
		RANDOM.nextBytes(salt);
		return new String(salt);
	}
		
}
