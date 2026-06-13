package com.sverko.ebnf;

import com.sverko.ebnf.tools.TerminalNodeFactory;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class LoopNode extends ParseNode {
  public enum LoopType { COLLECTOR, STRUCTURAL }
  LoopType loopType = LoopType.STRUCTURAL;
  int min = 0, max = 0;
  ParseNode bouncerParseNode;
  ParseNode kickoutParseNode;
  boolean collectEdgeTrivia;
  boolean collectorScanner;
  boolean triviaConfigured;
  String triviaSelector;

  public LoopNode(int min, int max) {
    this.min = min;
    this.max = max;
    loopType = LoopType.COLLECTOR;
    initBouncerParseNode();
  }

  public LoopNode(int max) {
    this.min = 0;
    this.max = max;
    loopType = LoopType.COLLECTOR;
    initBouncerParseNode();
  }

  public LoopNode() {
    initBouncerParseNode();
  }

  public ParseNode getBouncerParseNode() {
    return bouncerParseNode;
  }

  public LoopNode setBouncerParseNode(ParseNode bouncerParseNode) {
    this.bouncerParseNode = bouncerParseNode;
    return this;
  }

  public ParseNode getKickoutParseNode() {
    return kickoutParseNode;
  }

  public LoopNode setKickoutParseNode(ParseNode kickoutParseNode) {
    this.kickoutParseNode = kickoutParseNode;
    return this;
  }

  public LoopNode setCollectEdgeTrivia(boolean collectEdgeTrivia) {
    this.collectEdgeTrivia = collectEdgeTrivia;
    return this;
  }

  public LoopNode setCollectorScanner(boolean collectorScanner) {
    this.collectorScanner = collectorScanner;
    return this;
  }

  boolean isCollectorScanner() {
    return collectorScanner;
  }

  LoopNode setTriviaSelector(String triviaSelector) {
    this.triviaConfigured = true;
    this.triviaSelector = triviaSelector;
    return this;
  }

  LoopNode disableTrivia() {
    this.triviaConfigured = true;
    this.triviaSelector = null;
    return this;
  }

  private void initBouncerParseNode() {
    TerminalNode tn = TerminalNodeFactory.createCharacterRangeBasedTerminalNode(
        Character::isWhitespace, "ws");
    bouncerParseNode = new PositionNode("loop bouncer").setDownNode(tn);
  }

  int bouncerCheck(int token) {
    if (!isBouncerCandidate(token)) {
      return NOT_FOUND;
    }
    return boundaryCheck(bouncerParseNode, token);
  }

  int kickoutCheck(int token) {
    if (kickoutParseNode == null || !tokens.checkIndex(token)) {
      return NOT_FOUND;
    }
    return boundaryCheck(kickoutParseNode, token);
  }

  private boolean isBouncerCandidate(int token) {
    return bouncerParseNode != null
        && tokens.checkIndex(token)
        && !tokens.isAnRequest(token)
        && !tokens.isLoopProbe(token);
  }

  private int boundaryCheck(ParseNode boundaryNode, int token) {
    wireBoundaryNode(boundaryNode);
    int tokenCp = tokens.checkpoint();
    int lastTokenFound = tokens.getLastTokenFound();
    int resultCp = (parser != null) ? parser.checkpointResult() : -1;
    int result = boundaryNode.callReceived(token);
    tokens.rollbackTo(tokenCp);
    tokens.setLastTokenFound(lastTokenFound);
    if (parser != null) {
      parser.rollbackResultTo(resultCp);
    }
    return result;
  }

  private int matchCurrentToken(int token, boolean preventInnerPump,
      int pendingTriviaFrom, int pendingTriviaTo) {
    int resultCp = (parser != null) ? parser.checkpointResult() : -1;
    int result;

    if (parser != null && pendingTriviaFrom >= 0 && pendingTriviaTo > pendingTriviaFrom) {
      parser.recordTriviaMatch(
          pendingTriviaFrom, pendingTriviaTo, getBouncerTriviaCategory());
    }

    if (preventInnerPump) {
      boolean oldProbe = tokens.isLoopProbe(token);
      tokens.setLoopProbe(token, true);
      try {
        result = callDown(token);
      } finally {
        tokens.setLoopProbe(token, oldProbe);
      }
    } else {
      result = callDown(token);
    }

    if (result <= token && parser != null) {
      parser.rollbackResultTo(resultCp);
    }
    return result;
  }

  private String getBouncerTriviaCategory() {
    if (triviaConfigured && triviaSelector != null) {
      return triviaSelector;
    }
    String terminalTag = findFirstTerminalTag(
        bouncerParseNode, Collections.newSetFromMap(new IdentityHashMap<>()));
    if (terminalTag != null && !terminalTag.isBlank()) {
      return terminalTag;
    }
    if (bouncerParseNode != null
        && bouncerParseNode.getName() != null
        && !bouncerParseNode.getName().isBlank()
        && !"name not set".equals(bouncerParseNode.getName())) {
      return bouncerParseNode.getName();
    }
    return "loop bouncer";
  }

  private String findFirstTerminalTag(ParseNode node, Set<ParseNode> visited) {
    if (node == null || !visited.add(node)) {
      return null;
    }
    if (node instanceof TerminalNode terminalNode) {
      return terminalNode.getTag();
    }

    String downTag = findFirstTerminalTag(node.getDownNode(), visited);
    return downTag != null ? downTag : findFirstTerminalTag(node.getRightNode(), visited);
  }

  private int finishLoop(int token, boolean hasMin, int matchedIterations,
      int furthestMatch, int pendingTriviaFrom, int pendingTriviaTo) {
    if (hasMin && matchedIterations < min) {
      return NOT_FOUND;
    }
    if (collectEdgeTrivia
        && matchedIterations > 0
        && pendingTriviaFrom >= 0
        && pendingTriviaTo > pendingTriviaFrom) {
      if (parser != null) {
        parser.recordTriviaMatch(
            pendingTriviaFrom, pendingTriviaTo, getBouncerTriviaCategory());
      }
      return Math.max(pendingTriviaTo, Math.max(furthestMatch, token));
    }
    return Math.max(furthestMatch, token);
  }

  private void wireBoundaryNode(ParseNode node) {
    if (node == null) {
      return;
    }
    node.tokens = tokens;
    node.parser = parser;
    if (node.hasDownNode()) {
      wireBoundaryNode(node.getDownNode());
    }
    if (node.hasRightNode()) {
      wireBoundaryNode(node.getRightNode());
    }
  }

  @Override
  public int callReceived(int token) {
    if (token == END_OF_QUEUE) return token;

    final boolean hasMin = min != 0;

    if (!tokens.checkIndex(token)) {
      return hasMin ? END_OF_QUEUE : token;
    }
    int sent = token;
    boolean hadNonWhitespaceMatch = false;
    int furthestMatch = token;
    int matchedIterations = 0;
    int pendingTriviaFrom = -1;
    int pendingTriviaTo = -1;

    while (true) {
      if (!tokens.checkIndex(sent)) {
        return finishLoop(
            token, hasMin, matchedIterations, furthestMatch, pendingTriviaFrom, pendingTriviaTo);
      }

      int kickoutResult = kickoutCheck(sent);
      if (kickoutResult > sent) {
        return finishLoop(
            token, hasMin, matchedIterations, furthestMatch, pendingTriviaFrom, pendingTriviaTo);
      }

      boolean probeAgainstBouncer = (hadNonWhitespaceMatch || collectEdgeTrivia)
          && (triviaConfigured || isBouncerCandidate(sent));
      int curResult = matchCurrentToken(
          sent, probeAgainstBouncer, pendingTriviaFrom, pendingTriviaTo);

      if (curResult > sent) {
        pendingTriviaFrom = -1;
        pendingTriviaTo = -1;
        matchedIterations++;
        furthestMatch = curResult;

        if (!tokens.isUnhandledWhitespace(sent)) {
          hadNonWhitespaceMatch = true;
        }

        if (max != 0 && matchedIterations >= max) {
          return curResult;
        }

        sent = tokens.getNextToken(curResult - 1);
        if (sent < 0) {
          return finishLoop(
              token, hasMin, matchedIterations, furthestMatch, pendingTriviaFrom, pendingTriviaTo);
        }
        continue;
      }

      if (probeAgainstBouncer) {
        if (triviaConfigured) {
          int triviaResult = triviaSelector != null && parser != null
              ? parser.matchTrivia(triviaSelector, sent)
              : NOT_FOUND;
          if (triviaResult > sent) {
            if (pendingTriviaFrom < 0) {
              pendingTriviaFrom = sent;
            }
            pendingTriviaTo = triviaResult;
            sent = triviaResult;
            continue;
          }
          return finishLoop(
              token, hasMin, matchedIterations, furthestMatch, pendingTriviaFrom, pendingTriviaTo);
        }
        if (loopType == LoopType.STRUCTURAL) {
          int bouncerResult = bouncerCheck(sent);
          if (bouncerResult > sent) {
            if (pendingTriviaFrom < 0) {
              pendingTriviaFrom = sent;
            }
            pendingTriviaTo = bouncerResult;
            sent = bouncerResult;
            continue;
          }
        }
        return finishLoop(
            token, hasMin, matchedIterations, furthestMatch, pendingTriviaFrom, pendingTriviaTo);
      }

      if (curResult == sent) {
        return finishLoop(
            token, hasMin, matchedIterations, curResult, pendingTriviaFrom, pendingTriviaTo);
      }

      return finishLoop(
          token, hasMin, matchedIterations, furthestMatch, pendingTriviaFrom, pendingTriviaTo);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof LoopNode)) return false;
    LoopNode other = (LoopNode) o;
    return this.max == other.max
        && this.min == other.min
        && this.loopType == other.loopType
        && this.collectEdgeTrivia == other.collectEdgeTrivia
        && this.collectorScanner == other.collectorScanner;
  }
}
