package com.sverko.ebnf;

public class Token {

  private final String content;

  public Token(String content) {
    this.content = content;
  }

  public Token(String content, boolean unhandledWhitespace) {
    this.content = content;
  }
  public String getContent() {
    return content;
  }
}