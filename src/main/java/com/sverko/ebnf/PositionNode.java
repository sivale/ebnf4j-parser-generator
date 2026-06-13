package com.sverko.ebnf;

public class PositionNode extends ParseNode {

  private boolean triviaConfigured;
  private String triviaSelector;

  PositionNode() {
    super();
  }

  PositionNode(String name) {
    super(name);
  }

  PositionNode setTriviaSelector(String triviaSelector) {
    this.triviaConfigured = true;
    this.triviaSelector = triviaSelector;
    return this;
  }

  PositionNode disableTrivia() {
    this.triviaConfigured = true;
    this.triviaSelector = null;
    return this;
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
    int positionResultCp = (parser != null) ? parser.checkpointResult() : -1;

    while (true) {
      // Also allow sent == rawSize() inside the loop (can happen after pumping)
      if (sent == tokens.rawSize()) {
        int downNodeResult = callDown(sent);
        if (downNodeResult >= sent) {
          int forwarded = forwardToRightOrUp(downNodeResult);
          if (forwarded < 0 && parser != null) {
            parser.rollbackResultTo(positionResultCp);
          }
          return forwarded;
        }
        if (parser != null) {
          parser.rollbackResultTo(positionResultCp);
        }
        return downNodeResult;
      }

      if (!tokens.checkIndex(sent)) {
        if (parser != null) {
          parser.rollbackResultTo(positionResultCp);
        }
        return END_OF_QUEUE;
      }

      int resultCp = (parser != null) ? parser.checkpointResult() : -1;
      int downNodeResult = callDown(sent);

      if (downNodeResult >= sent) {
        int forwarded = forwardToRightOrUp(downNodeResult);
        if (forwarded < 0 && parser != null) {
          parser.rollbackResultTo(positionResultCp);
        }
        return forwarded;
      }

      if (parser != null) {
        parser.rollbackResultTo(resultCp);
      }

      if (downNodeResult == NOT_FOUND
          && !tokens.isAnRequest(sent)
          && !tokens.isLoopProbe(sent)
          && (parser == null || !parser.isMatchingTrivia())) {
        if (triviaConfigured && triviaSelector != null && parser != null) {
          int triviaResult = parser.matchTrivia(triviaSelector, sent);
          if (triviaResult > sent) {
            parser.recordTriviaMatch(sent, triviaResult, triviaSelector);
            sent = triviaResult;
            continue;
          }
        } else if (!triviaConfigured
            && tokens.isUnhandledWhitespace(sent)
            && tokens.checkIndex(sent + 1)) {
          sent++;
          continue;
        }
      }

      // no match no whitespace jump: return NOT_FOUND or END_OF_QUEUE
      if (parser != null) {
        parser.rollbackResultTo(positionResultCp);
      }
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
