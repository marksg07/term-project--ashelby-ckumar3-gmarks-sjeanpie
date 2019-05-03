package edu.brown.cs.database;

public class LeaderboardEntry {
  private String usr;
  private int total_games, wins;
  private double elo;

  public LeaderboardEntry(String u, int t, int w, double e) {
    usr = u;
    total_games = t;
    wins = w;
    elo = e;
  }


  /**
   * Gets elo.
   *
   * @return Value of elo.
   */
  public double getElo() {
    return elo;
  }

  /**
   * Gets usr.
   *
   * @return Value of usr.
   */
  public String getUsr() {
    return usr;
  }

  /**
   * Gets wins.
   *
   * @return Value of wins.
   */
  public int getWins() {
    return wins;
  }

  /**
   * Gets total_games.
   *
   * @return Value of total_games.
   */
  public int getTotalGames() {
    return total_games;
  }
}
