package com.sverko.ebnf;

import com.sverko.ebnf.tools.ParseNodeParserFactory;
import com.sverko.ebnf.tools.UnicodeString;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class Parser {

  TokenQueue tokenQueue;
  Lexer lexer;
  int token = -1; //before the first element in queue
  public ParseNode startNode;
  Map<String, ParseNode> nodeMap;
  Map<String, Predicate<Integer>> specialSequences = new HashMap<>();
  List<String> eventNodeNames = new ArrayList<>();

  public Parser() {
  }

  public Parser(ParseNode startNode, Map<String, ParseNode> namedNodes, Set<String> lexerTokens,
      boolean strictWhitespaceHandling) {
    this.startNode = startNode;
    this.nodeMap = namedNodes;
    this.lexer = new Lexer(lexerTokens);
  }

  public int parse(TokenQueue q, ParseNode startNode) {
    tokenQueue = q;
    this.startNode = startNode;
    return parse(startNode);
  }

  public int parse(TokenQueue q) {
    return parse(q, startNode);
  }

  public int parse(String text) {
    return parse(lexer.lexText(text), startNode);
  }

  public int parse(UnicodeString text) {
    return parse(lexer.lexText(text), startNode);
  }

  public int parse(Path textLocation) throws IOException {
    return parse(lexer.lexText(Files.readString(textLocation, StandardCharsets.UTF_8)), startNode);
  }

  private int parse(ParseNode startNode) {
    assignParserToEachNode(startNode);
    return (startNode.callReceived(tokenQueue.getFirstToken()));
  }

  public TokenQueue getTokenQueue() {
    return tokenQueue;
  }

  public Lexer getLexer() {
    return lexer;
  }

  public void assignParserToEachNode(ParseNode startNode) {
    ParseNodeParserFactory.assign(startNode, this);
  }

  public void assignNodeEventListener(String nodeName, ParseNodeEventListener listener) {
    nodeMap.get(nodeName).addEventListener(listener);
  }

  public void assignNodeEventListeners(ParseNodeEventListener listener, String... nodeNames) {
    if (nodeNames == null || nodeNames.length == 0) {
      // no explicit node names, so assign to all nodes
      for (Map.Entry<String, ParseNode> e : nodeMap.entrySet()) {
        e.getValue().addEventListener(listener);
      }
      return;
    }
    for (String nodeName : nodeNames) {
      assignNodeEventListener(nodeName, listener);
    }
  }

  public void setAcceptsWhitespace(boolean acceptsWhitespace, ParseNode parentNode) {

    parentNode.acceptsWhitespace = acceptsWhitespace;
    if (parentNode.hasDownNode()) {
      setAcceptsWhitespace(acceptsWhitespace, parentNode.downNode);
    }
    if (parentNode.hasRightNode()) {
      setAcceptsWhitespace(acceptsWhitespace, parentNode.rightNode);
    }
  }

  public void addSpecialSequence(String key, Predicate<Integer> value) {
    specialSequences.put(key, value);
  }

  public Predicate<Integer> getSpecialSequence(String key) {
    return specialSequences.get(key);
  }

  protected void addDefaultSpecialSequences() {
    addSpecialSequence("?WHITESPACE?", Character::isWhitespace);
    addSpecialSequence("?BMP?", Character::isBmpCodePoint);
    addSpecialSequence("?BMP_WITHOUT_NL?", (i) -> Character.isBmpCodePoint(i) && i != '\n');
    addSpecialSequence("?DIGIT?", Character::isDigit);
    addSpecialSequence("?LETTER?", Character::isLetter);
    addSpecialSequence("?LOWERCASE?", Character::isLowerCase);
    addSpecialSequence("?UPPERCASE?", Character::isUpperCase);
    addSpecialSequence("?BASIC_LATIN?",
        (i) -> Character.UnicodeBlock.of(i) == UnicodeBlock.BASIC_LATIN); //aka ASCII
    addSpecialSequence("?BASIC_LATIN_LETTER?",
        getSpecialSequence("?BASIC_LATIN?").and(Character::isLetter));
    addSpecialSequence("?CYRILLIC?", (i) -> Character.UnicodeBlock.of(i) == UnicodeBlock.CYRILLIC);
    addSpecialSequence("?EMOTICONS?",
        (i) -> Character.UnicodeBlock.of(i) == UnicodeBlock.EMOTICONS);
  }
}
