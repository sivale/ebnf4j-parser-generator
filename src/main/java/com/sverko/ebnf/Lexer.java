package com.sverko.ebnf;

import com.sverko.ebnf.tools.UnicodeString;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class Lexer {
  public static Builder builder() { return new Builder(); }

  private DownRightNode rootNode;
  private MatchType matchType;
  private boolean IGNORE_WHITESPACE = true;
  private BiFunction<DownRightNode, Integer, Boolean> matchTester;
  private boolean PRESERVE_WS_IN_QUOTES = false;

  private enum MatchType { CASE_SENSITIVE, CASE_INSENSITIVE }

  public Lexer(Set<String> keywords) { this(keywords, false, true, false); }

  public Lexer(Set<String> keywords, boolean caseSensitive) {
    this(keywords, caseSensitive, true, false);
  }

  public Lexer(Set<String> tokens, boolean caseSensitive, boolean ignoreWhitespace) {
    this(tokens, caseSensitive, ignoreWhitespace, false);
  }

  public Lexer(Set<String> tokens, boolean caseSensitive, boolean ignoreWhitespace, boolean preserveWhitespaceInQuotes) {
    this.matchType = caseSensitive ? MatchType.CASE_SENSITIVE : MatchType.CASE_INSENSITIVE;
    this.IGNORE_WHITESPACE = ignoreWhitespace;
    this.PRESERVE_WS_IN_QUOTES = preserveWhitespaceInQuotes;
    buildLexerTree(tokens);
    chooseMatchTester();
  }

  public Lexer(boolean ignoreWhitespace, boolean preserveWhitespaceInQuotes) {
    this.matchType = MatchType.CASE_INSENSITIVE;
    this.IGNORE_WHITESPACE = ignoreWhitespace;
    this.PRESERVE_WS_IN_QUOTES = preserveWhitespaceInQuotes;
    this.rootNode = null;
    chooseMatchTester();
  }

  public Lexer() {
    this.matchType = MatchType.CASE_INSENSITIVE;
    this.PRESERVE_WS_IN_QUOTES = false;
    this.rootNode = null;
    chooseMatchTester();
  }

  private void chooseMatchTester() {
    if (matchType == MatchType.CASE_SENSITIVE) {
      matchTester = (n, c) -> n.codePoints[0] == Character.toChars(c)[0];
    } else {
      matchTester = (n, c) -> Character.toUpperCase(n.codePoints[0]) == Character.toUpperCase(Character.toChars(c)[0]);
    }
  }

  void buildLexerTree(Set<String> keywords) {
    rootNode = null;
    if (keywords == null) return;

    for (String token : keywords) {
      if (token == null || token.isEmpty()) continue;
      char[] chars = token.toCharArray();

      if (rootNode == null) {
        rootNode = new DownRightNode(new char[]{chars[0]});
      }

      DownRightNode current = rootNode;
      DownRightNode previous;

      for (int i = 0; i < chars.length; i++) {
        if (i == 0) {
          DownRightNode sibling = current;
          previous = null;
          while (sibling != null && !matchChar(sibling, chars[i])) {
            previous = sibling;
            sibling = sibling.getFirstSibling();
          }
          if (sibling == null) {
            DownRightNode newNode = new DownRightNode(new char[]{chars[i]});
            if (previous == null) {
              newNode.setFirstSibling(rootNode);
              rootNode = newNode;
              current = newNode;
            } else {
              previous.setFirstSibling(newNode);
              current = newNode;
            }
          } else {
            current = sibling;
          }
        } else {
          DownRightNode child = current.getFirstChild();
          previous = null;
          while (child != null && !matchChar(child, chars[i])) {
            previous = child;
            child = child.getFirstSibling();
          }
          if (child == null) {
            DownRightNode newNode = new DownRightNode(new char[]{chars[i]});
            if (previous == null) {
              current.setFirstChild(newNode);
            } else {
              previous.setFirstSibling(newNode);
            }
            current = newNode;
          } else {
            current = child;
          }
        }
      }
      current.setStopMark(true);
    }
  }

  public List<String> lexText(String text) {
    List<String> lines = Arrays.asList(text.split("\n"));
    return lexText(lines);
  }

  public List<String> lexText(List<String> lines) {
    List<String> tokens = new ArrayList<>();

    if (rootNode == null) {
      for (String line : lines) {
        UnicodeString ul = new UnicodeString(line);
        boolean inQuotes = false;
        char quoteChar = 0;

        for (int idx = 0; idx < ul.length(); idx++) {
          String cpStr = ul.getStringAt(idx);
          char ch = cpStr.charAt(0);

          // Quote-Start?
          if (!inQuotes && (ch == '"' || ch == '\'')) {
            tokens.add(cpStr);
            inQuotes = true;
            quoteChar = ch;
            continue;
          }

          // Innerhalb Quotes
          if (inQuotes) {
            if (ch == quoteChar) {
              tokens.add(cpStr);
              inQuotes = false;
              quoteChar = 0;
            } else {
              if (Character.isWhitespace(ch) && !PRESERVE_WS_IN_QUOTES) {
                if (!IGNORE_WHITESPACE) tokens.add(cpStr);
              } else {
                tokens.add(cpStr);
              }
            }
            continue;
          }
          if (Character.isWhitespace(ch)) {
            if (!IGNORE_WHITESPACE) tokens.add(cpStr);
          } else {
            tokens.add(cpStr);
          }
        }
      }
      return tokens;
    }
    for (String line : lines) {
      int i = 0;
      while (i < line.length()) {
        if (IGNORE_WHITESPACE) {
          while (i < line.length() && Character.isWhitespace(line.charAt(i))) i++;
          if (i >= line.length()) break;
        }

        int longestMatchLength = 0;
        String longestMatch = null;

        DownRightNode depthHead = rootNode;
        int j = i;
        StringBuilder currentMatch = new StringBuilder();

        while (j < line.length()) {
          int c = line.charAt(j);
          if (IGNORE_WHITESPACE && Character.isWhitespace(c)) {
            if (!hasSiblingMatchingChar(depthHead, c)) {
              j++;
            }
          }
          DownRightNode probe = depthHead;
          while (probe != null && !matchTester.apply(probe, c)) {
            probe = probe.getFirstSibling();
          }
          if (probe == null) break;

          currentMatch.append((char) c);
          if (probe.hasStopMark()) {
            longestMatchLength = j - i + 1;
            longestMatch = currentMatch.toString();
          }
          depthHead = probe.getFirstChild();
          j++;
        }

        if (longestMatch != null) {
          tokens.add(longestMatch);
          i += longestMatchLength;
        } else {
          tokens.add(Character.toString(line.charAt(i)));
          i++;
        }
      }
    }
    return tokens;
  }

  /** Prüft, ob in der aktuellen Trie-Tiefe ein Sibling den gegebenen char 'c' akzeptiert. */
  private boolean hasSiblingMatchingChar(DownRightNode depthHead, int c) {
    DownRightNode n = depthHead;
    while (n != null) {
      if (matchTester.apply(n, c)) return true;
      n = n.getFirstSibling();
    }
    return false;
  }

  private boolean matchChar(DownRightNode node, char c) {
    if (node.codePoints == null || node.codePoints.length == 0) return false;
    if (matchType == MatchType.CASE_SENSITIVE) {
      return node.codePoints[0] == c;
    } else {
      return Character.toUpperCase(node.codePoints[0]) == Character.toUpperCase(c);
    }
  }

  public void printNodeGraph() {
    List<String> outputGraph = new ArrayList<>();
    if (rootNode != null) {
      printGraphFrom(rootNode, 0, 1, "-> ", outputGraph);
      outputGraph.forEach(System.out::println);
    }
  }

  public String getOutputGraph() {
    List<String> outputGraph = new ArrayList<>();
    if (rootNode != null) {
      printGraphFrom(rootNode, 0, 1, "-> ", outputGraph);
    }
    return String.join("\n", outputGraph);
  }

  private void printGraphFrom(DownRightNode node, int line, int column, String prefix, List<String> outputGraph) {
    if (outputGraph.size() == 0) {
      insertNewLine(outputGraph);
      insertNode(column, line, prefix, node, outputGraph);
    } else {
      insertNode(column, line, prefix, node, outputGraph);
    }
    if (node.hasSibling()) {
      if (!hasLinesBelowCurrentLine(line, outputGraph)) {
        insertPipeInNewLine(column, outputGraph);
        insertNewLine(outputGraph);
        printGraphFrom(node.getFirstSibling(), line+2, column, "   ", outputGraph);
      } else {
        duplicateLineWithPipes(line+1, outputGraph);
        addPipeToLineWithPipes(line+1, column, outputGraph);
        printGraphFrom(node.getFirstSibling(), line+2, column, "    ", outputGraph);
      }
    }
    if (node.hasChildren()) {
      printGraphFrom(node.getFirstChild(), line, column+1,"-> ", outputGraph);
    }
  }

  Predicate<String> hasAnotherPipeInLine = s -> getPositionOfPipe(s) > -1;
  boolean hasLinesBelowCurrentLine(int line, List<String> outputGraph) { return outputGraph.size()-1 > line; }
  int getPositionOfPipe(String outputLine) { return outputLine.indexOf('|'); }
  void insertPipeInNewLine(int column, List<String> outputGraph) {
    String outputline="";
    for (int i=0; i < column-1; i++) { outputline+="     "; }
    outputline+="   |";
    outputGraph.add(outputline);
  }
  void addPipeToLineWithPipes(int line, int column, List<String> outputGraph) {
    String outputLine = outputGraph.get(line);
    int lastOccupiedColumn = ceilDiv(outputLine.length(), 5);
    for (int i = lastOccupiedColumn; i < column-1; i++) { outputLine+="     "; }
    outputGraph.set(line, outputLine+="    |");
  }
  void duplicateLineWithPipes(int line, List<String> outputGraph) {
    if (hasAnotherPipeInLine.test(outputGraph.get(line))) {
      outputGraph.add(line, outputGraph.get(line));
    } else {
      String inputLine = outputGraph.get(line-1);
      StringBuilder outputLine = new StringBuilder();
      int index=0;
      while (true) {
        index = inputLine.indexOf("|", index);
        if (index==-1) break;
        for (int i = 0; i < index; i++) { outputLine.append(" "); }
        outputLine.append("|");
        index++;
      }
      outputGraph.add(line, outputLine.toString());
      outputGraph.add(line, outputLine.toString());
    }
  }
  void insertNewLine(List<String> outputGraph) { outputGraph.add(""); }
  void insertNode (int column, int line, String prefix, DownRightNode node, List<String> outputGraph) {
    String outputLine = outputGraph.get(line);
    int lastOccupiedColumn = ceilDiv(outputLine.length(), 5);
    for (int i = lastOccupiedColumn; i < column-1; i++) { outputLine+="     "; }
    outputGraph.set(line, outputLine+=(prefix+node));
  }

  public static int ceilDiv(int x, int y) { if (x == 0) return 0; return (x + y - 1) / y; }

  public static class DownRightNode {
    private DownRightNode firstSibling, firstChild;
    private char[] codePoints;
    private boolean stopMark;

    public DownRightNode(char[] codePoints) { this.codePoints = codePoints; }
    public DownRightNode getFirstSibling() { return firstSibling; }
    public void setFirstSibling(DownRightNode firstSibling) { this.firstSibling = firstSibling; }
    public DownRightNode getFirstChild() { return firstChild; }
    public void setFirstChild(DownRightNode firstChild) { this.firstChild = firstChild; }
    public char[] getcodePoints() { return codePoints; }
    public boolean hasSibling() { return firstSibling != null; }
    public boolean hasChildren() { return firstChild != null; }
    public boolean hasStopMark() { return stopMark; }
    public void setStopMark(boolean stopMark) { this.stopMark = stopMark; }
    @Override public String toString() { return codePoints == null ? "" : String.valueOf(codePoints) + (stopMark ? "*" : " "); }
  }

  public static class Builder {
    Set<String> keywords = null;
    boolean ignoreWhitespace = true;
    boolean ignoreCase = false; // Beibehaltung der vorhandenen Semantik
    boolean preserveWhitespaceInQuotes = false;

    public Builder tokens(Set<String> tokens){ this.keywords = tokens; return this; }
    public Builder keywords(Set<String> tokens){ this.keywords = tokens; return this; } // Alias
    public Builder ignoreWhitespace(boolean ignoreWhitespace){ this.ignoreWhitespace = ignoreWhitespace; return this; }
    public Builder ignoreCase(boolean ignoreCase){ this.ignoreCase = ignoreCase; return this; }
    public Builder preserveWhitespaceInQuotes(boolean preserve){ this.preserveWhitespaceInQuotes = preserve; return this; }
    public Lexer build(){ return new Lexer(keywords, ignoreCase, ignoreWhitespace, preserveWhitespaceInQuotes); }
  }
}
