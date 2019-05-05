package edu.brown.cs.server;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

/**
 * Packet descriptions:
 * REQUESTID: Server->client, contains nothing. Prompted by client connect.
 * SENDID: Client->server, sends ID and password hash. Prompted by request ID packet.
 * GAMESTART: Server->client, contains nothing. Prompted by
 *  server when matchmaking is ready.
 * INPUT: Client->server, contains value of input and ID. Prompted by client periodically.
 * UPDATE: Server->client, contains ID of server and game data. Prompted by client input (XXX).
 * PLAYERDEAD: Server->client, contains nothing. u dead.
 */

@WebSocket
public class PongWebSocketHandler {
  private static final Gson GSON = new Gson();
  private static Integer nextId = 0;
  private static MainServer server = null;

  public enum MESSAGE_TYPE {
    REQUESTID,
    SENDID,
    GAMESTART,
    INPUT,
    UPDATE,
    PLAYERDEAD,
    PLAYERWIN,
    BADID,
    UPDATEUSERS,
    KILLLOG;

    public static MESSAGE_TYPE fromInt(int t) {
      switch(t) {
        case 0:
          return REQUESTID;
        case 1:
          return SENDID;
        case 2:
          return GAMESTART;
        case 3:
          return INPUT;
        case 4:
          return UPDATE;
        case 5:
          return PLAYERDEAD;
        case 6:
          return PLAYERWIN;
        case 7:
          return BADID;
        case 8:
          return UPDATEUSERS;
        case 9:
          return KILLLOG;
      }
      return null;
    }
  }

  @OnWebSocketError
  public void onError(Throwable cause) {
    System.out.println("マッチング状況WebSocketセッションでエラーが起こりました。");
    cause.printStackTrace();
  }

  @OnWebSocketConnect
  public void connected(Session session) throws IOException {
    try {
      System.out.println("Requesting ident");
      // Build the REQUESTID message
      JsonObject connObj = new JsonObject();
      connObj.add("type", new JsonPrimitive(MESSAGE_TYPE.REQUESTID.ordinal()));
      JsonObject payload = new JsonObject();
      connObj.add("payload", payload);
      // TODO Send the CONNECT message
      session.getRemote().sendString(GSON.toJson(connObj));
    } catch (Exception e) {
      System.out.println("In web socket connect, caught error:");
      e.printStackTrace();
    }
  }

  @OnWebSocketClose
  public void closed(Session session, int statusCode, String reason) {
    // user bad
    server.removeSession(session);
  }

  @OnWebSocketMessage
  public void message(Session session, String message) throws IOException {
    try {
      JsonObject received = GSON.fromJson(message, JsonObject.class);
      JsonObject payload = received.get("payload").getAsJsonObject();
      String id = payload.get("id").getAsString();
      MESSAGE_TYPE type = MESSAGE_TYPE.fromInt(received.get("type").getAsInt());
      switch (type) {
        case SENDID:
          String userid = payload.get("userid").getAsString();
          if(server.hasName(id) && userid.equals(server.getUUID(id))) {
            if(!server.addClient(id, session)) {
              JsonObject badIdObj = new JsonObject();
              badIdObj.add("type", new JsonPrimitive(MESSAGE_TYPE.BADID.ordinal()));
              badIdObj.add("payload", new JsonObject());
              session.getRemote().sendString(GSON.toJson(badIdObj));
            }
          } else {
            JsonObject badIdObj = new JsonObject();
            badIdObj.add("type", new JsonPrimitive(MESSAGE_TYPE.BADID.ordinal()));
            badIdObj.add("payload", new JsonObject());
            session.getRemote().sendString(GSON.toJson(badIdObj));
          }
          break;

        case INPUT:
          Integer input = payload.get("input").getAsInt();
          server.update(id, input);
          JsonObject data = server.getGameState(id);
          if (data == null) { // client is not in a game
            return;
          }
          JsonObject updateObj = new JsonObject();
          updateObj.add("type", new JsonPrimitive(MESSAGE_TYPE.UPDATE.ordinal()));
          JsonObject payloadOut = new JsonObject();
          payloadOut.add("state", data);
          updateObj.add("payload", payloadOut);
          session.getRemote().sendString(GSON.toJson(updateObj));
          break;

        default:
          System.out.println("Received garbage from client! Thanks client!");
      }
    } catch (Exception e) {
      System.out.println("In web socket message, caught error:");
      e.printStackTrace();
    }
  }

  public static void setServer(MainServer server) {
    PongWebSocketHandler.server = server;
  }
}
