package com.sverko.ebnf;

import com.sverko.ebnf.tools.ParseNodeParserFactory;
import com.sverko.ebnf.tools.UnicodeString;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class EbnfParserGenerator extends Parser {

  ParseTreeBuilder parserBuilder = new ParseTreeBuilder(this);
  private final List<ParseNodeEventListener> extraListeners = new ArrayList<>();

  public void addSchemaListener(ParseNodeEventListener listener) {
    if (listener != null) {
      extraListeners.add(listener);
    }
  }

  public void addSchemaListeners(Collection<? extends ParseNodeEventListener> listeners) {
    if (listeners != null) {
      extraListeners.addAll(listeners);
    }
  }

  public EbnfParserGenerator() {
    startNode = EbnfParseTree.getStartNode();
  }

  public Parser getSchemaParser() {
    return this;
  }
  public Parser getParser(Path schemaLocation) throws IOException {
    return getParser(schemaLocation, true);
  }

  public Parser getParser(Path schemaLocation, boolean strictWhitespaceHandling) throws IOException {
    Lexer schemaLexer = new Lexer(Set.of("\\n","\\t","\\s","{:"));
    TokenQueue ebnfSchema = schemaLexer.lexText(schemaLocation);
    return getParser(ebnfSchema, strictWhitespaceHandling);
  }

  public Parser getParser(TokenQueue schema, boolean strictWhitespaceHandling) {
    propagateTokenQueueToAllNodes(schema);
    processEbnfSchema();
    return new Parser(getFirstNode(), parserBuilder.getNamedNodes(), parserBuilder.getLexerTokens(),
        strictWhitespaceHandling);
  }

  public Parser getParser(String schema) {
    return getParser(new UnicodeString(schema), true);
  }

  public Parser getParser(String schema, boolean strictWhitespaceHandling) {
    return getParser(new UnicodeString(schema), strictWhitespaceHandling);
  }

  public Parser getParser(UnicodeString schema) {
    return getParser(schema, true);
  }

  public Parser getParser(UnicodeString schema, boolean strictWhitespaceHandling) {
    Lexer schemaLexer = new Lexer(Set.of("\\n","\\t","\\s","{:"));
    TokenQueue ebnfSchema = schemaLexer.lexText(schema);
    return getParser(ebnfSchema, strictWhitespaceHandling);
  }

  public void propagateTokenQueueToAllNodes(TokenQueue tokenQueue) {
    this.tokenQueue = tokenQueue;
    ParseNodeParserFactory.assign(startNode, this);
  }

  public void processEbnfSchema() {
    addDefaultSpecialSequences();
    assignParseNodeEventListeners();
    int tokensFound = startNode.callReceived(tokenQueue.getFirstToken());
    while (tokensFound < tokenQueue.rawSize() && tokenQueue.isUnhandledWhitespace(tokensFound)) {
      tokensFound++;
    }
    if (tokensFound == tokenQueue.rawSize()) {
      NonTerminalNode endNode = new NonTerminalNode("end of parsing");
      endNode.addEventListener(parserBuilder);
      endNode.fireParseNodeEvent(" end of parsing ","end of parsing",0,0,0,0);
    } else {
      System.out.println(
          "WARNING only " + tokensFound + " of " + tokenQueue.rawSize() + " tokens have been processed");
    }
  }

  public ParseNode getFirstNode() {
    return parserBuilder.getStartNode();
  }

  public List<String> getPredefinedNodeNames() {
    return new ArrayList<>(parserBuilder.getNamedNodes().keySet());
  }

  public int parse(String schema, String input, boolean strictWhitespaceHandling) {
    Parser parser = getParser(schema, strictWhitespaceHandling);
    return parser.parse(input);
  }

  public int parse(String schema, String input) {
    return parse(schema, input, true);
  }

  public int parse(UnicodeString schema, UnicodeString input, boolean strictWhitespaceHandling) {
    Parser parser = getParser(schema, strictWhitespaceHandling);
    return parser.parse(input);
  }

  public int parse(UnicodeString schema, UnicodeString input) {
    return parse(schema, input, true);
  }

  public List<String> getDefaultEventEmittingNodes() {
    List<String> namedNodes = new ArrayList<>();
    namedNodes.add("meta identifier");
    namedNodes.add("defining symbol");
    namedNodes.add("concatenate symbol");
    namedNodes.add("definition separator symbol");
    namedNodes.add("terminal string");
    namedNodes.add("start option symbol");
    namedNodes.add("end option symbol");
    namedNodes.add("start group symbol");
    namedNodes.add("end group symbol");
    namedNodes.add("start repeat symbol");
    namedNodes.add("end repeat symbol");
    namedNodes.add("start collect symbol");
    namedNodes.add("end collect symbol");
    namedNodes.add("terminator symbol");
    namedNodes.add("special sequence");
    namedNodes.add("except symbol");
    namedNodes.add("integer");
    namedNodes.add("repetition symbol");
    return namedNodes;
  }
  public void assignParseNodeEventListeners() {

    nodeMap = EbnfParseTree.getNodeMap();
    List<String> namedNodes = getDefaultEventEmittingNodes();
    for (String node : namedNodes) {
      ParseNode n = (ParseNode) this.nodeMap.get(node);
      n.addEventListener(this.parserBuilder);
      for (ParseNodeEventListener l : extraListeners) {
        n.addEventListener(l);
      }
    }
  }
}
