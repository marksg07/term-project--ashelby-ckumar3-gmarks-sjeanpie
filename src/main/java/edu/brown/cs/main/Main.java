package edu.brown.cs.main;

import edu.brown.cs.database.PongDatabase;
import edu.brown.cs.pong.PongGame;
import edu.brown.cs.server.MainServer;
import edu.brown.cs.server.PongWebSocketHandler;
import edu.brown.cs.server.Server;
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

import java.awt.font.GlyphMetrics;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

import com.google.gson.Gson;

public final class Main {


  private static final int DEFAULT_PORT = 4567;
  private static final List<PongGame> GAME_LIST = new ArrayList<>();
  private static final Gson GSON = new Gson();
  private static final PongDatabase db = new PongDatabase("data/pongfolksDB.sqlite3");

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
    // Parse command line arguments
    OptionParser parser = new OptionParser();
    parser.accepts("port").withRequiredArg().ofType(Integer.class)
    .defaultsTo(DEFAULT_PORT);
    OptionSet options = parser.parse(args);
    runSparkServer((int) options.valueOf("port"));
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

  private void runSparkServer(int port) {
    Spark.port(getHerokuAssignedPort());
    Spark.externalStaticFileLocation("src/main/resources/static");
    Spark.exception(Exception.class, new ExceptionPrinter());
    FreeMarkerEngine freeMarker = createEngine();
    MainServer serv = new MainServer();
    PongWebSocketHandler.setServer(serv);
    //Spark.webSocketIdleTimeoutMillis(2000);

    Spark.webSocket("/gamesocket", PongWebSocketHandler.class);
    Spark.get("/game", new GameStartHandler(), freeMarker);
    Spark.get("/lobby", new LobbyHandler(), freeMarker);
    Spark.get("/home", new HomePageHandler(), freeMarker);
    Spark.post("/login", new LoginHandler(), freeMarker);
    Spark.get("/lb", new LeaderboardHandler(), freeMarker);
    Spark.post("/stats", new StatsHandler());
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

  private static class HomePageHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request request, Response response) throws Exception {
      Map<String, Object> variables = ImmutableMap.of("title",
      "P O N G F O L K S", "response", "");

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
      "Leaderboard");

      System.out.println("here");

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
      if (loginButton != null) {
        if (!db.validateUser(usr)) {
          response = "User does not exist\n Would you like to create an account?";
        } else { //check password
          if (db.validatePassword(usr, pass)) {
            response = "Successfully logged in!";
          } else {
            response = "User exists; however, password is incorrect";
          }
        }
      } else {
        if (usr.length() > 10) {
          response = "Username is too long! Username can not have more than 10 characters";
        } else if (pass.length() < 3) {
          response = "Password must be greater than 3 characters";
        } else if (db.validateUser(usr)) {
          response = "User already exists. Please choose a new username.";
        } else {
          db.createAccount(usr, pass);
          response = "Account successfully created!";
        }
      }
      //TODO: get password and hash it
      Map<String, Object> variables = ImmutableMap.of("title",
      "P O N G F O L K S", "response", response);

      return new ModelAndView(variables, "home.ftl");
    }

  }


  private static class LobbyHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request request, Response response) throws Exception {
      Map<String, Object> variables = ImmutableMap.of("title",
      "Battle Royale");
      return new ModelAndView(variables, "lobby.ftl");
    }
  }

  /**
   * Handles the initial request to the server.
   */
  private static class GameStartHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request request, Response response) throws Exception {

      Map<String, Object> variables = ImmutableMap.of("title",
      "Game");
      return new ModelAndView(variables, "pong.ftl");
    }
  }

  private static class StatsHandler implements Route {
    @Override
    public String handle(Request req, Response res) {

      Map<String, Object> variables =
      ImmutableMap.of("userData", db.getLeaderboardData());
      return GSON.toJson(variables);
    }
  }

    static int getHerokuAssignedPort() {
      ProcessBuilder processBuilder = new ProcessBuilder();
      if (processBuilder.environment().get("PORT") != null) {
        return Integer.parseInt(processBuilder.environment().get("PORT"));
      }
      return 4567; //return default port if heroku-port isn't set (i.e. on localhost)

    }
  }
