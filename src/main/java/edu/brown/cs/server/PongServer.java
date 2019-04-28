package edu.brown.cs.server;

import com.google.gson.JsonObject;
import edu.brown.cs.pong.PongGame;

public class PongServer implements Server {
  private String p1Id, p2Id;
  private PongGame game;
  // XXX websocket stuff


  public PongServer(String p1, String p2) {
    p1Id = p1;
    p2Id = p2;
    game = new PongGame(1, 1, 0.4, 0.15, 0.03, 0.3);
  }

  @Override
  public void run() {

  }

  @Override
  public void receiveMessage(JsonObject obj) {
    // XXX Calculate time diff from this message to last update
    // XXX Lagcomp
    // XXX set PongGame inputs correctly
  }

  @Override
  public void update(String id, Object obj) {
    int input = (Integer) obj;
    if (id.equals(p1Id)) {
      game.setP1Input(PongGame.InputType.fromInt(input));
    } else {
      assert(id.equals(p2Id));
      game.setP2Input(PongGame.InputType.fromInt(input));
    }
  }

  @Override
  public JsonObject getGameState(String id) {
    assert (p1Id.equals(id) || p2Id.equals(id));
    return game.getState();
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
        System.out.println("BAD GETID");
        return "BAD ARG";
    }
  }
}
