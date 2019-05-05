package edu.brown.cs.main;

import edu.brown.cs.database.PongDatabase;
import edu.brown.cs.server.MainServer;
import edu.brown.cs.server.PongWebSocketHandler;
import edu.brown.cs.server.RouteHandler;
import freemarker.template.Configuration;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;
import spark.*;

import java.util.Random;

import java.util.Map;
import java.util.HashMap;

import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

import com.google.gson.Gson;
import sun.rmi.runtime.Log;

public final class Main {


  private static final int DEFAULT_PORT = 4567;
  private static final Gson GSON = new Gson();
  private static final PongDatabase db = new PongDatabase("data/pongfolksDB.sqlite3");
  //private static final Map<String, String> unsToUuids = new HashMap<>();
  private MainServer server;

  /**
   * The initial method called when execution begins.
   *
   * @param args An array of command line arguments
   */
  public static void main(String[] args) {
    new Main(args).run();
  }

  private String[] args;

  private Main(String[] args) {
    this.args = args;
  }

  private void run() {
    runSparkServer();
  }

  private static FreeMarkerEngine createEngine() {
    Configuration config = new Configuration();
    File templates = new File("src/main/resources/spark/template/freemarker");
    try {
      config.setDirectoryForTemplateLoading(templates);
    } catch (IOException ioe) {
      System.out.printf("ERROR: Unable use %s for template loading.%n",
              templates);
      System.exit(1);
    }
    return new FreeMarkerEngine(config);
  }

  private void runSparkServer() {
    Spark.port(getHerokuAssignedPort());
    Spark.externalStaticFileLocation("src/main/resources/static");
    Spark.exception(Exception.class, new ExceptionPrinter());

    FreeMarkerEngine freeMarker = createEngine();

    server = new MainServer(db);
    PongWebSocketHandler.setServer(server);

    // timeout for websockets = 2 seconds in case of badly behaved clients
    Spark.webSocketIdleTimeoutMillis(2000);
    Spark.webSocket("/gamesocket", PongWebSocketHandler.class);
    (new RouteHandler(server, db)).addRoutes(freeMarker);
    // make everything redirect to HTTPS
    Spark.before(((request, response) -> {
      final String url = request.url();
      if (url.startsWith("http://")) {
        final String[] split = url.split("http://");
        response.redirect("https://" + split[1]);
      }
    }));
  }

  /**
   * Display an error page when an exception occurs in the server.
   */
  private static class ExceptionPrinter implements ExceptionHandler<Exception> {
    @Override
    public void handle(Exception e, Request req, Response res) {
      res.status(500);
      StringWriter stacktrace = new StringWriter();
      try (PrintWriter pw = new PrintWriter(stacktrace)) {
        pw.println("<pre>");
        e.printStackTrace(pw);
        pw.println("</pre>");
      }
      res.body(stacktrace.toString());
    }
  }
<<<<<<< HEAD
  
=======

  private static class HomePageHandler implements TemplateViewRoute {
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
        if (clientId.equals(unsToUuids.getOrDefault(usr, "DNE"))) {



//        String usr = cookies.getOrDefault("username", "");
//        String pass = cookies.getOrDefault("password", "");
//
//
//        if ((!usr.equals("")) && (!pass.equals(""))) {
//
          Boolean successful = false;
          String response = "";
//          if (!db.validateUser(usr)) {
//            response = "User does not exist. Try creating an account!";
//          } else { //check password
//            if (db.validatePassword(usr, pass)) {
              response = "Successfully logged in!";
              successful = true;
//            } else {
//              response = "Username and password do not match.";
//            }
//          }
//
//          if (successful) {
//            String userId = "";
//
//
//            /**
//             * based on https://www.baeldung.com/java-random-string
//             */
//              int leftLimit = 97; // letter 'a'
//              int rightLimit = 122; // letter 'z'
//              int targetStringLength = 32;
//              Random random = new Random();
//              StringBuilder buffer = new StringBuilder(targetStringLength);
//              for (int i = 0; i < targetStringLength; i++) {
//                int randomLimitedInt = leftLimit + (int)
//                        (random.nextFloat() * (rightLimit - leftLimit + 1));
//                buffer.append((char) randomLimitedInt);
//              }
//              userId = buffer.toString();
//
//              unsToUuids.put(usr, userId);
//
//
            variables = ImmutableMap.<String, Object>builder().put("title",
                    "P O N G F O L K S").put( "response", response).put(
                    "successful", successful).put(
                            "username", usr).put(
                                    "userid", clientId).build();
//          } else {
//            variables = ImmutableMap.of("title",
//                    "P O N G F O L K S", "response", response,
//                    "successful", successful);
//          }

          return new ModelAndView(variables, "home.ftl");
        }
      }


      //code to have starting webpage that allows for user login
      // finding a match/going into a lobby
      // looking up users
      // starting up the server should call this before game start handler
      return new ModelAndView(variables, "home.ftl");
    }
  }

  private static class LeaderboardHandler implements TemplateViewRoute {

    public ModelAndView handle(Request request, Response response) throws Exception {
      Map<String, Object> variables = ImmutableMap.of("title",
      "Leaderboard", "leaderboardData", db.getLeaderboardData());

      return new ModelAndView(variables, "leaderboard.ftl");
    }
  }

  private static class LoginHandler implements TemplateViewRoute {
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
      //TODO: get password and hash it
      Map<String, Object> variables;
      if(successful) {
        String hash = unsToUuids.get(usr);
        if (hash == null) {
                      /**
             * based on https://www.baeldung.com/java-random-string
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

              unsToUuids.put(usr, hash);
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

>>>>>>> cleaned up javascript and ftl files
  /**
   * Get the heroku port.
   *
   * @return port
   */
  static int getHerokuAssignedPort() {
    ProcessBuilder processBuilder = new ProcessBuilder();
    if (processBuilder.environment().get("PORT") != null) {
      return Integer.parseInt(processBuilder.environment().get("PORT"));
    }
    return 4567; //return default port if heroku-port isn't set (i.e. on localhost)

  }
}
