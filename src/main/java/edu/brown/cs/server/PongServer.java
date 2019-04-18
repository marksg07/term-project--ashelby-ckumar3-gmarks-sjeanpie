package edu.brown.cs.server;

import com.google.gson.JsonObject;
import edu.brown.cs.pong.PongGame;

public class PongServer implements Server {
  private int p1Id, p2Id;
  private PongGame game;
  // XXX websocket stuff


  public PongServer(int p1, int p2) {
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
}
