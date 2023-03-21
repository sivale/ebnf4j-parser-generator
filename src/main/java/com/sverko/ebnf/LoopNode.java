package com.sverko.ebnf;


import com.sverko.ebnf.tools.HighestNumberKeeper;

public class LoopNode extends ParseNode {

  int min = 0, max = 0;

  public LoopNode(int min, int max) {
    this.min = min;
    this.max = max;
  }

  public LoopNode(int max) {
    this.min = 0;
    this.max = max;
  }

  public LoopNode() {
  }

  @Override
  public int callReceived(int currentToken) {
    if (min == 0) {
      return callForRepeatAndMax(currentToken);
    } else {
      return callForMinSet(currentToken);
    }
  }

  private int callForRepeatAndMax(int currentToken) {
    HighestNumberKeeper<Integer> foundTokens = new HighestNumberKeeper<>(0);
    int tkn = currentToken;
    while (true) {
      int curResult = downNode.callReceived(tkn);
      if (curResult < 0) {
        return Math.max(foundTokens.getVal(), currentToken);
      } else if (curResult == tkn) {
        return curResult;
      } else {
        if (curResult == currentToken + max) {
          return curResult;
        }
        foundTokens.setVal(curResult);
        tkn = tokens.getNextToken(curResult - 1);
      }
    }
  }

  private int callForMinSet(int currentToken) {
    HighestNumberKeeper<Integer> foundTokens = new HighestNumberKeeper<>(0);
    int tkn = currentToken;
    while (true) {
      int curResult = downNode.callReceived(tkn);
      if (curResult == currentToken) {
        return curResult;
      }
      if (curResult < 0) {
        if (tkn < min) {
          return PositionNode.NOT_FOUND;
        } else {
          return Math.max(foundTokens.getVal(), currentToken);
        }
      } else {
        if (curResult == max) {
          return curResult;
        }
        foundTokens.setVal(curResult);
        tkn = tokens.getNextToken(tkn);
      }
    }
  }

  @Override
  public boolean equals(Object o) {
		if (!(o instanceof LoopNode)) {
			return false;
		}
    LoopNode otherNode = (LoopNode) o;
    return this.max == otherNode.max && this.min == otherNode.min;
  }

}