package edu.brown.cs.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

@WebSocket
public class MainServer implements Server {
  Set<String> clients;
  List<BRServer> servers;
  Map<String, Server> clientToServer;
  private static final Gson GSON = new Gson();
  private Map<String, Session> sessions;

  public MainServer() {
    sessions = new ConcurrentHashMap<>();
    clients = new ConcurrentSkipListSet<>();
    clientToServer = new ConcurrentHashMap<>();
    servers = new CopyOnWriteArrayList<>();
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

  public void addClient(String id, Session session) {
    System.out.println("yeet");
    sessions.put(id, session);
    clients.add(id);
    if (servers.size() == 0) {
      servers.add(new BRServer()); // XXX actual MM
    }
    servers.get(0).addClient(id, session);
    clientToServer.put(id, servers.get(0));
    System.out.println("yote");
    /*matchmake(id);


    */
  }

  @Override
  public void update(String id, Object obj) {
    if (clientToServer.containsKey(id)) {
      clientToServer.get(id).update(id, obj);
    }
  }

  /*public List<Integer> matchmake(String id) {
    if (servers.size() == 0) {
      servers.add(new BRServer());
    }
    servers.get(0).addClient(id, session);
    if (servers.get(0).ready()) {

    }
      List<Integer> serverIDs =

    return Arrays.asList(0, 0);
  }*/

  @Override
  public JsonObject getGameState(String id) {
    if (clientToServer.containsKey(id)) {
      return clientToServer.get(id).getGameState(id);
    }
    return null;
  }
}
