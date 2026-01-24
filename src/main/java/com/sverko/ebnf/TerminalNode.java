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
  public int callReceived(int curPtr) {
    if (!tokens.checkIndex(curPtr)) {
      return END_OF_QUEUE;
    }
    String cur = tokens.getToken(curPtr);

    if (compareFunction.apply(cur)) {
      Token tok = tokens.getTokenObject(curPtr);
      if (tok != null && tok.getType() == TokenType.UNKNOWN) {
        tok.setType(TokenType.PAYLOAD);
      }
      return curPtr + 1;
    }
    if (cur != null && !cur.isEmpty()) {
      int cp = cur.codePointAt(0);
      if (Character.isWhitespace(cp)) {
        Token tok = tokens.getTokenObject(curPtr);
        if (tok != null && tok.getType() == TokenType.UNKNOWN) {
          tok.setType(TokenType.TRIVIA);
        }
      }
    }
    return NOT_FOUND;
  }
}
