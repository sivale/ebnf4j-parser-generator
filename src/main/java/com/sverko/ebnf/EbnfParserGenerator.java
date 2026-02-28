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

  public Parser getShemaParser() {
    return this;
  }
  public Parser getParser(Path shemaLocation) throws IOException {
    return getParser(shemaLocation, true);
  }

  public Parser getParser(Path shemaLocation, boolean strictWhitespaceHandling) throws IOException {
    Lexer shemaLexer = new Lexer(Set.of("\\n","\\t","\\s","{:"));
    TokenQueue ebnfSchema = shemaLexer.lexText(shemaLocation);
    return getParser(ebnfSchema, strictWhitespaceHandling);
  }

  public Parser getParser(TokenQueue shema, boolean ignoreWhitespace) {
    propagateTokenQueueToAllNodes(shema);
    processEbnfSchema();
    return new Parser(getFirstNode(), parserBuilder.getNamedNodes(), parserBuilder.getLexerTokens(),
        ignoreWhitespace);
  }

  public Parser getParser(String shema) {
    Lexer shemaLexer = new Lexer(Set.of("\\n","\\t","\\s","{:"));
    TokenQueue ebnfSchema = shemaLexer.lexText(new UnicodeString(shema));
    return getParser(ebnfSchema, true);
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

  public void assignParseNodeEventListeners() {

    nodeMap = EbnfParseTree.getNodeMap();
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

    for (String node : namedNodes) {
      ParseNode n = (ParseNode) this.nodeMap.get(node);
      n.addEventListener(this.parserBuilder);
      for (ParseNodeEventListener l : extraListeners) {
        n.addEventListener(l);
      }
    }
  }
}
