package com.sverko.ebnf.result;

public abstract class BaseResultNode implements ResultNode {

  private static void setParent(ResultNode child, ResultNode parent) {
    if (child instanceof BaseResultNode brn) {
      brn.parent = parent;
    }
  }

  String name;
  ResultNode parent;
  ResultNode downNode;
  ResultNode rightNode;
  int foundToken = -1;
  int fromToken = -1;
  int toToken = -1;

  @Override
  public ResultNode setSpan(int fromToken, int toToken) {
    this.fromToken = fromToken;
    this.toToken = toToken;
    return this;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public ResultNode getParent() {
    return parent;
  }

  @Override
  public ResultNode getDownNode() {
    return downNode;
  }

  @Override
  public ResultNode getRightNode() {
    return rightNode;
  }

  @Override
  public int getFoundToken() {
    return foundToken;
  }

  @Override
  public int getFromToken() {
    return fromToken;
  }

  @Override
  public int getToToken() {
    return toToken;
  }

  @Override
  public boolean hasToken() {
    return foundToken != -1;
  }

  @Override
  public ResultNode setDownNode(ResultNode downNode) {
    this.downNode = downNode;
    setParent(downNode, this);
    return downNode;
  }

  @Override
  public ResultNode setRightNode(ResultNode rightNode) {
    this.rightNode = rightNode;
    setParent(rightNode, this);
    return rightNode;
  }

  @Override
  public ResultNode returnDownNode(ResultNode node) {
    this.downNode = node;
    setParent(node, this);
    return this;
  }

  @Override
  public ResultNode returnRightNode(ResultNode node) {
    this.rightNode = node;
    setParent(node, this);
    return this;
  }
}
