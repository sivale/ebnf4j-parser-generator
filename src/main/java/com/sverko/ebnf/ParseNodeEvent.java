package com.sverko.ebnf;

public class ParseNodeEvent {
  private final ParseNode node;
  private final String raw;
  private final String trimmed;
  private final int fromPtr;
  private final int toPtr;

  // New: token span for trimmed text (exclusive end)
  private final int trimmedFromPtr;
  private final int trimmedToPtr;

  public ParseNodeEvent(ParseNode node, String raw, String trimmed,
      int fromPtr, int toPtr,
      int trimmedFromPtr, int trimmedToPtr) {
    this.node = node;
    this.raw = raw;
    this.trimmed = trimmed;
    this.fromPtr = fromPtr;
    this.toPtr = toPtr;
    this.trimmedFromPtr = trimmedFromPtr;
    this.trimmedToPtr = trimmedToPtr;
  }

  public ParseNode getNode() { return node; }
  public int getFromPtr() { return fromPtr; }
  public int getToPtr() { return toPtr; }
  public String getRaw() { return raw; }
  public String getTrimmed() { return trimmed; }
  public int getTrimmedFromPtr() { return trimmedFromPtr; }
  public int getTrimmedToPtr() { return trimmedToPtr; }
}
