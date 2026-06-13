package com.sverko.ebnf.result;

public class SimpleResultNode extends BaseResultNode {

  public SimpleResultNode(String name) {
    this.name = name;
  }

  public SimpleResultNode(String name, int foundToken) {
    this.name = name;
    this.foundToken = foundToken;
  }
}
