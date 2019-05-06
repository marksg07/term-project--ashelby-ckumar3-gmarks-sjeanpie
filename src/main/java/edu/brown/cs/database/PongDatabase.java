package edu.brown.cs.database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Manages user login, updating player information, and
 * handling user accounts.
 */
public class PongDatabase {
  private static final Integer RADIX_CONSANT = 16;
  private static final Integer SALT_CONSTANT_FIRST = 0xff;
  private static final Integer SALT_CONSTANT_SECOND = 0x100;
  private Connection conn;
  private String path;
  private static final Random RANDOM = new SecureRandom();


  /**
   * Constructor for PongDatabase.
   *
   * @param dbPath the path to the database
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

  /**
   * Establishes connection to sqldatabase.
   *
   * @param dbPath the path to the database
   * @return 1 for good status
   * @throws ClassNotFoundException if sqlite class not found
   * @throws SQLException           if sql fails
   */
  public int establishConnection(String dbPath)
          throws ClassNotFoundException, SQLException {
    Class.forName("org.sqlite.JDBC");
    String url = "jdbc:sqlite:" + dbPath;
    conn = DriverManager.getConnection(url);
    Statement stat = conn.createStatement();
    stat.executeUpdate("PRAGMA foreign_keys = ON;");
    stat.close();
    dbPath = path;
    setupTables();
    return 1;
  }

  /**
   * Sets up initial tables.
   *
   * @return 1 for good status, 0 otherwise
   */
  public int setupTables() {
    try (PreparedStatement prep = conn.prepareStatement(
            "CREATE TABLE IF NOT EXISTS usr_stats"
                    + "("
                    + "usr TEXT,"
                    + "total_games INTEGER,"
                    + "elo REAL,"
                    + "wins INTEGER"
                    + ")")) {
      prep.executeUpdate();
    } catch (Exception e) {
      System.out.println("Failed to create table:");
      e.printStackTrace();
      return 0;
    }
    try (PreparedStatement prep = conn.prepareStatement(
            "CREATE TABLE IF NOT EXISTS usr_pass"
                    + "("
                    + "usr TEXT,"
                    + "pass TEXT,"
                    + "salt TEXT"
                    + ")")) {
      prep.executeUpdate();
    } catch (Exception e) {
      System.out.println("Failed to create table:");
      e.printStackTrace();
      return 0;
    }
    return 1;
  }

  /**
   * Validates user from username.
   *
   * @param username username of potential user
   * @return true if user exists, false otherwise
   * @throws SQLException if sql fails
   */
  public Boolean validateUser(String username) throws SQLException {

    try (PreparedStatement prep = conn
            .prepareStatement("SELECT usr FROM usr_pass "
                    + "WHERE usr = ?;")) {

      prep.setString(1, username);
      ResultSet rs = prep.executeQuery();

      while (rs.next()) {
        System.out.println(rs.getString(1));
        return true;
      }
    } catch (Exception e) {
      System.out.println("SQL query failed:");
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Validates username-password pair.
   *
   * @param username username of potential user
   * @param password password of potential user
   * @return true if user and password match, false otherwise
   * @throws SQLException if sql fails
   */
  public Boolean validatePassword(String username, String password)
          throws SQLException {
    boolean valid = false;
    try (PreparedStatement prep = conn
            .prepareStatement("SELECT salt, pass FROM usr_pass "
                    + "WHERE usr = ?;")) {

      prep.setString(1, username);
      ResultSet rs = prep.executeQuery();

      while (rs.next()) {
        if (this.hashPassword(password,
                rs.getString(1)).equals(rs.getString(2))) {
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

  /**
   * Stores username and hashed password in database.
   *
   * @param username username of new user
   * @param password password of new user
   * @throws SQLException if sql fails
   */
  public void createAccount(String username, String password)
          throws SQLException {
    try (PreparedStatement prep = conn
            .prepareStatement("INSERT INTO usr_pass "
                    + "(usr, pass, salt) VALUES "
                    + "(?, ?, ?)")) {
      String salt = this.generateSalt();
      prep.setString(1, username);
      prep.setString(2, this.hashPassword(password, salt));
      prep.setString(3, salt);

      prep.executeUpdate();
    } catch (Exception e) {
      System.out.println("SQL query failed:");
      e.printStackTrace();
    }

    try (PreparedStatement prep = conn.prepareStatement(
            "INSERT INTO usr_stats (usr, total_games, elo, wins) "
                    + "VALUES (?, 0, ?, 0);")) {
      prep.setString(1, username);
      prep.setDouble(2, ELOUpdater.ELOFLOOR);
      prep.executeUpdate();
    } catch (Exception e) {
      System.out.println("SQL query failed:");
      e.printStackTrace();
    }
  }

  /**
   * Gets leaderboard data.
   *
   * @return list of LeaderboardEntry objects to show
   */
  public List<LeaderboardEntry> getLeaderboardData() {
    List<LeaderboardEntry> leaderboardData = new ArrayList<>();
    try (PreparedStatement prep = conn.prepareStatement(
            "SELECT usr, total_games, elo, wins from usr_stats "
                    + "ORDER BY elo DESC;")) {
      ResultSet rs = prep.executeQuery();
      int i = 0;
      while (rs.next() && i < 5) {
        leaderboardData.add(new LeaderboardEntry(rs.getString(1),
                rs.getInt(2), rs.getInt(4), rs.getDouble(3)));
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
  
  /**
   * Gets the leaderboard data for a specific user
   * @param usr
   * @return The LeaderboardEntry of the specified user
   */
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

  /**
   * Increments total games for a user.
   *
   * @param usr username
   */
  public void incrementTotalGames(String usr) {
    try (PreparedStatement prep = conn.prepareStatement(
            "UPDATE usr_stats SET total_games = "
                    + "total_games + 1 WHERE usr = ?")) {
      prep.setString(1, usr);
      prep.executeUpdate();
    } catch (Exception e) {
      System.out.println("SQL query failed:");
      e.printStackTrace();
    }
  }

  /**
   * Increments total wins for a user.
   *
   * @param usr username
   */
  public void incrementWins(String usr) {
    try (PreparedStatement prep = conn.prepareStatement(
            "UPDATE usr_stats SET wins = wins + 1 WHERE usr = ?")) {
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


  /**
   * Hashes the password, implementation found online.
   * Please note that https://stackoverflow.com/questions/33085493/
   * how-to-hash-a-password-with-sha-512-in-java is the source.
   *
   * @param password the password
   * @param salt     the salt base
   * @return the hashed password
   */
  public String hashPassword(String password, String salt) {
    String generatedPassword = null;
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-512");
      md.update(salt.getBytes(StandardCharsets.UTF_8));
      byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < bytes.length; i++) {
        sb.append(Integer.toString((bytes[i] & SALT_CONSTANT_FIRST)
                + SALT_CONSTANT_SECOND, RADIX_CONSANT).substring(1));
      }
      generatedPassword = sb.toString();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return generatedPassword;
  }

  /**
   * Generates a salt string for encryption.
   *
   * @return the string for salt
   */
  public String generateSalt() {
    byte[] salt = new byte[RADIX_CONSANT];
    RANDOM.nextBytes(salt);
    return new String(salt);
  }

}
