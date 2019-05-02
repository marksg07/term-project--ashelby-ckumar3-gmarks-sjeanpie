package edu.brown.cs.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.*;

public class BRServer implements Server {
  private static final Gson GSON = new Gson();
  private final List<String> clients;
  private final Map<String, Session> sessions;
  private long myId;
  static final int MAXPLAYERS = 3;

  private static long idCounter = 0;

  public static synchronized long nextId()
  {
    return idCounter++;
  }

  private class ServerPair {
    PongServer right, left;
  }

  private final Map<String, ServerPair> clientToServers;
  private boolean ready;

  public BRServer() {
    clients = new ArrayList<>();
    sessions = new HashMap<>();
    clientToServers = new HashMap<>();
    ready = false;
    myId = nextId();
  }

  public boolean ready() {
    return ready;
  }

  public void addClient(String id, Session session) {
    System.out.println("BR #" + myId + " :: Adding client " + id + ".");
    synchronized (clientToServers) {
      if (ready()) {
        return;
      }
      clients.add(id);
      sessions.put(id, session);
      if (clients.size() == MAXPLAYERS) {
        ready = true;
        onFilled();
      }
    }
  }

  public void onFilled() {
    System.out.println("BR #" + myId + " :: Game filled with " + clients.size() + " players, starting.");
    Collections.shuffle(clients);
    for(String cli : clients) {
      clientToServers.put(cli, new ServerPair());
    }
    for(int i = 0; i < clients.size(); i++) {
      int iLeft = (clients.size() - 1 + i) % clients.size();
      String cliLeft = clients.get(iLeft);
      String cli = clients.get(i);
      PongServer serv = new PongServer(cliLeft, cli);
      clientToServers.get(cli).left = serv;
      clientToServers.get(cliLeft).right = serv;
    }
    // send all clients matchmaking packs?
    // yes

    for(Map.Entry<String, Session> pair : sessions.entrySet()) {
      Session session = pair.getValue();

      JsonObject updateObj = new JsonObject();
      updateObj.add("type", new JsonPrimitive(PongWebSocketHandler.MESSAGE_TYPE.GAMESTART.ordinal()));
      JsonObject payload = new JsonObject();
      updateObj.add("payload", payload);
      try {
        session.getRemote().sendString(GSON.toJson(updateObj));
      } catch (IOException e) {
        System.out.println("Sending game start update failed:");
        e.printStackTrace();
      }
    }


  }

  @Override
  public void update(String id, Object obj) {
    // XXX assert?

    synchronized(clientToServers) {
      if (clientToServers.containsKey(id)) {
        ServerPair sp = clientToServers.get(id);
        if (sp == null) {
          // player dead
          return;
        }
        sp.right.update(id, obj);
        sp.left.update(id, obj);
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
        if (sp.left.getGame().isP1Dead()) {
          leftDeadID = sp.left.getID("1");
        } else if (sp.left.getGame().isP2Dead()) {
          leftDeadID = sp.left.getID("2");
        }

        if (sp.right.getGame().isP1Dead()) {
          rightDeadID = sp.right.getID("1");
        } else if (sp.right.getGame().isP2Dead()) {
          rightDeadID = sp.right.getID("2");
        }

        if (!leftDeadID.equals("")) {
          kill(leftDeadID);
        }

        if (!rightDeadID.equals("")) {
          kill(rightDeadID);
        }

        obj.add("left", sp.left.getGameState(id));
        obj.add("right", sp.right.getGameState(id));
        return obj;
      }
    }
    return new JsonObject();
  }

  private void kill(String playerID) {
    Integer playerIndex = clients.indexOf(playerID);
    if (!playerIndex.equals(-1)) {
      String prevID = clients.get((playerIndex + (clients.size() - 1)) % clients.size());
      String nextID = clients.get((playerIndex + 1) % clients.size());

      Session deadSession = sessions.get(playerID);
      JsonObject msg = new JsonObject();
      msg.addProperty("type", PongWebSocketHandler.MESSAGE_TYPE.PLAYERDEAD.ordinal());
      msg.add("payload", new JsonObject());
      try {
        deadSession.getRemote().sendString(GSON.toJson(msg));
      } catch (Exception e) {
        System.out.println("Failed to send PLAYERDEAD");
      }
      sessions.remove(playerID);

      // make new server for new neighbors
      PongServer newServer = new PongServer(prevID, nextID);
      clientToServers.get(prevID).right = newServer;
      clientToServers.get(nextID).left = newServer;
      clients.remove(playerID);
      clientToServers.put(playerID, null);
    }

  }

}
