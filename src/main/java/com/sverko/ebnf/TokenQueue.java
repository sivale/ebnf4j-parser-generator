package com.sverko.ebnf;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TokenQueue {

  private final List<String> tokens;

  /**
   * Undo-Log: speichert Indizes, bei denen wir unhandledWhitespace von true -> false gesetzt haben.
   * Rollback setzt diese Indizes wieder auf true.
   */
  private final ArrayList<Integer> uwUndoLog = new ArrayList<>();

  public TokenQueue(List<String> tokens) {
    this.tokens = Objects.requireNonNull(tokens, "tokens must not be null");
    initWhitespaceBitsets();
  }

  // WS-characteristics (constant, dependent on token content)
  private final BitSet isWhitespace = new BitSet();
  // parser status indicates which whitespace tokens have been handled
  private final BitSet handledWhitespace = new BitSet();
  // --- Token flags moved into TokenQueue: anRequest + loopProbe ---
  private final BitSet anRequest = new BitSet();
  private final BitSet loopProbe = new BitSet();
  private int lastTokenFound = -1;

  private void initWhitespaceBitsets() {
    for (int i = 0; i < tokens.size(); i++) {
      if (isWhitespaceToken(tokens.get(i))) {
        isWhitespace.set(i);
      }
    }
    // handledWhitespace is initially empty all WS are unhandled
  }

  private static boolean isWhitespaceToken(String t) {
    if (t == null || t.isEmpty()) return false;
    for (int k = 0; k < t.length(); k++) {
      if (!Character.isWhitespace(t.charAt(k))) return false;
    }
    return true;
  }

  public boolean checkIndex(int index) {
    return index > -1 && index < tokens.size();
  }

  public int getNextToken(int index) {
    if (tokens.size() - 1 > index && index >= 0) {
      return index + 1;
    }
    return ParseNode.END_OF_QUEUE; // -2
  }

  // =========================
  // Unhandled Whitespace API
  // =========================

  public boolean isUnhandledWhitespace(int index) {
    return checkIndex(index)
        && isWhitespace.get(index)
        && !handledWhitespace.get(index);
  }

  public void handleWhitespace(int index) {
    if (!checkIndex(index)) return;
    if (!isWhitespace.get(index)) return;                 // kein WS => nix zu tun
    if (handledWhitespace.get(index)) return;             // schon handled

    handledWhitespace.set(index);
    uwUndoLog.add(index);
  }

  public void clearAllUnhandledWhitespace() {
    // alle WS werden handled
    handledWhitespace.or(isWhitespace);
    uwUndoLog.clear();
  }

  // =========================
  // Checkpoint / Rollback
  // =========================

  public int checkpoint() {
    return uwUndoLog.size();
  }

  public void rollbackTo(int checkpoint) {
    if (checkpoint < 0 || checkpoint > uwUndoLog.size()) {
      throw new IllegalArgumentException("Invalid checkpoint: " + checkpoint);
    }
    for (int i = uwUndoLog.size() - 1; i >= checkpoint; i--) {
      int tokenIndex = uwUndoLog.remove(i);
      handledWhitespace.clear(tokenIndex);
    }
  }

  // -------------------------
  // anRequest API (scoped)
  // -------------------------

  public boolean isAnRequest(int index) {
    return checkIndex(index) && anRequest.get(index);
  }

  public void setAnRequest(int index, boolean value) {
    if (!checkIndex(index)) return;
    if (value) anRequest.set(index);
    else anRequest.clear(index);
  }

  /**
   * Executes body with anRequest=true at index and restores previous value afterwards.
   * This is intended for AntiNode-style "scoped" marking.
   */

  public <T> T withAnRequest(int index, java.util.function.Supplier<T> body) {
    if (!checkIndex(index)) return body.get();
    boolean old = anRequest.get(index);
    anRequest.set(index);
    try {
      return body.get();
    } finally {
      if (old) anRequest.set(index);
      else anRequest.clear(index);
    }
  }

  public void withAnRequest(int index, Runnable body) {
    withAnRequest(index, () -> {
      body.run();
      return null;
    });
  }

  // -------------------------
  // loopProbe API (ephemeral)
  // -------------------------

  public boolean isLoopProbe(int index) {
    return checkIndex(index) && loopProbe.get(index);
  }

  public void setLoopProbe(int index, boolean value) {
    if (!checkIndex(index)) return;
    if (value) loopProbe.set(index);
    else loopProbe.clear(index);
  }


  // =========================
  // Payload-Logik
  // =========================

  // Payload = alles außer "unhandled whitespace"
  private boolean isPayloadIndex(int i) {
    return !isUnhandledWhitespace(i);
  }

  public int getPayloadIndex(int index) {
    int payloadIndex = 0;
    for (int i = 0; i < index; i++) {
      if (isPayloadIndex(i)) payloadIndex++;
    }
    return payloadIndex;
  }

  public int size() {
    int count = 0;
    for (int i = 0; i < tokens.size(); i++) {
      if (isPayloadIndex(i)) count++;
    }
    return count;
  }

  public int getLastTokenFound() {
    return lastTokenFound;
  }

  public void setLastTokenFound(int lastTokenFound) {
    this.lastTokenFound = lastTokenFound;
  }

  public static TokenQueue ofList(List<String> tokens) {
    return new TokenQueue(tokens);
  }

  public static TokenQueue ofList(String... tokens) {
    return new TokenQueue(List.of(tokens));
  }

  public TokenQueue subList(int from, int to) {
    return new TokenQueue(tokens.subList(from, to));
  }

  public IntStream indexStream() {
    return IntStream.range(0, tokens.size());
  }

  public int getFirstToken() {
    return 0;
  }

  public String getSubstring(int from, int to) {
    return String.join("", tokens.subList(from, to));
  }

  public int rawSize() {
    return tokens.size();
  }

  public String get(int index) {
    return tokens.get(index);
  }

  public boolean contains(String token) {
    return tokens.stream().anyMatch(t -> t.equals(token));
  }

  // --- Token-API ---

  public List<String> getTokens() {
    return tokens;
  }

  // =========================
  // Checkpoint / Rollback
  // =========================


  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TokenQueue other)) {
      return false;
    }
    return this.tokens.equals(other.tokens);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tokens);
  }
}