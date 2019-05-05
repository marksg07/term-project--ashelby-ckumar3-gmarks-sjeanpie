package edu.brown.cs.pong;

/**
 * A class that wraps a long for synchronization on changing the long.
 */
public class LongWrapper {
  private long value;

  /**
   * A constructor for the wrapper.
   * @param l the long
   */
  public LongWrapper(long l) {
    value = l;
  }

  /**
   * Gets the value of the long.
   * @return the long
   */
  public long getValue() {
    return value;
  }

  /**
   * Sets value of long.
   * @param l the value to set it equal to
   */
  public void setValue(long l) {
    value = l;
  }
}
