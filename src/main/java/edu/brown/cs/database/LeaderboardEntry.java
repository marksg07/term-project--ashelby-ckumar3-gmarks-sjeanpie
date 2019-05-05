package edu.brown.cs.database;

import java.util.Objects;

/**
 * A class to store data for a leaderboard entry.
 */
public class LeaderboardEntry {
  private String usr;
  private int totalGames, wins;
  private double elo;

  /**
   * A constructor for a LeaderboardEntry.
   * @param u username
   * @param t total games
   * @param w wins
   * @param e elo
   */
  public LeaderboardEntry(String u, int t, int w, double e) {
    usr = u;
    totalGames = t;
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
    return totalGames;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LeaderboardEntry that = (LeaderboardEntry) o;
    return totalGames == that.totalGames &&
            wins == that.wins &&
            Double.compare(that.elo, elo) == 0 &&
            usr.equals(that.usr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(usr, totalGames, wins, elo);
  }

  @Override
  public String toString() {
    return "LeaderboardEntry{" +
            "usr='" + usr + '\'' +
            ", totalGames=" + totalGames +
            ", wins=" + wins +
            ", elo=" + elo +
            '}';
  }
}
