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
  private final Set<String> clients;
  private final List<BRServer> servers;
  private final Map<String, Server> clientToServer;
  private static final Gson GSON = new Gson();
  private final Map<String, Session> sessions;

  public MainServer() {
    sessions = new ConcurrentHashMap<>();
    clients = new ConcurrentSkipListSet<>();
    clientToServer = new ConcurrentHashMap<>();
    servers = new CopyOnWriteArrayList<>();
  }

  public void addClient(String id, Session session) {
    sessions.put(id, session);
    clients.add(id);
    synchronized(servers) {
      BRServer openServer = null;
      for (BRServer server : servers) {
        if (!server.ready()) {
          openServer = server;
          break;
        }
      }
      if (openServer == null) {
        openServer = new BRServer();
        servers.add(openServer); // XXX actual MM
      }
      openServer.addClient(id, session);
      clientToServer.put(id, openServer);
    }
  }

  @Override
  public void update(String id, Object obj) {
    synchronized (clientToServer) {
      if (clientToServer.containsKey(id)) {
        clientToServer.get(id).update(id, obj);
      }
    }
  }

  @Override
  public JsonObject getGameState(String id) {
    synchronized (clientToServer) {
      if (clientToServer.containsKey(id)) {
        return clientToServer.get(id).getGameState(id);
      }
      return null;
    }
  }
}
