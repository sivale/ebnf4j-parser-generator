package com.sverko.ebnf;

public class NonTerminalNode extends ParseNode {
  String resultString;

  public NonTerminalNode(String name) { super(name); }
  public NonTerminalNode() {}

  @Override
  public int callReceived(int token) {
    this.frmPtr = token;
    int receivedResult = this.downNode.callReceived(token);

    if (receivedResult >= 0 && receivedResult > token) {
      this.toPtr = receivedResult;

      boolean anReq = tokens.isAnRequest(token);
      if (!anReq) {
        // Raw span (includes unhandled whitespace)
        String raw = tokens.getSubstring(token, receivedResult);
        this.resultString = raw;

        // Trimmed span (excludes leading/trailing UNHANDLED whitespace)
        int trimmedFrom = token;
        while (trimmedFrom < receivedResult && tokens.isUnhandledWhitespace(trimmedFrom)) {
          trimmedFrom++;
        }

        int trimmedTo = receivedResult;
        while (trimmedTo > trimmedFrom && tokens.isUnhandledWhitespace(trimmedTo - 1)) {
          trimmedTo--;
        }

        String trimmed = (trimmedTo > trimmedFrom) ? tokens.getSubstring(trimmedFrom, trimmedTo) : "";

        fireParseNodeEvent(raw, trimmed, token, receivedResult, trimmedFrom, trimmedTo);
      }
      tokens.setLastTokenFound(receivedResult);
    }
    return receivedResult;
  }

  void fireParseNodeEvent(String raw, String trimmed,
      int rawFrom, int rawTo,
      int trimmedFrom, int trimmedTo) {
    for (ParseNodeEventListener l : listeners) {
      l.parseNodeEventOccurred(
          new ParseNodeEvent(this, raw, trimmed, rawFrom, rawTo, trimmedFrom, trimmedTo)
      );
    }
  }
}
