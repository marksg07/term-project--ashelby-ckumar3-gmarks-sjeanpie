package edu.brown.cs.pong;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;

public class PongGame implements Cloneable {
  private double ballX, ballY, ballVelX, ballVelY;
  private double p1PaddleY, p2PaddleY;
  private double maxX, maxY;
  private double paddleVel, paddleRadius;
  private double ballRadius; // NOTE: We call it a "ball" but it's gonna be a square
  private double startVel;
  private Instant lastUpdate;
  private boolean p1Dead, p2Dead;

  public enum InputType {
    NONE,
    UP,
    DOWN;

    public static InputType fromInt (int t) {
      switch (t) {
        case -1:
          return DOWN;
        case 0:
          return NONE;
        case 1:
          return UP;
      }
      return null;
    }
  }

  InputType p1Input, p2Input;

  public PongGame(double sizeX, double sizeY, double paddleVel, double paddleLen, double ballRadius, double startVel) {
    maxX = sizeX;
    maxY = sizeY;
    ballX = maxX / 2;
    ballY = maxY / 2;
    double initDirection = Math.random() * Math.PI * 2;
    if ((initDirection + Math.PI / 4) % Math.PI >= Math.PI / 2) {
      initDirection += Math.PI / 2;
    }
    ballVelX = startVel * Math.cos(initDirection);
    ballVelY = startVel * Math.sin(initDirection);
    p1PaddleY = maxY / 2;
    p2PaddleY = maxY / 2;
    this.paddleVel = paddleVel;
    paddleRadius = paddleLen / 2;
    p1Input = InputType.NONE;
    p2Input = InputType.NONE;
    this.ballRadius = ballRadius;
    this.startVel = startVel;
    p1Dead = false;
    p2Dead = false;
    lastUpdate = Instant.now();
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  private enum CollisionType {
    NONE,
    UP,
    DOWN,
    LEFT,
    RIGHT
  }

  public int tickToCurrent() {
    synchronized (lastUpdate) {
      Instant now = Instant.now();
      double seconds = Duration.between(lastUpdate, now).toNanos() / 1000000000.;
      lastUpdate = now;
      return tick(seconds);
    }
  }

  private class Collision {
    public double time;
    public CollisionType type;
  }

  public int tick(double seconds) {
    assert (ballX >= ballRadius && ballX <= maxX - ballRadius
            && ballY >= ballRadius && ballY <= maxY - ballRadius);
    if(!(p1Dead || p2Dead)) {
      Collision collision = getNextCollision(seconds);
      while (collision.type != CollisionType.NONE) {
        seconds -= collision.time;
        assert (seconds >= 0);
        assert (collision.time >= 0);
        // First, move paddles to where they should be when collision occurs
        movePaddles(collision.time);

        // now handle each type of collision
        switch (collision.type) {
          case UP:
            ballY = ballRadius;
            ballX += ballVelX * collision.time;
            ballVelY = -ballVelY; // XXX add randomness
            break;
          case DOWN:
            ballY = maxY - ballRadius;
            ballX += ballVelX * collision.time;
            ballVelY = -ballVelY;
            break;
          case LEFT:
            ballX = ballRadius;
            ballY += ballVelY * collision.time;
            ballVelX = -ballVelX;
            // check if we hit the paddle. If not, ded
            if (ballY + ballRadius < p1PaddleY - paddleRadius
                    || ballY - ballRadius > p1PaddleY + paddleRadius) {
              p1Dead = true;
              return 2;
            }
            break;
          case RIGHT:
            ballX = maxX - ballRadius;
            ballY += ballVelY * collision.time;
            ballVelX = -ballVelX;
            // check if we hit the paddle. If not, ded
            if (ballY + ballRadius < p2PaddleY - paddleRadius
                    || ballY - ballRadius > p2PaddleY + paddleRadius) {
              p2Dead = true;
              return 1;
            }
            break;
            
          default:
            assert (false);
        }

        addRandomness(0.2);

        // once handled, we repeat the collision test with updated X, Y, vX, vY, pX, pY
        collision = getNextCollision(seconds);
      }

      // done solving collisions
      ballX = ballX + ballVelX * seconds;
      ballY = ballY + ballVelY * seconds;
    }
    // do paddle movements, then done
    movePaddles(seconds);
    if (p1Dead)
      return 2;
    if (p2Dead)
      return 1;
    return 0;
  }

  private void addRandomness(double frac) {
    boolean invalid = true;
    double newVelX = 0, newVelY = 0;
    while (invalid) {
      double initDirection = Math.random() * Math.PI * 2;
      newVelX = ballVelX + startVel * frac * Math.cos(initDirection);
      newVelY = ballVelY + startVel * frac * Math.sin(initDirection);
      invalid = false;
      if((newVelX > 0) != (ballVelX > 0) || (newVelY > 0) != (ballVelY > 0)) {
        invalid = true;
      } else if (newVelX * newVelX + newVelY * newVelY < (startVel * startVel) * 64./81
      || newVelX * newVelX + newVelY * newVelY > (startVel * startVel) * 100./81) {
        invalid = true;
      } else if (Math.abs(newVelX) < startVel / 3.) {
        invalid = true;
      }
    }
    ballVelX = newVelX;
    ballVelY = newVelY;
  }

  private void movePaddles(double time) {
    if (!p1Dead) {
      if (p1Input == InputType.DOWN) {
        p1PaddleY += time * paddleVel;
        if (p1PaddleY > maxY)
          p1PaddleY = maxY;
      } else if (p1Input == InputType.UP) {
        p1PaddleY -= time * paddleVel;
        if (p1PaddleY < 0)
          p1PaddleY = 0;
      }
    }

    if(!p2Dead) {
      if (p2Input == InputType.DOWN) {
        p2PaddleY += time * paddleVel;
        if (p2PaddleY > maxY)
          p2PaddleY = maxY;
      } else if (p2Input == InputType.UP) {
        p2PaddleY -= time * paddleVel;
        if (p2PaddleY < 0)
          p2PaddleY = 0;
      }
    }
  }

  private Collision getNextCollision(double time) {
    double newBallX = ballX + ballVelX * time;
    double newBallY = ballY + ballVelY * time;
    CollisionType type = CollisionType.NONE;
    double timeDelta = -1;
    if (newBallX < ballRadius) {
      type = CollisionType.LEFT;
      // ballRad = ballX + ballVelX * dT
      timeDelta = (ballRadius - ballX) / ballVelX;
    } else if (newBallX > maxX - ballRadius) {
      type = CollisionType.RIGHT;
      // maxX - ballRad = ballX + ballVelX * dT
      timeDelta = (maxX - ballRadius - ballX) / ballVelX;
    }

    if (newBallY < ballRadius) {
      // ballRad = ballY + ballVelY * dT
      double dT = (ballRadius - ballY) / ballVelY;
      if (type == CollisionType.NONE || dT < timeDelta) { // this collides first
        type = CollisionType.UP;
        timeDelta = dT;
      }
    } else if (newBallY > maxY - ballRadius) {
      // maxY - ballRad = ballY + ballVelY * dT
      double dT = (maxY - ballRadius - ballY) / ballVelY;
      if (type == CollisionType.NONE || dT < timeDelta) { // this collides first
        type = CollisionType.DOWN;
        timeDelta = dT;
      }
    }
    Collision collision = new Collision();
    collision.time = timeDelta;
    collision.type = type;
    return collision;
  }

  /**
   * Gets ballX.
   *
   * @return Value of ballX.
   */
  public double getBallX() {
    return ballX;
  }

  /**
   * Gets ballVelX.
   *
   * @return Value of ballVelX.
   */
  public double getBallVelX() {
    return ballVelX;
  }

  /**
   * Gets maxY.
   *
   * @return Value of maxY.
   */
  public double getMaxY() {
    return maxY;
  }

  /**
   * Gets ballY.
   *
   * @return Value of ballY.
   */
  public double getBallY() {
    return ballY;
  }

  /**
   * Gets ballVelY.
   *
   * @return Value of ballVelY.
   */
  public double getBallVelY() {
    return ballVelY;
  }

  /**
   * Gets p2Input.
   *
   * @return Value of p2Input.
   */
  public InputType getP2Input() {
    return p2Input;
  }

  /**
   * Gets ballRadius.
   *
   * @return Value of ballRadius.
   */
  public double getBallRadius() {
    return ballRadius;
  }

  /**
   * Gets p2PaddleY.
   *
   * @return Value of p2PaddleY.
   */
  public double getP2PaddleY() {
    return p2PaddleY;
  }

  /**
   * Gets p1Input.
   *
   * @return Value of p1Input.
   */
  public InputType getP1Input() {
    return p1Input;
  }

  /**
   * Gets paddleRadius.
   *
   * @return Value of paddleRadius.
   */
  public double getPaddleRadius() {
    return paddleRadius;
  }

  /**
   * Gets maxX.
   *
   * @return Value of maxX.
   */
  public double getMaxX() {
    return maxX;
  }

  /**
   * Gets p1PaddleY.
   *
   * @return Value of p1PaddleY.
   */
  public double getP1PaddleY() {
    return p1PaddleY;
  }

  /**
   * Gets paddleVel.
   *
   * @return Value of paddleVel.
   */
  public double getPaddleVel() {
    return paddleVel;
  }

  /**
   * Sets new p1Input.
   *
   * @param p1Input New value of p1Input.
   */
  public void setP1Input(InputType p1Input) {
    this.p1Input = p1Input;
  }

  /**
   * Sets new p2Input.
   *
   * @param p2Input New value of p2Input.
   */
  public void setP2Input(InputType p2Input) {
    this.p2Input = p2Input;
  }

  public JsonObject getState() {
    JsonObject obj = new JsonObject();
    obj.addProperty("ballX", ballX);
    obj.addProperty("ballY", ballY);
    obj.addProperty("ballVelX", ballVelX);
    obj.addProperty("ballVelY", ballVelY);
    obj.addProperty("p1PaddleY", p1PaddleY);
    obj.addProperty("p2PaddleY", p2PaddleY);
    obj.addProperty("p1Dead", p1Dead);
    obj.addProperty("p2Dead", p2Dead);
    return obj;
  }

  /**
   * Gets p2Dead.
   *
   * @return Value of p2Dead.
   */
  public boolean isP2Dead() {
    return p2Dead;
  }

  /**
   * Gets p1Dead.
   *
   * @return Value of p1Dead.
   */
  public boolean isP1Dead() {
    return p1Dead;
  }
}
