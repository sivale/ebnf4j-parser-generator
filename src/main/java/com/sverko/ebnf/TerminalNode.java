package com.sverko.ebnf;

import java.util.function.Function;

public class TerminalNode extends ParseNode {

  private final Function<String, Boolean> compareFunction;
  private final String tag;

  public TerminalNode(Function<String, Boolean> compareFunction, String tag) {
    this.compareFunction = compareFunction;
    this.tag = tag;
  }

  public TerminalNode(String name, Function<String, Boolean> compareFunction, String tag) {
    this.name = name;
    this.compareFunction = compareFunction;
    this.tag = tag;
  }

  public String getTag() {
    if (tag != null && !tag.isEmpty()) return tag;
    if (name != null && !name.isEmpty()) return name;
    return "<terminal>";
  }

  @Override
  public int callReceived(int token) {
    if (!tokens.checkIndex(token)) {
      return END_OF_QUEUE;
    }
    String cur = tokens.get(token);
    // normal match if compareFunction says "yes" then consume exactly 1 token
    if (compareFunction.apply(cur)) {
      // if token was "unhandled whitespace" then it is now "handled"
      // can be rolled back by calling tokens.rollback()
      if (tokens.isUnhandledWhitespace(token)) {
        tokens.handleWhitespace(token);
      }
      return token + 1;
    }
    return NOT_FOUND;
  }
}
