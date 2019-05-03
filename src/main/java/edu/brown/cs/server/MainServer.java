package edu.brown.cs.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import edu.brown.cs.database.PongDatabase;
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
  private final Map<String, BRServer> clientToServer;
  private static final Gson GSON = new Gson();
  private final Map<String, Session> sessions;
  private final PongDatabase db;

  public MainServer(PongDatabase db) {
    sessions = new ConcurrentHashMap<>();
    clients = new ConcurrentSkipListSet<>();
    clientToServer = new ConcurrentHashMap<>();
    servers = new CopyOnWriteArrayList<>();
    this.db = db;
  }

  public void addClient(String id, Session session) {
    println("Adding client " + id);
    synchronized(clientToServer) {
      sessions.put(id, session);
      clients.add(id);
      BRServer openServer = null;
      for (BRServer server : servers) {
        if (!server.ready()) {
          openServer = server;
          break;
        }
      }
      if (openServer == null) {
        println("Made new server to accomodate client " + id);
        openServer = new BRServer(db);
        servers.add(openServer); // XXX actual MM
      }
      openServer.addClient(id, session);
      clientToServer.put(id, openServer);
    }
  }

  public void removeSession(Session session) {
    synchronized (clientToServer) {
      for(Map.Entry<String, Session> entry : sessions.entrySet()) {
        if (session.equals(entry.getValue())) {
          String id = entry.getKey();
          println("Disconnect: found ID =." + id);
          if (clientToServer.containsKey(id)) {
            clientToServer.get(id).removeClient(id);
            clientToServer.remove(id);
          } else {
            println("No server contains ID.");
          }
          sessions.remove(id);
        }
      }
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

  @Override
  public void println(String msg) {
    System.out.println("Main :: " + msg);
  }
}
