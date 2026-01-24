package com.sverko.ebnf;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TokenQueue {
  private final List<Token> tokens;

  public TokenQueue(List<Token> tokens) {
    this.tokens = Objects.requireNonNull(tokens, "tokens must not be null");
  }

  // --- Adapter für bestehenden Code, falls noch irgendwo List<String> reinkommt ---
  public static TokenQueue ofStrings(List<String> tokens) {
    return new TokenQueue(toTokenList(tokens));
  }

  public static List<Token> toTokenList(List<String> tokens) {
    return tokens.stream().map(Token::new).collect(Collectors.toList());
  }

  public int getFirstToken() {
    return 0;
  }

  public int getNextToken(int index) {
    if (tokens.size() - 1 > index && index >= 0) {
      return index + 1;
    } else {
      return ParseNode.END_OF_QUEUE;
    }
  }

  public boolean checkIndex(int index) {
    return index > -1 && index < tokens.size();
  }

  // --- String-API bleibt exakt erhalten ---
  public List<String> getTokens() {
    return tokens.stream().map(Token::getContent).collect(Collectors.toList());
  }

  public String getToken(int index) {
    return tokens.get(index).getContent();
  }

  public String getSubstring(int from, int to) {
    return tokens.subList(from, to).stream()
        .map(Token::getContent)
        .collect(Collectors.joining());
  }

  public int size() {
    return tokens.size();
  }

  public String get(int index) {
    return tokens.get(index).getContent();
  }

  public boolean contains(String token) {
    return tokens.stream().anyMatch(t -> t.getContent().equals(token));
  }

  // --- Neue API für Phase 2 (optional, stört keinen) ---
  public Token getTokenObject(int index) {
    return tokens.get(index);
  }

  public List<Token> getTokenObjects() {
    return tokens;
  }

  public void setTokenType(int index, TokenType type) {
    tokens.get(index).setType(type);
  }

  public static TokenQueue ofList(List<String> tokens) {
    // behält Signatur wie vorher, leitet intern weiter
    return ofStrings(tokens);
  }

  public static TokenQueue ofList(String... tokens) {
    return ofStrings(List.of(tokens));
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TokenQueue other)) return false;
    // wie früher: inhaltlich vergleichen (String contents)
    return this.getTokens().equals(other.getTokens());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getTokens());
  }
}
