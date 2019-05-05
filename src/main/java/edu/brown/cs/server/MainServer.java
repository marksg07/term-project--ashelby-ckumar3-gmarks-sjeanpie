package edu.brown.cs.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.brown.cs.database.PongDatabase;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Representation of a main server which can spawn new BR lobbies (i.e. BR servers)
 * to handle joining users. All requests to update a user's input and for game state go
 * through this class first.
 */
@WebSocket
public class MainServer implements Server {
  private final List<BRServer> servers;
  private final Map<String, BRServer> clientToServer;
  private final Map<String, Session> sessions;
  private final PongDatabase db;
  private final Map<String, String> unsToUuids;

  /**
   * Constructor for MainServer.
   * @param db Pong database to write and read.
   */
  public MainServer(PongDatabase db) {
    sessions = new ConcurrentHashMap<>();
    clientToServer = new ConcurrentHashMap<>();
    servers = new CopyOnWriteArrayList<>();
    unsToUuids = new ConcurrentHashMap<>();
    this.db = db;
  }

  /**
   * Add a new client to this server.
   * @param id client ID (username)
   * @param session WebSockets session object
   * @return If the adding was successful; only returns false when user already in server.
   */
  public boolean addClient(String id, Session session) {
    println("Adding client " + id);
    synchronized(clientToServer) {
      if(clientToServer.containsKey(id)) {
        return false;
      }
      sessions.put(id, session);
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
        servers.add(openServer);
      }
      openServer.addClient(id, session);
      clientToServer.put(id, openServer);
    }
    return true;
  }

  /**
   * Remove a client from this server, keyed by session.
   * @param session The session of the client to remove.
   */
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

  public PongDatabase getDatabase() {
    return db;
  }

  String getUUID(String name) {
    return unsToUuids.get(name);
  }

  public void putUUID(String name, String uuid) {
    unsToUuids.put(name, uuid);
  }

  public boolean hasName(String name) {
    return unsToUuids.containsKey(name);
  }
}
