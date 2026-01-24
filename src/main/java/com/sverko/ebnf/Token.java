package com.sverko.ebnf;

public class Token {

  private final String content;
  private TokenType type;

  public Token(String content) {
    this.content = content;
    this.type = TokenType.UNKNOWN;
  }

  public Token(String content, TokenType type) {
    this.content = content;
    this.type = type;
  }

  public String getContent() {
    return content;
  }

  public TokenType getType() {
    return type;
  }

  public void setType(TokenType type) {
    this.type = type;
  }
}