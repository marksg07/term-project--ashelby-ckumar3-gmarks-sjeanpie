package edu.brown.cs.server;

import com.google.common.collect.ImmutableMap;
import edu.brown.cs.database.PongDatabase;
import spark.*;
import spark.template.freemarker.FreeMarkerEngine;

import java.sql.SQLException;
import java.util.Map;
import java.util.Random;

public class RouteHandler {
  private MainServer server;
  private PongDatabase db;

  public RouteHandler(MainServer main, PongDatabase pdb) {
    server = main;
    db = pdb;
  }

  public void addRoutes(FreeMarkerEngine freeMarker) {
    Spark.post("/game", new GameStartHandler(server), freeMarker);
    Spark.get("/home", new HomePageHandler(server), freeMarker);
    Spark.post("/login", new LoginHandler(server, db), freeMarker);
    Spark.get("/lb", new LeaderboardHandler(db), freeMarker);

    Spark.get("/", new HomePageHandler(server), freeMarker);
  }

  /**
   * /home page handler, handles login cookies if they are set.
   */
  private static class HomePageHandler implements TemplateViewRoute {
    private final MainServer server;

    public HomePageHandler(MainServer main) {
      server = main;
    }

    @Override
    public ModelAndView handle(Request req, Response res) throws Exception {
      Map<String, Object> variables = ImmutableMap.of("title",
              "P O N G F O L K S", "response", "", "successful", false);


      Map<String, String> cookies = req.cookies();

      /**
       * homepage loading with cookies, so we just want to check
       * if the user id matches the stored user id. if so, we log
       * them in. if not, we don't
       */
      if (cookies != null) {
        String usr = cookies.getOrDefault("username", "");
        String clientId = cookies.getOrDefault("userid", "");
        if (server.hasName(usr) && clientId.equals(server.getUUID(usr))) {
          String response = "";
          response = "Successfully logged in!";
          variables = ImmutableMap.<String, Object>builder().put("title",
                  "P O N G F O L K S").put("response", response).put(
                  "successful", true).put(
                  "username", usr).put(
                  "userid", clientId).build();

          return new ModelAndView(variables, "home.ftl");
        }
      }
      return new ModelAndView(variables, "home.ftl");
    }
  }

  /**
   * Leaderboard page.
   */
  private static class LeaderboardHandler implements TemplateViewRoute {
    private PongDatabase db;

    public LeaderboardHandler(PongDatabase pdb) {
      db = pdb;
    }

    public ModelAndView handle(Request request, Response response) throws Exception {
      Map<String, Object> variables = ImmutableMap.of("title",
              "Leaderboard", "leaderboardData", db.getLeaderboardData());

      return new ModelAndView(variables, "leaderboard.ftl");
    }
  }

  /**
   * Handles requests to login. Uses the PongDatabase to validate user's name/pass.
   */
  private static class LoginHandler implements TemplateViewRoute {
    private MainServer server;
    private PongDatabase db;

    public LoginHandler(MainServer main, PongDatabase pdb) {
      server = main;
      db = pdb;
    }

    @Override
    public ModelAndView handle(Request req, Response res) throws SQLException {
      QueryParamsMap qm = req.queryMap();
      String usr = qm.value("username");
      String pass = qm.value("password");
      String loginButton = qm.value("Log In");
      String acctButton = qm.value("Create Account");
      System.out.println(loginButton);
      System.out.println(acctButton);
      String response = "";
      boolean successful = false;
      if (loginButton != null) {
        if (!db.validateUser(usr)) {
          response = "User does not exist. Try creating an account!";
        } else { //check password
          if (db.validatePassword(usr, pass)) {
            response = "Successfully logged in!";
            successful = true;
          } else {
            response = "Username and password do not match.";
          }
        }
      } else {
        if (usr.length() > 10) {
          response = "Your username must not exceed 10 characters.";
        } else if (pass.length() < 3) {
          response = "Password must exceed 3 characters.";
        } else if (db.validateUser(usr)) {
          response = "Please choose a new username.";
        } else {
          db.createAccount(usr, pass);
          response = "Account successfully created!";
          successful = true;
        }
      }
      // Now we send the user their unique ID for cookie purposes.
      Map<String, Object> variables;
      if (successful) {
        String hash = server.getUUID(usr);
        if (hash == null) {
          /*
           * based on https://www.baeldung.com/java-random-string
           * generate new random ID
           */
          int leftLimit = 97; // letter 'a'
          int rightLimit = 122; // letter 'z'
          int targetStringLength = 32;
          Random random = new Random();
          StringBuilder buffer = new StringBuilder(targetStringLength);
          for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
          }
          hash = buffer.toString();

          server.putUUID(usr, hash);
        }
        variables = ImmutableMap.of("title",
                "P O N G F O L K S", "response", response,
                "successful", successful, "username", usr, "userid", hash);
      } else {
        variables = ImmutableMap.of("title",
                "P O N G F O L K S", "response", response,
                "successful", successful);
      }

      return new ModelAndView(variables, "home.ftl");
    }

  }

  /**
   * Handles the initial request to the server.
   */
  private static class GameStartHandler implements TemplateViewRoute {

    private MainServer server;

    public GameStartHandler(MainServer main) { server = main; }

    @Override
    public ModelAndView handle(Request request, Response response) throws Exception {
      QueryParamsMap map = request.queryMap();
      if (!(map.hasKey("username") && map.hasKey("userid"))) {
        return new HomePageHandler(server).handle(request, response);
      }
      Map<String, Object> variables = ImmutableMap.of("title",
              "Game", "username", map.value("username"), "userid", map.value("userid"));
      return new ModelAndView(variables, "pong.ftl");
    }
  }
}

