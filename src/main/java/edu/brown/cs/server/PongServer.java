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
    game = new PongGame(400, 300, 150, 40, 10, 300);
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
    game.tickToCurrent();
  }

  @Override
  public JsonObject getGameState(String id) {
    assert (p1Id.equals(id) || p2Id.equals(id));
    return game.getState();
  }
}
