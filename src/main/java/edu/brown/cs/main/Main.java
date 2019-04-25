package edu.brown.cs.main;

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

import com.google.gson.Gson;

public final class Main {
<<<<<<< HEAD
  private static final int DEFAULT_PORT = 4567;

=======
  private static final int DEFAULT_PORT = 1111;
  private static final List<PongGame> GAME_LIST = new ArrayList<>();
>>>>>>> more javascript and html for find game button
  private static final Gson GSON = new Gson();

  // TRASH UNDER HERE
  private static final List<PongGame> GAME_LIST = new ArrayList<>();
  private static Integer firstId = null;
  private static Integer secondId = null;
  private static PongGame game = null;

  /**
   * The initial method called when execution begins.
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
    Spark.port(port);
    Spark.externalStaticFileLocation("src/main/resources/static");
    Spark.exception(Exception.class, new ExceptionPrinter());
    FreeMarkerEngine freeMarker = createEngine();
    MainServer serv = new MainServer();

    serv.run();
    PongWebSocketHandler.setServer(serv);
    Spark.webSocket("/gamesocket", PongWebSocketHandler.class);
    Spark.get("/game", new GameStartHandler(), freeMarker);
    Spark.get("/lobby", new LobbyHandler(), freeMarker);
    Spark.get("/home", new HomePageHandler());
  }

  /**
   * Display an error page when an exception occurs in the server.
   *
   */
  private static class ExceptionPrinter implements ExceptionHandler {
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
  private static class HomePageHandler implements TemplateViewRoute {
	  @Override
	  public ModelAndView handle(Request request, Response response) throws Exception {
		  Map<String, Object> variables = ImmutableMap.of("title",
      "P O N G F O L K S");
=======
  private static class HomePageHandler implements TemplateViewRoute, Route {
	  @Override
	  public ModelAndView handle(Request request, Response response) throws Exception {
		  Map<String, Object> variables = ImmutableMap.of("title",
      "P O N G B R O S");
>>>>>>> New route for start page and
		//code to have starting webpage that allows for user login
		// finding a match/going into a lobby
		// looking up users
		// starting up the server should call this before game start handler  
<<<<<<< HEAD
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
  
=======
		  return new ModelAndView(variables, "pong.ftl");
	  }
  }

>>>>>>> New route for start page and
  /**
   * Handles the initial request to the server.
   */
  private static class GameStartHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request request, Response response) throws Exception {
<<<<<<< HEAD
      /*int id = -1;
      if(firstId == null) {
        id = firstId = 0;
      } else if (secondId == null) {
        id = secondId = 1;
        game = new PongGame(400, 300, 150, 40, 10, 20);
      }
      /*PongGame rightGame = new PongGame(400, 300, 150, 40, 10, 300);
=======
      Map<String, Object> variables = ImmutableMap.of("title",
              "P O N G B R O S");
      PongGame leftGame = new PongGame(400, 300, 150, 40, 10, 300);
      PongGame rightGame = new PongGame(400, 300, 150, 40, 10, 300);
>>>>>>> New route for start page and
      GAME_LIST.clear();
      GAME_LIST.add(leftGame);
      GAME_LIST.add(rightGame);*/
      Map<String, Object> variables = ImmutableMap.of("title",
              "Game");
      return new ModelAndView(variables, "pong.ftl");
    }
  }
}
