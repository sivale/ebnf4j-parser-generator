package com.sverko.ebnf;

public class OrNode extends ParseNode {

  OrNode() {
    super();
  }

  OrNode(String name) {
    super(name);
  }

  @Override
  public int callReceived(int token) {
    // Save whitespace-state before trying the left alternative
    int cp = tokens.checkpoint();
    int resultCp = (parser != null) ? parser.checkpointResult() : -1;
    int downNodeResult = callDown(token);
    // left alternative failed (same rule as before)
    if (downNodeResult <= token) {
      // rollback UW changes made by the left alternative
      tokens.rollbackTo(cp);
      if (parser != null) {
        parser.rollbackResultTo(resultCp);
      }

      if (hasRightNode()) {
        return callRight(token);
      }
    }
    // if teh left alternative succeeded -> keep UW changes
    return downNodeResult;
  }
}
