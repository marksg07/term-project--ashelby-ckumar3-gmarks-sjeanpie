package edu.brown.cs.server;

import com.google.gson.JsonObject;
import edu.brown.cs.pong.PongGame;

/**
 * Server between two players playing one game.
 */
public class PongServer implements Server {
  private final PongGame game;
  private String p1Id, p2Id;

  /**
   * Normal constructor on BR game start.
   * @param p1 P1 ID
   * @param p2 P2 ID
   * @param startVel Start velocity of ball
   */
  public PongServer(String p1, String p2, double startVel) {
    p1Id = p1;
    p2Id = p2;
    game = new PongGame(400, 300, 400, 40, 10, startVel, 5, false);
  }

  /**
   * Constructor on new game start while BR game is running; must sync paddles correctly.
   * @param p1 P1 ID
   * @param p2 P2 ID
   * @param p1PaddleY P1 paddle position
   * @param p2PaddleY P2 paddle position
   * @param startVel Start velocity of ball
   */
  public PongServer(String p1, String p2, double p1PaddleY, double p2PaddleY, double startVel) {
    p1Id = p1;
    p2Id = p2;
    game = new PongGame(400, 300, 400, 40, 10, startVel, 5, true);
    game.setP1PaddleY(p1PaddleY);
    game.setP2PaddleY(p2PaddleY);
  }

  @Override
  public void update(String id, Object obj) {
    synchronized (game) {
      int input = (Integer) obj;
      if (id.equals(p1Id)) {
        game.setP1Input(PongGame.InputType.fromInt(input));
      } else {
        assert (id.equals(p2Id));
        game.setP2Input(PongGame.InputType.fromInt(input));
      }
      game.tickToCurrent();
    }
  }

  @Override
  public JsonObject getGameState(String id) {
    assert (p1Id.equals(id) || p2Id.equals(id));
    synchronized (game) {
      return game.getState();
    }
  }

  /**
   * Return "flipped" game state; i.e. the mirrored state for 1v1s.
   * @param id Client's ID (name).
   * @return State
   */
  public JsonObject getFlippedGameState(String id) {
    assert (p1Id.equals(id) || p2Id.equals(id));
    synchronized (game) {
      return game.getFlippedState();
    }
  }

  /**
   * Get the ID of a given player (by number).
   * @param pNum number of player
   * @return ID
   */
  public String getID(int pNum) {
    if (pNum == 1) {
      return p1Id;
    } else {
      assert (pNum == 2);
      return p2Id;
    }
  }
    // adam wrote this
//    switch (pNum) {
//      case "player1":
//      case "p1":
//      case "1":
//        return p1Id;
//      case "player2":
//      case "p2":
//      case "2":
//        return p2Id;
//      default:
//        println("BAD GETID");
//        return null;
//    }

  public PongGame getGame() {
    return game;
  }

  @Override
  public void println(String msg) {
    System.out.println("Pong (\"" + p1Id + "\" vs \"" + p2Id + "\") :: " + msg);
  }

  public double getP1PaddleY() {
    return game.getP1PaddleY();
  }

  public double getP2PaddleY() {
    return game.getP2PaddleY();
  }

  public void setBallSpeed(double speed) {
    game.setBallSpeed(speed);
  }
}
