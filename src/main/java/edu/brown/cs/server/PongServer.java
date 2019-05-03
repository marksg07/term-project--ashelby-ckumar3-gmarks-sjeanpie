package edu.brown.cs.server;

import com.google.gson.JsonObject;
import edu.brown.cs.pong.PongGame;

public class PongServer implements Server {
  private String p1Id, p2Id;
  private final PongGame game;

  public PongServer(String p1, String p2) {
    p1Id = p1;
    p2Id = p2;
    game = new PongGame(400, 300, 230, 40, 10, 150, 5, false);
  }

  public PongServer(String p1, String p2, double p1PaddleY, double p2PaddleY) {
    p1Id = p1;
    p2Id = p2;
    game = new PongGame(400, 300, 230, 40, 10, 150, 5, true);
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

  public String getID(String pNum) {
    switch (pNum) {
      case "player1":
      case "p1":
      case "1":
        return p1Id;
      case "player2":
      case "p2":
      case "2":
        return p2Id;
      default:
        println("BAD GETID");
        return null;
    }
  }

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
}
