package edu.brown.cs.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import edu.brown.cs.database.PongDatabase;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class BRServer implements Server {
  private static final Gson GSON = new Gson();
  private final List<String> clients;
  private final Map<String, Session> sessions;
  private long myId;
  private final PongDatabase db;
  static final int MAXPLAYERS = 3;

  private static long idCounter = 0;

  public static synchronized long nextId() {
    return idCounter++;
  }

  private class ServerPair {
    PongServer right, left;
  }

  private final Map<String, ServerPair> clientToServers;
  private boolean ready;

  public BRServer(PongDatabase db) {
    clients = new CopyOnWriteArrayList<>();
    sessions = new ConcurrentHashMap<>();
    clientToServers = new ConcurrentHashMap<>();
    ready = false;
    myId = nextId();
    this.db = db;
  }

  public boolean ready() {
    return ready;
  }

  public void addClient(String id, Session session) {
    println("Adding client " + id + ".");
    synchronized (clientToServers) {
      if (ready()) {
        return;
      }
      clients.add(id);
      sessions.put(id, session);
      println(clients.size() + " players total.");
      if (clients.size() == MAXPLAYERS) {
        ready = true;
        onFilled();
      }
    }
  }

  private void onFilled() {
    println("Game filled with " + clients.size() + " players, starting.");
    Collections.shuffle(clients);
    for (String cli : clients) {
      clientToServers.put(cli, new ServerPair());
    }
    for (int i = 0; i < clients.size(); i++) {
      int iLeft = (clients.size() - 1 + i) % clients.size();
      String cliLeft = clients.get(iLeft);
      String cli = clients.get(i);
      PongServer serv = new PongServer(cliLeft, cli);
      clientToServers.get(cli).left = serv;
      clientToServers.get(cliLeft).right = serv;
    }
    // send all clients matchmaking packs?
    // yes

    for (Map.Entry<String, Session> pair : sessions.entrySet()) {
      String id = pair.getKey();
      synchronized (db) {
        db.incrementTotalGames(id);
      }

      Session session = pair.getValue();
      JsonObject updateObj = new JsonObject();
      updateObj.add("type", new JsonPrimitive(PongWebSocketHandler.MESSAGE_TYPE.GAMESTART.ordinal()));
      JsonObject payload = new JsonObject();
      updateObj.add("payload", payload);
      try {
        session.getRemote().sendString(GSON.toJson(updateObj));
      } catch (IOException e) {
        println("Sending game start update failed:");
        e.printStackTrace();
      }
    }


  }

  @Override
  public void update(String id, Object obj) {
    // XXX assert?

    synchronized (clientToServers) {
      if (clientToServers.containsKey(id)) {
        ServerPair sp = clientToServers.get(id);
        if (sp == null) {
          // player dead
          return;
        }
        if (sp.right != null) {
          sp.right.update(id, obj);
        }
        if (sp.left != null) {
          sp.left.update(id, obj);
        }
      }
    }
  }

  @Override
  public JsonObject getGameState(String id) {
    synchronized (clientToServers) {
      if (clientToServers.containsKey(id)) {
        ServerPair sp = clientToServers.get(id);
        if (sp == null) {
          // player dead
          return new JsonObject();
        }
        JsonObject obj = new JsonObject();

        String leftDeadID = "";
        String rightDeadID = "";

        if (sp.left != null) {
          if (sp.left.getGame().isP1Dead()) {
            leftDeadID = sp.left.getID("1");
          } else if (sp.left.getGame().isP2Dead()) {
            leftDeadID = sp.left.getID("2");
          }
        }

        if (sp.right != null) {
          if (sp.right.getGame().isP1Dead()) {
            rightDeadID = sp.right.getID("1");
          } else if (sp.right.getGame().isP2Dead()) {
            rightDeadID = sp.right.getID("2");
          }
        }

        if (!leftDeadID.equals("")) {
          kill(leftDeadID);
        }

        if (!rightDeadID.equals("")) {
          kill(rightDeadID);
        }

        if (sp.left != null) {
          obj.add("left", sp.left.getGameState(id));
        } else {
          obj.addProperty("left", "dead");
        }

        if (sp.right != null) {
          obj.add("right", sp.right.getGameState(id));
        } else {
          obj.addProperty("right", "dead");
        }
        return obj;
      }
    }
    return new JsonObject();
  }

  private void kill(String playerID) {
    println("Killing player " + playerID);
    if(clients.size() == 1) {
      // don't "kill" last client, just leave it as every connection should be closed
      return;
    }
    Integer playerIndex = clients.indexOf(playerID);
    if (!playerIndex.equals(-1)) {
      String prevID = clients.get((playerIndex + (clients.size() - 1)) % clients.size());
      String nextID = clients.get((playerIndex + 1) % clients.size());

      Session deadSession = sessions.get(playerID);
      JsonObject deadMsg = new JsonObject();
      deadMsg.addProperty("type", PongWebSocketHandler.MESSAGE_TYPE.PLAYERDEAD.ordinal());
      deadMsg.add("payload", new JsonObject());
      try {
        deadSession.getRemote().sendString(GSON.toJson(deadMsg));
      } catch (Exception e) {
        println("Failed to send PLAYERDEAD");
      }
      sessions.remove(playerID);
      clients.remove(playerID);
      // make new server for new neighbors
      if (clients.size() > 2) {
        PongServer newServer = new PongServer(prevID, nextID);
        clientToServers.get(prevID).right = newServer;
        clientToServers.get(nextID).left = newServer;
      } else if (clients.size() == 2) {
        clientToServers.get(prevID).right = null;
        clientToServers.get(nextID).left = null;
      } else {
        assert (clients.size() == 1);
        synchronized (db) {
          db.incrementWins(clients.get(0));
        }
        Session winSession = sessions.get(clients.get(0));
        JsonObject winMsg = new JsonObject();
        winMsg.addProperty("type", PongWebSocketHandler.MESSAGE_TYPE.PLAYERWIN.ordinal());
        winMsg.add("payload", new JsonObject());
        try {
          winSession.getRemote().sendString(GSON.toJson(winMsg));
        } catch (Exception e) {
          println("Failed to send PLAYERWIN");
        }
      }

      // the br server has to know the client used to exist
      clientToServers.put(playerID, null);
    } else {
      println("Client DNE " + playerID + ".");
    }

  }

  public void removeClient(String id) {
    synchronized (clientToServers) {
      kill(id);
    }
  }

  @Override
  public void println(String msg) {
    System.out.println("BR #" + myId + " :: " + msg);
  }

}
