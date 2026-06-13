package com.sverko.ebnf;

public class AntiNode extends ParseNode {

  private final boolean strictWhitespaceHandling;

  public AntiNode() {
    this(false);
  }

  public AntiNode(boolean strictWhitespaceHandling) {
    this.strictWhitespaceHandling = strictWhitespaceHandling;
  }

  @Override
  public int callReceived(int token) {
    if (token == END_OF_QUEUE) return token;
    if (!tokens.checkIndex(token)) return END_OF_QUEUE;

    int sent = token;

    while (true) {
      if (!tokens.checkIndex(sent)) return END_OF_QUEUE;

      final int sentFinal = sent;

      int result = tokens.withAnRequest(sentFinal, () -> {
        // 1) Anti-check
        int antiCp = (parser != null) ? parser.checkpointResult() : -1;
        int downNodeResult = callDown(sentFinal);
        if (parser != null) {
          parser.rollbackResultTo(antiCp);
        }
        if (downNodeResult >= sentFinal) {
          return NOT_FOUND;
        }

        // 2) Continue right (PN must not pump due to anRequest)
        int rightResult;
        if (hasRightNode()) {
          int rightCp = (parser != null) ? parser.checkpointResult() : -1;
          rightResult = callRight(sentFinal);
          if (rightResult == NOT_FOUND || rightResult == END_OF_QUEUE || rightResult == Integer.MIN_VALUE) {
            if (parser != null) {
              parser.rollbackResultTo(rightCp);
            }
          }
        } else {
          rightResult = sentFinal + 1;
        }

        if (rightResult != NOT_FOUND && rightResult != END_OF_QUEUE) {
          return rightResult;
        }

        // 3) If right fails and we stand on UW: AntiNode pumps itself
        if (tokens.isUnhandledWhitespace(sentFinal)) {
          if (strictWhitespaceHandling) {
            return NOT_FOUND;
          }
          return Integer.MIN_VALUE; // sentinel: "continue with sent++"
        }

        return rightResult;
      });

      if (result == Integer.MIN_VALUE) {
        sent++;
        continue;
      }
      return result;
    }
  }
}
