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
    int downNodeResult = downNode.callReceived(token);
    // left alternative failed (same rule as before)
    if (downNodeResult <= token) {
      // rollback UW changes made by the left alternative
      tokens.rollbackTo(cp);

      if (hasRightNode()) {
        return rightNode.callReceived(token);
      }
    }
    // if teh left alternative succeeded -> keep UW changes
    return downNodeResult;
  }
}
