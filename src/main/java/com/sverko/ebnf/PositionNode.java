package com.sverko.ebnf;

public class PositionNode extends ParseNode {

  PositionNode() {
    super();
  }

  PositionNode(String name) {
    super(name);
  }

  @Override
  public int callReceived(int token) {
    if (token == END_OF_QUEUE) return token;

    // Allow "one past last" as a valid cursor so optional nodes can match empty at EOF
    if (token == tokens.rawSize()) {
      int downNodeResult = callDown(token);
      if (downNodeResult >= token) {
        return forwardToRightOrUp(downNodeResult);
      }
      // Typically END_OF_QUEUE or NOT_FOUND
      return downNodeResult;
    }

    if (!tokens.checkIndex(token)) return END_OF_QUEUE;

    int sent = token;

    while (true) {
      // Also allow sent == rawSize() inside the loop (can happen after pumping)
      if (sent == tokens.rawSize()) {
        int downNodeResult = callDown(sent);
        if (downNodeResult >= sent) {
          return forwardToRightOrUp(downNodeResult);
        }
        return downNodeResult;
      }

      if (!tokens.checkIndex(sent)) return END_OF_QUEUE;

      int downNodeResult = callDown(sent);

      if (downNodeResult >= sent) {
        return forwardToRightOrUp(downNodeResult);
      }

      // end of queue protection if the unhandled whitespace is the last token
      if (!tokens.checkIndex(sent + 1)) {
        return NOT_FOUND;
      }

      // jump over unhandled whitespace
      if (downNodeResult == NOT_FOUND &&
          tokens.isUnhandledWhitespace(sent) &&
          !tokens.isAnRequest(sent) &&
          !tokens.isLoopProbe(sent)
      ) {
        sent++;
        continue;
      }

      // no match no whitespace jump: return NOT_FOUND or END_OF_QUEUE
      return downNodeResult;
    }
  }

  private int forwardToRightOrUp(int token) {
    if (hasRightNode()) {
      // token may be "one past last" (rawSize) -> still let rightNode decide
      if (token == tokens.rawSize()) {
        int rightNodeResult = callRight(token);
        return rightNodeResult;
      }

      if (tokens.checkIndex(token)) {
        int rightNodeResult = callRight(token);
        if (rightNodeResult < 0 || rightNodeResult >= token) return rightNodeResult;
        return WRONG_RETURN_VALUE;
      }
      return END_OF_QUEUE;
    }
    return token;
  }
}