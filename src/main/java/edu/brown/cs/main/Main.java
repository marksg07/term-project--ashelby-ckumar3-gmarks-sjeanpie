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

  private static final Gson GSON = new Gson();

  // TRASH UNDER HERE
  private static final List<PongGame> GAME_LIST = new ArrayList<>();
  private static Integer firstId = null;
  private static Integer secondId = null;
  private static PongGame game = null;

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
      int id = -1;
      if(firstId == null) {
        id = firstId = 0;
      } else if (secondId == null) {
        id = secondId = 1;
        game = new PongGame(400, 300, 150, 40, 10, 20);
      }
      /*PongGame rightGame = new PongGame(400, 300, 150, 40, 10, 300);
      GAME_LIST.clear();
      GAME_LIST.add(leftGame);
      GAME_LIST.add(rightGame);*/
      Map<String, Object> variables = ImmutableMap.of("title",
              "Game", "id", id);
      return new ModelAndView(variables, "pong.ftl");
    }
  }


  private static class GameLogicHandler implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {

      QueryParamsMap q = request.queryMap();
      String input = q.value("press");
      Integer id = Integer.parseInt(q.value("id"));
      if(id < 0) {
        return "";
      }
      PongGame.InputType inp = PongGame.InputType.NONE;
      if(input.equals("0")) {
        inp = PongGame.InputType.NONE;
      } else if (input.equals("1")) {
        inp = PongGame.InputType.UP;
      } else if (input.equals("-1")) {
        inp = PongGame.InputType.DOWN;
      }

      if(id == 0) {
        game.setP1Input(inp);
      } else if (id == 1) {
        game.setP2Input(inp);
      }

      int winner = game.tick(0.02);
      boolean youwin = false;
      boolean youlose = false;
      if (winner == 1 || winner == 2) {
        if(winner == id + 1) {
          youwin = true;
        } else {
          youlose = true;
        }
      }

      /*
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
      */

      Map<String, Object> resp = new HashMap();
      resp.put("title", "Game");
      resp.put("leftPaddleY", game.getP1PaddleY());
      resp.put("rightPaddleY", game.getP2PaddleY());
      //resp.put("playerPaddleY", ((int) ((leftGame.getP2PaddleY() + rightGame.getP1PaddleY()) / 2)));
      resp.put("ballX", game.getBallX() + 200);
      resp.put("ballY", game.getBallY());
      resp.put("lose", youlose);
      resp.put("win", youwin);

      return GSON.toJson(resp);
    }
  }
}
