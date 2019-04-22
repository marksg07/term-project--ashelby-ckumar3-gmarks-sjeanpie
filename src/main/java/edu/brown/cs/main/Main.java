package edu.brown.cs.main;

import edu.brown.cs.pong.PongGame;
import edu.brown.cs.server.MainServer;
import edu.brown.cs.server.Server;
import freemarker.template.Configuration;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;
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
  private static final int DEFAULT_PORT = 4567;
  private static final List<PongGame> GAME_LIST = new ArrayList<>();
  private static final Gson GSON = new Gson();

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
    Spark.port(port);
    Spark.externalStaticFileLocation("src/main/resources/static");
    Spark.exception(Exception.class, new ExceptionPrinter());
    FreeMarkerEngine freeMarker = createEngine();
    Spark.get("/game", new GameStartHandler(), freeMarker);
    Spark.post("/logic", new GameLogicHandler());
    Server serv = new MainServer();
    serv.run();
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

  /**
   * Handles the initial request to the server.
   */
  private static class GameStartHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request request, Response response) throws Exception {
      Map<String, Object> variables = ImmutableMap.of("title",
              "Game");
      PongGame leftGame = new PongGame(400, 300, 150, 40, 10, 300);
      PongGame rightGame = new PongGame(400, 300, 150, 40, 10, 300);
      GAME_LIST.clear();
      GAME_LIST.add(leftGame);
      GAME_LIST.add(rightGame);
      return new ModelAndView(variables, "pong.ftl");
    }
  }


  private static class GameLogicHandler implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {

      QueryParamsMap q = request.queryMap();
      String input = q.value("press");
      PongGame leftGame = GAME_LIST.get(0);
      PongGame rightGame = GAME_LIST.get(1);
      if (input.equals("0")) {
        leftGame.setP2Input(PongGame.InputType.NONE);
        rightGame.setP1Input(PongGame.InputType.NONE);
      } else if (input.equals("1")) {
        leftGame.setP2Input(PongGame.InputType.UP);
        rightGame.setP1Input(PongGame.InputType.UP);
      } else if (input.equals("-1")) {
        leftGame.setP2Input(PongGame.InputType.DOWN);
        rightGame.setP1Input(PongGame.InputType.DOWN);
      }

      Boolean leftEnemyWin = false;
      Boolean rightEnemyWin = false;
      Boolean leftEnemyLose = false;
      Boolean rightEnemyLose = false;


      if (!(leftEnemyLose || leftEnemyWin)) {
        Integer leftState = leftGame.tick(.02);
        switch (leftState) {
          case 2: leftEnemyLose = true;
          case 1: leftEnemyWin = true;
        }
      }

      if (!(rightEnemyLose || rightEnemyWin)) {
        Integer rightState = rightGame.tick(.02);
          switch (rightState) {
            case 2: rightEnemyWin = true;
            case 1: rightEnemyLose = true;
          }
      }


      Map<String, Object> resp = new HashMap();
      resp.put("title", "Game");
      resp.put("leftPaddleY", leftGame.getP1PaddleY());
      resp.put("rightPaddleY", rightGame.getP2PaddleY());
      resp.put("playerPaddleY", ((int) ((leftGame.getP2PaddleY() + rightGame.getP1PaddleY()) / 2)));
      resp.put("ballLeftX", leftGame.getBallX());
      resp.put("ballLeftY", leftGame.getBallY());
      resp.put("ballRightX", rightGame.getBallX() + 400);
      resp.put("ballRightY", rightGame.getBallY());
      resp.put("rightEnemyWin", rightEnemyWin);
      resp.put("rightEnemyLose", rightEnemyLose);
      resp.put("leftEnemyWin", leftEnemyWin);
      resp.put("leftEnemyLose", leftEnemyLose);

      return GSON.toJson(resp);
    }
  }
}
