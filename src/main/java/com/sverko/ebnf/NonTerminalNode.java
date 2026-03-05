package com.sverko.ebnf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NonTerminalNode extends ParseNode {

  private static final Logger log = LoggerFactory.getLogger(NonTerminalNode.class);
  String resultString;

  public NonTerminalNode(String name) { super(name); }
  public NonTerminalNode() {}

  @Override
  public int callReceived(int token) {
  if(!tokens.get(token).isEmpty() && !Character.isWhitespace(tokens.get(token).charAt(0))) {
    log.debug("{} entered with {}", name,tokens.get(token));
  }

    this.frmPtr = token;
    int receivedResult = this.callDown(token);

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
    } else {
      if (!tokens.get(token).isEmpty() && !Character.isWhitespace(tokens.get(token).charAt(0))) {
        log.debug("{} did not receive: {}",name, tokens.get(token));
      }
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
