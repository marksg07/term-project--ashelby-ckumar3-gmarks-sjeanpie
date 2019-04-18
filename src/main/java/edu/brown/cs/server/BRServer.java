package edu.brown.cs.server;

import com.google.gson.JsonObject;

import java.util.*;

public class BRServer implements Server {

  List<Integer> clients;

  private class ServerPair {
    public Server right, left;
  }
  Map<Integer, ServerPair> clientToServers;

  public BRServer(Set<Integer> c) {
    clients = new ArrayList<>(c);
    clientToServers = new HashMap<>();
    Collections.shuffle(clients);
    for(Integer cli : clients) {
      clientToServers.put(cli, new ServerPair());
    }
    for(int i = 0; i < clients.size(); i++) {
      int iLeft = (clients.size() + i) % clients.size();
      Integer cliLeft = clients.get(iLeft);
      Integer cli = clients.get(i);
      PongServer serv = new PongServer(cliLeft, cli);
      clientToServers.get(cli).left = serv;
      clientToServers.get(cliLeft).right = serv;
    }
  }

  @Override
  public void run() {

  }

  @Override
  public void receiveMessage(JsonObject obj) {
    // XXX get client id from obj and send obj to correct pongservers
  }
}
