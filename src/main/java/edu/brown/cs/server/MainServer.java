package edu.brown.cs.server;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

public class MainServer implements Server {
  List<Integer> clients;
  Map<Integer, Server> clientToServer;

  public MainServer() {

  }

  @Override
  public void run() {

  }

  @Override
  public void receiveMessage(JsonObject obj) {
    // XXX add clients, do matchmaking and figure out when to spawn a new
    // BRServer
    // XXX also should handle passing obj to correct BRServer if exists
  }
}
