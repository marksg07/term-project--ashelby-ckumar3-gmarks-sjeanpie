package edu.brown.cs.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import edu.brown.cs.database.PongDatabase;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * One battle royale server containing some number of players in a circle of games.
 */
public class BRServer implements Server {
  private static final Gson GSON = new Gson();
  private final List<String> clients;
  private final Map<String, Session> sessions;
  private long myId;
  private final PongDatabase db;
  static final int MINPLAYERS = 3;
  static final double startSpeed = 100;
  static final double acceleration = 4;
  private double ballSpeed;
  private Timer ballAccelTimer;
  static final int MAXPLAYERS = 10;
  static final double startTime = 20;
  private Instant timerStart = null;

  private static long idCounter = 0;

  private static synchronized long nextId() {
    // get a unique ID for println purposes
    return idCounter++;
  }

  private class ServerPair {
    PongServer right, left;
  }

  private final Map<String, ServerPair> clientToServers;
  private boolean ready;
  private boolean starting;
  private Timer startTimer;

  /**
   * Construct a new BRServer.
   * @param db Database to read/write to.
   */
  public BRServer(PongDatabase db) {
    clients = new CopyOnWriteArrayList<>();
    sessions = new ConcurrentHashMap<>();
    clientToServers = new ConcurrentHashMap<>();
    ready = false;
    starting = false;
    myId = nextId();
    ballSpeed = startSpeed;
    this.db = db;
  }

  /**
   * Get if the server is running a game.
   * @return if running
   */
  public boolean ready() {
    return ready;
  }

  /**
   * Add a new client to the server.
   * @param id Client ID (username)
   * @param session WebSocket session object for client
   */
  public void addClient(String id, Session session) {
    println("Adding client " + id + ".");
    synchronized (clientToServers) {
      // if we are running a game or already have user, do nothing
      if(clientToServers.containsKey(id) || ready()) {
        return;
      }
      clients.add(id);
      sessions.put(id, session);
      println(clients.size() + " players total.");
      if (clients.size() == MINPLAYERS) {
        // when we hit min players, start a timer to start the game.
        starting = true;
        startTimer = new Timer();
        timerStart = Instant.now();
        startTimer.schedule(new TimerTask() {
          @Override
          public void run() {
            synchronized (clientToServers) {
              starting = false;
              ready = true;
              onFilled();
            }
          }
        }, (long)(startTime * 1000));
      }
      if (clients.size() == MAXPLAYERS) {
        // when we hit max players, start the game immediately.
        assert (starting);
        starting = false;
        startTimer.cancel();
        startTimer = null;
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
    // make each client play against each adjacent client
    for (int i = 0; i < clients.size(); i++) {
      int iLeft = (clients.size() - 1 + i) % clients.size();
      String cliLeft = clients.get(iLeft);
      String cli = clients.get(i);
      PongServer serv = new PongServer(cliLeft, cli, ballSpeed);
      clientToServers.get(cli).left = serv;
      clientToServers.get(cliLeft).right = serv;
    }

    // add 1 game to client's total games, tell them game's started
    for (String id : sessions.keySet()) {
      synchronized (db) {
        db.incrementTotalGames(id);
      }
      sendGameStart(id);
      sendUsernamesUpdate(id);
    }
    // start speed increase timer, will accelerate ball every second
    ballAccelTimer = new Timer();
    ballAccelTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        synchronized(clientToServers) {
          ballSpeed += acceleration;
          for (ServerPair sp : clientToServers.values()) {
            if(sp.left == null)
              continue;
            sp.left.setBallSpeed(ballSpeed);
          }
        }
      }
    }, 1000, 1000);
  }

  private void sendGameStart(String id) {
    Session session = sessions.get(id);
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

  private void sendUsernamesUpdate(String id) {
    Session session = sessions.get(id);
    JsonObject usernamesObj = new JsonObject();
    usernamesObj.addProperty("type", PongWebSocketHandler.MESSAGE_TYPE.UPDATEUSERS.ordinal());
    JsonObject userPayload = new JsonObject();
    ServerPair sp = clientToServers.get(id);

    if(sp.left != null) {
      userPayload.addProperty("left", sp.left.getID(1));
    } else {
      userPayload.addProperty("left", sp.right.getID(2));
    }

    if(sp.right != null) {
      userPayload.addProperty("right", sp.right.getID(2));
    } else {
      userPayload.addProperty("right", sp.left.getID(1));
    }

    usernamesObj.add("payload", userPayload);
    try {
      session.getRemote().sendString(GSON.toJson(usernamesObj));
    } catch (IOException e) {
      println("Sending username update failed:");
      e.printStackTrace();
    }
  }

  @Override
  public void update(String id, Object obj) {
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
      if (!ready && starting) {
        // send how long until game will start
        Duration timerValue = Duration.between(Instant.now(), timerStart);
        double timerValueSeconds = timerValue.toNanos() / 1000000000.0;
        JsonObject ret = new JsonObject();
        ret.addProperty("timeUntilStart", timerValueSeconds);
        return ret;
      }
      if (clientToServers.containsKey(id)) {
        ServerPair sp = clientToServers.get(id);
        if (sp == null) {
          // player dead
          return new JsonObject();
        }
        JsonObject obj = new JsonObject();

        // this is where we check for dead players in the left and right games
        String leftDeadID = "";
        String leftKillerID = "";
        String rightDeadID = "";
        String rightKillerID = "";

        if (sp.left != null) {
          if (sp.left.getGame().isP1Dead()) {
            leftDeadID = sp.left.getID(1);
            leftKillerID = sp.left.getID(2);
          } else if (sp.left.getGame().isP2Dead()) {
            leftDeadID = sp.left.getID(2);
            leftKillerID = sp.left.getID(1);
          }
        }

        if (sp.right != null) {
          if (sp.right.getGame().isP1Dead()) {
            rightDeadID = sp.right.getID(1);
            rightKillerID = sp.right.getID(2);
          } else if (sp.right.getGame().isP2Dead()) {
            rightDeadID = sp.right.getID(2);
            rightKillerID = sp.right.getID(1);
          }
        }

        if (!leftDeadID.equals("")) {
          kill(leftKillerID, leftDeadID);
        }

        if (!rightDeadID.equals("")) {
          kill(rightKillerID, rightDeadID);
        }

        if(sp.left == null && sp.right == null) {
          return obj;
        }

        if (sp.left != null) {
          obj.add("left", sp.left.getGameState(id));
        } else {
          obj.add("left", sp.right.getFlippedGameState(id));
        }

        if (sp.right != null) {
          obj.add("right", sp.right.getGameState(id));
        } else {
          obj.add("right", sp.left.getFlippedGameState(id));
        }
        return obj;
      }
    }
    return new JsonObject();
  }

  private void kill(String killer, String killed) {
    if(clients.size() == 1) {
      // don't "kill" last client, just leave it as every connection should be closed
      return;
    }
    Integer playerIndex = clients.indexOf(killed);
    if (!playerIndex.equals(-1)) {
      String prevID = clients.get((playerIndex + (clients.size() - 1)) % clients.size());
      String nextID = clients.get((playerIndex + 1) % clients.size());

      Session deadSession = sessions.get(killed);
      JsonObject deadMsg = new JsonObject();
      deadMsg.addProperty("type", PongWebSocketHandler.MESSAGE_TYPE.PLAYERDEAD.ordinal());
      deadMsg.add("payload", new JsonObject());
      try {
        deadSession.getRemote().sendString(GSON.toJson(deadMsg));
      } catch (Exception e) {
        // println("Failed to send PLAYERDEAD");
        // this often happens; it's not a real problem if we don't send a dead client a pack bc
        // they are already dead
      }

      JsonObject killLogMsg = new JsonObject();
      killLogMsg.addProperty("type", PongWebSocketHandler.MESSAGE_TYPE.KILLLOG.ordinal());
      JsonObject logPayload = new JsonObject();
      logPayload.addProperty("killer", killer == null ? "" : killer);
      logPayload.addProperty("killed", killed);
      killLogMsg.add("payload", logPayload);
      String killLogString = GSON.toJson(killLogMsg);
      for(Session session : sessions.values()) {
        try {
          session.getRemote().sendString(killLogString);
        } catch (Exception e) {
          //println("Failed to send kill log");
          // same
        }
      }

      sessions.remove(killed);
      clients.remove(killed);
      // make new server for new neighbors
      if (clients.size() > 2) {
        double p1PaddleY = clientToServers.get(prevID).right.getP1PaddleY();
        double p2PaddleY = clientToServers.get(nextID).left.getP2PaddleY();
        PongServer newServer = new PongServer(prevID, nextID, p1PaddleY, p2PaddleY, ballSpeed);
        clientToServers.get(prevID).right = newServer;
        clientToServers.get(nextID).left = newServer;
        sendUsernamesUpdate(prevID);
        sendUsernamesUpdate(nextID);
      } else if (clients.size() == 2) {
        // 2-player game, don't make a new game between the two players
        clientToServers.get(prevID).right = null;
        clientToServers.get(nextID).left = null;
        sendUsernamesUpdate(prevID);
        sendUsernamesUpdate(nextID);
      } else {
        // 1 player left, they win
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
          //println("Failed to send PLAYERWIN");
        }
      }
      clientToServers.remove(killed);
    }
  }

  /**
   * Remove a client by their ID.
   * @param id Client ID to kill.
   */
  public void removeClient(String id) {
    synchronized (clientToServers) {
      if(ready) { // if the game is running, just kill the player
        kill(null, id);
      } else { // otherwise, remove the player from the clients list
        clients.remove(id);
        sessions.remove(id);
        if (starting && clients.size() < MINPLAYERS) { // no longer enough players! :(
          starting = false;
          startTimer.cancel();
          startTimer = null;
        }
      }
    }
  }

  @Override
  public void println(String msg) {
    System.out.println("BR #" + myId + " :: " + msg);
  }

  public boolean hasClient(String id) {
    return clients.contains(id);
  }

}
