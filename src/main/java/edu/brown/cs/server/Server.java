package edu.brown.cs.server;

import com.google.gson.JsonObject;
import org.eclipse.jetty.websocket.api.Session;

public interface Server {
  void run();
  void receiveMessage(JsonObject obj);
  void update(String id, Object obj);
  JsonObject getGameState(String id);
}
