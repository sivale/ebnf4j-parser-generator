package com.sverko.ebnf;


import com.sverko.ebnf.tools.ParseNodeParserFactory;
import com.sverko.ebnf.tools.UTF8FileToStringArrayList;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

  public Parser getParser(Path shemaLocation) throws IOException {
    return getParser(shemaLocation, true);
  }

  public Parser getParser(Path shemaLocation, boolean ignoreWhitespace) throws IOException {
    Lexer shemaLexer = new Lexer();
    return getParser(
        shemaLexer.lexText(UTF8FileToStringArrayList.loadFileIntoStringList(shemaLocation)),
        ignoreWhitespace);
  }

  public Parser getParser(List<String> shema, boolean ignoreWhitespace) {
    loadEbnfSchema(shema);
    processEbnfSchema();
    return new Parser(getFirstNode(), parserBuilder.getNamedNodes(), parserBuilder.getLexerTokens(),
        ignoreWhitespace);
  }

  public void loadEbnfSchema(List<String> tokenQueue) {
    q = tokenQueue;
    ParseNodeParserFactory.assign(startNode, this);
  }

  public void processEbnfSchema() {
    addDefaultSpecialSequences();
    assignParseNodeEventListeners();
    int tokensFound = startNode.callReceived(getNextToken(false));
    if (tokensFound == q.size()) {
      NonTerminalNode endNode = new NonTerminalNode("end of parsing");
      endNode.addEventListener(parserBuilder);
      endNode.fireParseNodeEvent("end of parsing");
    } else {
      System.out.println(
          "WARNING only " + tokensFound + " of " + q.size() + " tokens have been processed");
    }
  }

  public ParseNode getFirstNode() {
    return parserBuilder.getStartNode();
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
