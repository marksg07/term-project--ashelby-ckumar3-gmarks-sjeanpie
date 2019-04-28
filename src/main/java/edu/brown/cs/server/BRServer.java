package edu.brown.cs.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.*;

public class BRServer implements Server {
  private static final Gson GSON = new Gson();
  private List<String> clients;
  private Map<String, Session> sessions;

  private class ServerPair {
    PongServer right, left;
  }

  private Map<String, ServerPair> clientToServers;
  private boolean ready;

  public BRServer() {
    List<String> clients = new ArrayList<>();
    clientToServers = new HashMap<>();
    ready = false;
  }

  public boolean ready() {
    return ready;
  }

  public void addClient(String id, Session session) {
    if (ready()) {
      return;
    }
    clients.add(id);
    sessions.put(id, session);
    if (clients.size() == 2) {
      ready = true;
      onFilled();
    }
  }

  public void onFilled() {
    Collections.shuffle(clients);
    for(String cli : clients) {
      clientToServers.put(cli, new ServerPair());
    }
    for(int i = 0; i < clients.size(); i++) {
      int iLeft = (clients.size() + i) % clients.size();
      String cliLeft = clients.get(iLeft);
      String cli = clients.get(i);
      PongServer serv = new PongServer(cliLeft, cli);
      clientToServers.get(cli).left = serv;
      clientToServers.get(cliLeft).right = serv;
    }
    // send all clients matchmaking packs?
    // yes

    for(Map.Entry<String, Session> pair : sessions.entrySet()) {
      String cli = pair.getKey();
      Session session = pair.getValue();
      ServerPair sp = clientToServers.get(cli);

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
  public void run() {

  }

  @Override
  public void update(String id, Object obj) {
    assert (clientToServers.containsKey(id));
    ServerPair sp = clientToServers.get(id);
    sp.right.update(id, obj);
    sp.left.update(id, obj);
  }

  @Override
  public JsonObject getGameState(String id) {
    assert (clientToServers.containsKey(id));
    ServerPair sp = clientToServers.get(id);
    JsonObject obj = new JsonObject();

    String leftDeadID = "";
    String rightDeadID = "";
    if (sp.left.getGameState(id).getBoolean("p1Dead")) {
      leftDeadID = sp.left.getID("1");
    } else if (sp.left.getGameState(id).getBoolean("p2Dead")) {
      leftDeadID = sp.left.getID("2");
    }

    if (sp.right.getGameState(id).getBoolean("p1Dead")) {
      rightDeadID = sp.right.getID("1");
    } else if (sp.right.getGameState(id).getBoolean("p2Dead")) {
      rightDeadID = sp.right.getID("2");
    }

    if (!leftDeadID.equals("")) {
      kill(leftDeadID);
    }

    if (!rightDeadID.equals("")) {
      kill(rightDeadID);
    }

    obj.add("leftState", sp.left.getGameState(id));
    obj.add("rightState", sp.right.getGameState(id));
    return obj;
  }

  @Override
  public void receiveMessage(JsonObject obj) {
    // XXX get client id from obj and send obj to correct pongservers
  }

  private void kill(String playerID) {
    Integer playerIndex = clients.indexOf(playerID);
    if (!playerIndex.equals(-1)) {
      String prevID = clients.get((playerID + (clients.size() - 1)) % clients.size());
      String nextID = clients.get((playerID + (clients.size() + 1)) % clients.size());
      // sessions.get(playerID) XXX send a "you're dead" msg
      sessions.remove(playerID);
      PongServer newServer = new PongServer(prevID, nextID);
      clientToServers.get(prevID).right = newServer;
      clientToServers.get(nextID).left = newServer;
      clients.remove(playerID);
    }
  }

}
