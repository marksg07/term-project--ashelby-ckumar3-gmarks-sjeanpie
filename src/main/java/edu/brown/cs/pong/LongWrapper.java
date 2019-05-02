package edu.brown.cs.pong;

public class LongWrapper {
  private long _l;
  public LongWrapper(long l) {
    _l = l;
  }

  public long getValue() {
    return _l;
  }

  public void setValue(long l) {
    _l = l;
  }
}
