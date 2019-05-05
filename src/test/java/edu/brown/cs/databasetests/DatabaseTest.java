package edu.brown.cs.databasetests;

import edu.brown.cs.database.PongDatabase;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertTrue;

public class DatabaseTest {

  @Test
  public void testdb() {
    PongDatabase db = new PongDatabase("data/testDB.sqlite3");
    try {
      assertTrue(db.establishConnection("data/testDB.sqlite3") == 1);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void checkTablesExist() {
    PongDatabase db = new PongDatabase("data/testDB.sqlite3");
    assertTrue(db.setupTables() == 1);
  }

  @Test
  public void checkValidateUser() {
    PongDatabase db = new PongDatabase("data/testDB.sqlite3");
    try {
      assertTrue(db.validateUser("abc"));
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void checkValidatePassword() {
    PongDatabase db = new PongDatabase("data/testDB.sqlite3");
    try {
      assertTrue(db.validatePassword("abc", "abc"));
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void hashDeterminism() {
    PongDatabase db = new PongDatabase("data/testDB.sqlite3");
    String hash = db.generateSalt();
    String hashedPassword = db.hashPassword("hello", hash);
    assertTrue(hashedPassword.equals(db.hashPassword("hello", hash)));

  }
}
