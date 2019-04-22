package edu.brown.cs.server;

import com.google.gson.JsonObject;

public interface Server {
  void run();
  void receiveMessage(JsonObject obj);
}
