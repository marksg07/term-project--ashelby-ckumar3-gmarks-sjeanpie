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
import java.util.Map;
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
  private Connection conn;
  private String path;
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
    setupTables();
  }

  public void setupTables() {
    try (PreparedStatement prep = conn.prepareStatement("CREATE TABLE IF NOT EXISTS usr_stats" +
            "(" +
            "usr TEXT," +
            "total_games INTEGER," +
            "elo REAL," +
            "wins INTEGER" +
            ")")) {
      prep.execute();
    } catch (Exception e) {
      System.out.println("Failed to create table:");
      e.printStackTrace();
    }
    try (PreparedStatement prep = conn.prepareStatement("CREATE TABLE IF NOT EXISTS usr_pass" +
            "(" +
            "usr TEXT," +
            "pass TEXT," +
            "salt TEXT" +
            ")")) {
      prep.execute();
    } catch (Exception e) {
      System.out.println("Failed to create table:");
      e.printStackTrace();
    }
  }

  public Boolean validateUser(String username) throws SQLException {

    try (PreparedStatement prep = conn
            .prepareStatement("SELECT usr FROM usr_pass "
                    + "WHERE usr = ?;")) {

      prep.setString(1, username);
      ResultSet rs = prep.executeQuery();

      while (rs.next()) {
        return true;
      }
    } catch (Exception e) {
      System.out.println("SQL query failed:");
      e.printStackTrace();
    }
    return false;
  }

  public Boolean validatePassword(String username, String password) throws SQLException {
    boolean valid = false;
    try (PreparedStatement prep = conn
            .prepareStatement("SELECT salt, pass FROM usr_pass "
                    + "WHERE usr = ?;")) {

      prep.setString(1, username);
      ResultSet rs = prep.executeQuery();

      while (rs.next()) {
        if (this.hashPassword(password, rs.getString(1)).equals(rs.getString(2))) {
          valid = true;
        }
      }
      rs.close();
    } catch (Exception e) {
      System.out.println("SQL query failed:");
      e.printStackTrace();
    }

    return valid;
  }

  public Boolean validateHash(String username, String hash) {
    try (PreparedStatement prep = conn.prepareStatement("SELECT usr FROM usr_pass WHERE usr = ? AND pass = ?")) {
      prep.setString(1, username);
      prep.setString(2, hash);
      ResultSet rs = prep.executeQuery();

      while (rs.next()) {
        return true; // it matched
      }
      return false;
    } catch (Exception e) {
      System.out.println("SQL query failed:");
      e.printStackTrace();
      return null;
    }
  }

  //password will be hashed in the present
  public void createAccount(String username, String password) throws SQLException {
    try (PreparedStatement prep = conn
            .prepareStatement("INSERT INTO usr_pass (usr, pass, salt) VALUES "
                    + "(?, ?, ?)")) {
      String salt = this.generateSalt();
      prep.setString(1, username);
      prep.setString(2, this.hashPassword(password, salt));
      prep.setString(3, salt);

      prep.addBatch();
      prep.executeBatch();
    } catch (Exception e) {
      System.out.println("SQL query failed:");
      e.printStackTrace();
    }

    try (PreparedStatement prep = conn.prepareStatement(
            "INSERT INTO usr_stats (usr, total_games, elo, wins) VALUES (?, 0, ?, 0);")) {
      prep.setString(1, username);
      prep.setDouble(3, ELOUpdater.BASE_ELO);
      prep.addBatch();
      prep.executeBatch();
    } catch (Exception e) {
      System.out.println("SQL query failed:");
      e.printStackTrace();
    }
  }

  public List<LeaderboardEntry> getLeaderboardData() {
    List<LeaderboardEntry> leaderboardData = new ArrayList<>();
    try(PreparedStatement prep = conn.prepareStatement(
            "SELECT usr, total_games, elo, wins from usr_stats ORDER BY wins * 1.0 / total_games DESC;")) {
      ResultSet rs = prep.executeQuery();
      int i = 0;
      while (rs.next() && i < 5) {
        leaderboardData.add(new LeaderboardEntry(rs.getString(1), rs.getInt(2), rs.getInt(4), rs.getDouble(3)));
        i++;
      }
    } catch (SQLException e) {
      return null;
    }
    while (leaderboardData.size() < 5) {
      leaderboardData.add(null);
    }
    return leaderboardData;
  }
  
  public LeaderboardEntry getLeaderboardEntry(String usr) {
	  LeaderboardEntry entry = null;
	  try(PreparedStatement prep = conn.prepareStatement(
	            "SELECT * from usr_stats WHERE usr = ?;")) {
		  prep.setString(1,  usr);
	      ResultSet rs = prep.executeQuery();
	      while (rs.next()) {
	        entry = new LeaderboardEntry(rs.getString(1), rs.getInt(2), rs.getInt(4), rs.getDouble(3));
	      }
	    } catch (SQLException e) {
	      return null;
	    }
	    return entry;
  }

  public void incrementTotalGames(String usr) {
    try (PreparedStatement prep = conn.prepareStatement(
            "UPDATE usr_stats SET total_games = total_games + 1 WHERE usr = ?")){
      prep.setString(1, usr);
      prep.executeUpdate();
    } catch (Exception e) {
      System.out.println("SQL query failed:");
      e.printStackTrace();
    }
  }

  public void incrementWins(String usr) {
    try (PreparedStatement prep = conn.prepareStatement(
            "UPDATE usr_stats SET wins = wins + 1 WHERE usr = ?")){
      prep.setString(1, usr);
      prep.executeUpdate();
    } catch (Exception e) {
      System.out.println("SQL query failed:");
      e.printStackTrace();
    }
  }
  
  public void updateELOs(Map<String, Double> newElos) {
	    try (PreparedStatement prep = conn.prepareStatement(
	            "UPDATE usr_stats SET elo = ? WHERE usr = ?")){
	      for (String usr : newElos.keySet()) {
	    	  prep.setDouble(1, newElos.get(usr));
	    	  prep.setString(2, usr);
	    	  prep.addBatch();
	      }
	      prep.executeBatch();
	    } catch (Exception e) {
	      System.out.println("SQL query failed:");
	      e.printStackTrace();
	    }
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
      for (int i = 0; i < bytes.length; i++) {
        sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
      }
      generatedPassword = sb.toString();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return generatedPassword;
  }

  public String getHash(String usr) {
    try (PreparedStatement prep = conn.prepareStatement("SELECT pass FROM usr_pass WHERE usr = ?")) {
      prep.setString(1, usr);
      ResultSet rs = prep.executeQuery();
      while (rs.next()) {
        return rs.getString(1);
      }
      return null;
    } catch (SQLException e) {
      System.out.println("SQL query failed:");
      e.printStackTrace();
      return null;
    }
  }

  public String generateSalt() {
    byte[] salt = new byte[16];
    RANDOM.nextBytes(salt);
    return new String(salt);
  }

}
