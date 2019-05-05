package edu.brown.cs.server;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * Packet descriptions:
 * REQUESTID: Server to client, contains nothing. Prompted by client connect.
 * SENDID: Client to server, sends ID and password hash.
 * Prompted by request ID packet.
 * GAMESTART: Server to client, contains nothing. Prompted by
 *  server when matchmaking is ready.
 * INPUT: Client to server, contains value of input and ID.
 * Prompted by client periodically.
 * UPDATE: Server to client, contains ID of server and game data.
 * Prompted by client input (XXX).
 * PLAYERDEAD: Server to client, contains nothing. u dead.
 */

@WebSocket
public class PongWebSocketHandler {
  private static final Gson GSON = new Gson();
  private static MainServer server = null;

  /**
   * A message type enumeration.
   */
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

    /**
     * Message types from integers.
     * @param t the input integer
     * @return the message type
     */
    public static MESSAGE_TYPE fromInt(int t) {
      switch (t) {
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
        default:
          return null;
      }
    }
  }

  /**
   * Handles websocket errors.
   * @param cause cause of error
   */
  @OnWebSocketError
  public void onError(Throwable cause) {
    System.out.println("マッチング状況WebSocketセッションでエラーが起こりました。");
    cause.printStackTrace();
  }

  /**
   * Connects a session.
   * @param session session to connect
   * @throws IOException if io fails
   */
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

  /**
   * Closes a session.
   * @param session session to close
   * @param statusCode status code when closing
   * @param reason reason for closing
   */
  @OnWebSocketClose
  public void closed(Session session, int statusCode, String reason) {
    // user bad
    server.removeSession(session);
  }

  /**
   * Sends message through a session.
   * @param session session through which to send message
   * @param message message to send
   * @throws IOException if io fails
   */
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
          if (server.hasName(id) && userid.equals(server.getUUID(id))) {
            if (!server.addClient(id, session)) {
              JsonObject badIdObj = new JsonObject();
              badIdObj.add("type",
                      new JsonPrimitive(MESSAGE_TYPE.BADID.ordinal()));
              badIdObj.add("payload", new JsonObject());
              session.getRemote().sendString(GSON.toJson(badIdObj));
            }
          } else {
            JsonObject badIdObj = new JsonObject();
            badIdObj.add("type",
                    new JsonPrimitive(MESSAGE_TYPE.BADID.ordinal()));
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
          updateObj.add("type",
                  new JsonPrimitive(MESSAGE_TYPE.UPDATE.ordinal()));
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

  /**
   * Sets the server of the socket handler.
   * @param server the MainServer
   */
  public static void setServer(MainServer server) {
    PongWebSocketHandler.server = server;
  }
}
