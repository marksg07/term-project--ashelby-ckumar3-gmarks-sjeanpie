package edu.brown.cs.Physics;

import edu.brown.cs.pong.PongGame;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PhysicsTest {

  @Test
  public void testInitialYRandomness() {
    for (int i = 0; i < 10000; i++) {
      PongGame game = new PongGame(400, 300, 400, 40, 10, 200, 5, true);
      game.tick(0.5);
      double lowest = 0 * Math.PI * 2 / 3 - Math.PI / 3;
      double highest = 1 * Math.PI * 2 / 3 - Math.PI / 3;
      double ballVelY1 = 200 * Math.sin(lowest);
      double ballVelY2 = 200 * Math.sin(highest);
      assertTrue(game.getBallY() >= 150 + (ballVelY1 * .5));
      assertTrue(game.getBallY() <= 150 + (ballVelY2 * .5));
    }
  }

  @Test
  public void testInitialXRandomness() {
    for (int i = 0; i < 10000; i++) {
      PongGame game = new PongGame(400, 300, 400, 40, 10, 200, 5, true);
      game.tick(0.5);
      double lowest = 0 * Math.PI * 2 / 3 - Math.PI / 3;
      double ballVelX1 = 200 * Math.cos(lowest);
      double ballVelX2 = 200 * Math.cos(0);
      assertTrue(game.getBallX() >= 200 + (ballVelX1 * .5));
      assertTrue(game.getBallX() <= 200 + (ballVelX2 * .5));
    }
  }
}
