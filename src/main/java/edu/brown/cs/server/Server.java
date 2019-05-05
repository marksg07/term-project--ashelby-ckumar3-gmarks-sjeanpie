package edu.brown.cs.server;

import com.google.gson.JsonObject;

/**
 * Interface for server which holds some subset of logic for handling
 * pong player clients.
 */
public interface Server {

  /**
   * Updates a player's game.
   * @param id id of player
   * @param obj input data
   */
  void update(String id, Object obj);

  /**
   * Gets game state as a JsonObject.
   * @param id the id of the user
   * @return the gamestate requested
   */
  JsonObject getGameState(String id);

  /**
   * Prints the message to system.
   * @param msg the message to print
   */
  void println(String msg);
}
