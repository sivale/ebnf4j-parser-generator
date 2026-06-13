package com.sverko.ebnf.result;

public interface ResultNode {
  ResultNode setDownNode(ResultNode node);
  ResultNode setRightNode(ResultNode node);
  ResultNode returnDownNode(ResultNode node);
  ResultNode returnRightNode(ResultNode node);
  ResultNode setSpan(int fromToken, int toToken);
  String getName();
  ResultNode getParent();
  ResultNode getDownNode();
  ResultNode getRightNode();
  int getFoundToken();
  int getFromToken();
  int getToToken();
  boolean hasToken();

  default ResultNodeType getType() {
    return hasToken() ? ResultNodeType.TERMINAL : ResultNodeType.NON_TERMINAL;
  }
}
