package edu.brown.cs.pong;

import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LongWrapper that = (LongWrapper) o;
    return value == that.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return "LongWrapper{" +
            "value=" + value +
            '}';
  }
}
