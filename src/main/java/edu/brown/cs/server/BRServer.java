package edu.brown.cs.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import edu.brown.cs.database.ELOUpdater;
import edu.brown.cs.database.PongDatabase;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * One battle royale server containing
 * some number of players in a circle of games.
 */
public class BRServer implements Server {
  static final int MINPLAYERS = 3;
  static final double START_SPEED = 100;
  static final double ACCELERATION = 4;
  static final int MAXPLAYERS = 10;
  static final double START_TIME = 20;
  private static final Gson GSON = new Gson();
  private static final Double BILLION = 1000000000.;
  private static final Integer THOUSAND = 1000;
  private static final Integer DCPENALTY = -20;
  private static long idCounter = 0;
  private final List<String> clients;
  private final Map<String, Session> sessions;
  private final PongDatabase db;
  private final Map<String, ServerPair> clientToServers;
  private long myId;
  private double ballSpeed;
  private Timer ballAccelTimer;
  private Instant timerStart = null;
  private Map<String, Double> updatedElos; //adding to this should b synchro

  //TODO: get db query of users and elos at beginning of game when all players ready
  private boolean ready;
  private boolean starting;
  private Timer startTimer;
  /**
   * Construct a new BRServer.
   *
   * @param db Database to read/write to.
   */
  public BRServer(PongDatabase db) {
    clients = new CopyOnWriteArrayList<>();
    sessions = new ConcurrentHashMap<>();
    clientToServers = new ConcurrentHashMap<>();

    updatedElos = new ConcurrentHashMap<>();
    ready = false;
    starting = false;
    myId = nextId();
    ballSpeed = START_SPEED;
    this.db = db;
  }

  /**
   * NextID fetcher synchronized.
   *
   * @return the next id as a long
   */
  private static synchronized long nextId() {
    // get a unique ID for println purposes
    return idCounter++;
  }

  /**
   * Get if the server is running a game.
   *
   * @return if running
   */
  public boolean ready() {
    return ready;
  }

  /**
   * Add a new client to the server.
   *
   * @param id      Client ID (username)
   * @param session WebSocket session object for client
   */
  public void addClient(String id, Session session) {
    println("Adding client " + id + ".");
    synchronized (clientToServers) {
      // if we are running a game or already have user, do nothing
      if (clientToServers.containsKey(id) || ready()) {
        return;
      }
      clients.add(id);
      sessions.put(id, session);
      updatedElos.put(id, db.getLeaderboardEntry(id).getElo());
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
        }, (long) (START_TIME * THOUSAND));
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

  private void onFilled() { //get list of ids to ELOS!!!
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
        synchronized (clientToServers) {
          ballSpeed += ACCELERATION;
          for (ServerPair sp : clientToServers.values()) {
            if (sp.left == null) {
              continue;
            }
            sp.left.setBallSpeed(ballSpeed);
          }
        }
      }
    }, THOUSAND, THOUSAND);
  }

  private void sendGameStart(String id) {
    Session session = sessions.get(id);
    JsonObject updateObj = new JsonObject();
    updateObj.add("type",
            new JsonPrimitive(
                    PongWebSocketHandler.MESSAGE_TYPE.GAMESTART.ordinal()));
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
    usernamesObj.addProperty("type",
            PongWebSocketHandler.MESSAGE_TYPE.UPDATEUSERS.ordinal());
    JsonObject userPayload = new JsonObject();
    ServerPair sp = clientToServers.get(id);

    if (sp.left != null) {
      userPayload.addProperty("left", sp.left.getID(1));
    } else {
      userPayload.addProperty("left", sp.right.getID(2));
    }

    if (sp.right != null) {
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
        double timerValueSeconds = timerValue.toNanos() / BILLION;
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

        if (sp.left == null && sp.right == null) {
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
    if (clients.size() == 1) {
      // don't "kill" last client, just leave it
      // as every connection should be closed
      return;
    }
    Integer playerIndex = clients.indexOf(killed);
    if (!playerIndex.equals(-1)) {
      String prevID = clients.get((playerIndex + (clients.size() - 1))
              % clients.size());
      String nextID = clients.get((playerIndex + 1) % clients.size());

      Session deadSession = sessions.get(killed);
      JsonObject deadMsg = new JsonObject();
      deadMsg.addProperty("type",
              PongWebSocketHandler.MESSAGE_TYPE.PLAYERDEAD.ordinal());
      deadMsg.add("payload", new JsonObject());
      try {
        deadSession.getRemote().sendString(GSON.toJson(deadMsg));
      } catch (Exception e) {
        // println("Failed to send PLAYERDEAD");
        // this often happens; it's not a real problem
        // if we don't send a dead client a pack bc
        // they are already dead
      }

      JsonObject killLogMsg = new JsonObject();
      killLogMsg.addProperty("type",
              PongWebSocketHandler.MESSAGE_TYPE.KILLLOG.ordinal());
      JsonObject logPayload = new JsonObject();
      logPayload.addProperty("killer", killer == null ? "" : killer);
      logPayload.addProperty("killed", killed);
      killLogMsg.add("payload", logPayload);
      String killLogString = GSON.toJson(killLogMsg);
      for (Session session : sessions.values()) {
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
        PongServer newServer =
                new PongServer(prevID, nextID, p1PaddleY, p2PaddleY, ballSpeed);
        clientToServers.get(prevID).right = newServer;
        clientToServers.get(nextID).left = newServer;
        sendUsernamesUpdate(prevID);
        sendUsernamesUpdate(nextID);

        String survivor;
        if (killer.equals(prevID)) {
          survivor = nextID;
        } else {
          assert (killer.equals(nextID));
          survivor = prevID;
        }

        //need to adjust elo's for the player that got a kill, got killed
        //and the player who outlived the player that got killed
        updateScore(killer, killed, survivor);

      } else if (clients.size() == 2) {
        // 2-player game, don't make a new game between the two players
        clientToServers.get(prevID).right = null;
        clientToServers.get(nextID).left = null;
        sendUsernamesUpdate(prevID);
        sendUsernamesUpdate(nextID);

        String survivor;
        if (killer.equals(prevID)) {
          survivor = nextID;
        } else {
          assert (killer.equals(nextID));
          survivor = prevID;
        }
        updateScore(killer, killed, survivor);
      } else {
        // 1 player left, they win
        assert (clients.size() == 1);
        updateScore(killer, killed, null);
        synchronized (db) {
          db.incrementWins(clients.get(0));
          /*we only update the database with updated elos at 
           *the end of the game to avoid computation lag
           *mid-game
           **/
          db.updateELOs(updatedElos);
        }
        Session winSession = sessions.get(clients.get(0));
        JsonObject winMsg = new JsonObject();
        winMsg.addProperty("type",
                PongWebSocketHandler.MESSAGE_TYPE.PLAYERWIN.ordinal());
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
   *
   * @param id Client ID to kill.
   */
  public void removeClient(String id) {
    synchronized (clientToServers) {
      if (ready) { // if the game is running, just kill the player
        kill(null, id);
      } else { // otherwise, remove the player from the clients list
        clients.remove(id);
        sessions.remove(id);
        if (starting && clients.size() < MINPLAYERS) {
          // no longer enough players! :(
          starting = false;
          startTimer.cancel();
          startTimer = null;
          //no longer need reference to elo of player not in game
          updatedElos.remove(id);
        }
      }
    }
  }

  /**
   * Update the elos for the players involved in a kill
   *
   * @param killerID killer ID
   * @param killedID killed ID
   * @param survivorID survivor's ID, if oone exists
   */
  public void updateScore(String killerID, String killedID, String survivorID) { //with survivor and another w/o
    if (killerID == null) { //the case in which a player disconnects mid-game
      double killedELO = updatedElos.get(killedID) + DCPENALTY;
    /*if the new elo would cause the player to drop below the elo floor, 
     * set it to the elo floor. Also applied in all other cases
     */
      if (killedELO < ELOUpdater.ELOFLOOR) { 
        killedELO = ELOUpdater.ELOFLOOR;
      }
      updatedElos.replace(killedID, killedELO);
    } else { //general case
      double killerELO = updatedElos.get(killerID);
      double killedELO = updatedElos.get(killedID);

      /*
       * Calculation is based on how many players were in the game before
       * the killed player is removed. This also gives more weight for eliminations
       * that occur later in the game/when there are fewer players
       */
      double[] eloUpdates = ELOUpdater.update("WIN", killerELO, killedELO);
      killerELO = killerELO + eloUpdates[0] / (clients.size() + 1);
      killedELO = killedELO + eloUpdates[1] / (clients.size() + 1);

      if (killerELO < ELOUpdater.ELOFLOOR) {
        killerELO = ELOUpdater.ELOFLOOR;
      }

      if (killedELO < ELOUpdater.ELOFLOOR) {
    	  killedELO = ELOUpdater.ELOFLOOR;
      }
      updatedElos.replace(killerID, killerELO);
      updatedElos.replace(killedID, killedELO);

      if (survivorID != null) {
        double survivorELO = updatedElos.get(survivorID);
        /*
         *Although a survivor "WIN"s against it's opponent, the division by 3
         *ensures gain is valued less than actually getting the elimination
         */
        survivorELO = survivorELO + ELOUpdater.update
                ("WIN", survivorELO, killedELO)[0] / (3 * (clients.size() + 1));

        if (survivorELO < ELOUpdater.ELOFLOOR) {
        	survivorELO = ELOUpdater.ELOFLOOR;
        }
        updatedElos.replace(survivorID, survivorELO);
      }
    }


  }

  @Override
  public void println(String msg) {
    System.out.println("BR #" + myId + " :: " + msg);
  }

  /**
   * ServerPair class.
   */
  private class ServerPair {
    PongServer right, left;
  }

}
