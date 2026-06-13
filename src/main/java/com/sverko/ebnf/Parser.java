package com.sverko.ebnf;

import com.sverko.ebnf.result.ResultNode;
import com.sverko.ebnf.result.ResultTree;
import com.sverko.ebnf.result.SimpleResultNode;
import com.sverko.ebnf.result.TriviaResultNode;
import com.sverko.ebnf.tools.ParseNodeParserFactory;
import com.sverko.ebnf.tools.StackAdapter;
import com.sverko.ebnf.tools.UnicodeString;
import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
  ParseNodeStack parseNodeStack = new ParseNodeStack();
  private final StackAdapter<ResultFrame> resultFrames = new StackAdapter<>();
  private final ResultTree resultTree = new ResultTree();
  private ResultNode lastResultStrand;
  private int triviaProbeDepth;

  static final class ResultPart {
    enum Type {
      NODE,
      TERMINAL,
      TRIVIA
    }

    final Type type;
    final ResultNode node;
    final String text;
    final String category;
    final int fromToken;
    final int toToken;

    private ResultPart(Type type, ResultNode node, String text, String category,
        int fromToken, int toToken) {
      this.type = type;
      this.node = node;
      this.text = text;
      this.category = category;
      this.fromToken = fromToken;
      this.toToken = toToken;
    }

    static ResultPart node(ResultNode node) {
      return new ResultPart(Type.NODE, node, null, null, -1, -1);
    }

    static ResultPart terminal(String text, int fromToken, int toToken) {
      return new ResultPart(Type.TERMINAL, null, text, null, fromToken, toToken);
    }

    static ResultPart trivia(String category, int fromToken, int toToken) {
      return new ResultPart(Type.TRIVIA, null, null, category, fromToken, toToken);
    }

    boolean isTerminal() {
      return type == Type.TERMINAL;
    }

    boolean isTrivia() {
      return type == Type.TRIVIA;
    }
  }

  static final class ResultFrame {
    final SimpleResultNode node;
    final List<ResultPart> parts = new ArrayList<>();

    ResultFrame(String name) {
      this.node = new SimpleResultNode(name);
    }

    void addChild(ResultNode child) {
      if (child != null) {
        parts.add(ResultPart.node(child));
      }
    }

    void addTerminal(String text, int fromToken, int toToken) {
      parts.add(ResultPart.terminal(text, fromToken, toToken));
    }

    void addTrivia(String category, int fromToken, int toToken) {
      parts.add(ResultPart.trivia(category, fromToken, toToken));
    }

    int checkpoint() {
      return parts.size();
    }

    void rollbackTo(int checkpoint) {
      while (parts.size() > checkpoint) {
        parts.remove(parts.size() - 1);
      }
    }

    ResultNode buildNode() {
      List<ResultNode> children = condenseParts();
      if (children.isEmpty()) {
        return node;
      }

      node.setDownNode(children.get(0));
      for (int i = 1; i < children.size(); i++) {
        children.get(i - 1).setRightNode(children.get(i));
      }
      return node;
    }

    private List<ResultNode> condenseParts() {
      List<ResultNode> children = new ArrayList<>();
      StringBuilder terminalText = new StringBuilder();
      int terminalFrom = -1;
      int terminalTo = -1;

      for (ResultPart part : parts) {
        if (part.isTerminal()) {
          if (terminalText.length() == 0) {
            terminalFrom = part.fromToken;
          }
          terminalText.append(part.text);
          terminalTo = part.toToken;
          continue;
        }

        flushTerminal(children, terminalText, terminalFrom, terminalTo);
        terminalFrom = -1;
        terminalTo = -1;
        if (part.isTrivia()) {
          appendTrivia(children, part);
        } else {
          children.add(part.node);
        }
      }

      flushTerminal(children, terminalText, terminalFrom, terminalTo);
      return children;
    }

    private void flushTerminal(List<ResultNode> children, StringBuilder terminalText,
        int terminalFrom, int terminalTo) {
      if (terminalText.length() == 0) {
        return;
      }
      children.add(new SimpleResultNode(terminalText.toString(), terminalFrom)
          .setSpan(terminalFrom, terminalTo));
      terminalText.setLength(0);
    }

    private void appendTrivia(List<ResultNode> children, ResultPart part) {
      if (!children.isEmpty()
          && children.get(children.size() - 1) instanceof TriviaResultNode previous
          && previous.getCategory().equals(part.category)
          && previous.getToToken() == part.fromToken) {
        previous.setSpan(previous.getFromToken(), part.toToken);
        return;
      }
      children.add(new TriviaResultNode(part.category, part.fromToken, part.toToken));
    }
  }

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
    parseNodeStack.clear();
    resultFrames.clear();
    lastResultStrand = null;
    resultTree.setRoot(null);
    resultTree.setTokenQueue(tokenQueue);
    assignParserToEachNode(startNode);
    return startNode.callReceived(tokenQueue.getFirstToken());
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
      for (Map.Entry<String, ParseNode> e : nodeMap.entrySet()) {
        e.getValue().addEventListener(listener);
      }
      return;
    }
    for (String nodeName : nodeNames) {
      assignNodeEventListener(nodeName, listener);
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
        (i) -> Character.UnicodeBlock.of(i) == UnicodeBlock.BASIC_LATIN);
    addSpecialSequence("?BASIC_LATIN_LETTER?",
        getSpecialSequence("?BASIC_LATIN?").and(Character::isLetter));
    addSpecialSequence("?CYRILLIC?", (i) -> Character.UnicodeBlock.of(i) == UnicodeBlock.CYRILLIC);
    addSpecialSequence("?EMOTICONS?",
        (i) -> Character.UnicodeBlock.of(i) == UnicodeBlock.EMOTICONS);
  }

  void enterNonTerminal(ParseNode node) {
    resultFrames.push(new ResultFrame(node.getName()));
  }

  void leaveNonTerminal(ParseNode node, boolean matched) {
    if (resultFrames.isEmpty()) {
      return;
    }

    ResultFrame completedFrame = resultFrames.pop();
    if (!matched) {
      return;
    }

    completedFrame.node.setSpan(node.frmPtr, node.toPtr);
    ResultNode completedNode = completedFrame.buildNode();
    if (resultFrames.isEmpty()) {
      lastResultStrand = completedNode;
      resultTree.setRoot(completedNode);
      return;
    }

    resultFrames.top().addChild(completedNode);
  }

  void recordTerminalMatch(int tokenIndex, String text) {
    if (resultFrames.isEmpty()) {
      return;
    }
    resultFrames.top().addTerminal(text, tokenIndex, tokenIndex + 1);
  }

  void recordTriviaMatch(int fromToken, int toToken, String category) {
    if (resultFrames.isEmpty() || toToken <= fromToken) {
      return;
    }
    resultFrames.top().addTrivia(category, fromToken, toToken);
  }

  boolean isMatchingTrivia() {
    return triviaProbeDepth > 0;
  }

  int matchTrivia(String selector, int tokenIndex) {
    ParseNode triviaNode = nodeMap.get(selector);
    if (triviaNode == null || !tokenQueue.checkIndex(tokenIndex)) {
      return ParseNode.NOT_FOUND;
    }

    int tokenCheckpoint = tokenQueue.checkpoint();
    int resultCheckpoint = checkpointResult();
    int lastTokenFound = tokenQueue.getLastTokenFound();
    triviaProbeDepth++;
    try {
      ParseNodeParserFactory.assign(triviaNode, this);
      int result = triviaNode.callReceived(tokenIndex);
      return result > tokenIndex ? result : ParseNode.NOT_FOUND;
    } finally {
      triviaProbeDepth--;
      tokenQueue.rollbackTo(tokenCheckpoint);
      tokenQueue.setLastTokenFound(lastTokenFound);
      rollbackResultTo(resultCheckpoint);
    }
  }

  int checkpointResult() {
    if (resultFrames.isEmpty()) {
      return -1;
    }
    return resultFrames.top().checkpoint();
  }

  void rollbackResultTo(int checkpoint) {
    if (checkpoint < 0 || resultFrames.isEmpty()) {
      return;
    }
    resultFrames.top().rollbackTo(checkpoint);
  }

  public ResultNode getLastResultStrand() {
    return lastResultStrand;
  }

  public ResultTree getResultTree() {
    return resultTree;
  }
}
