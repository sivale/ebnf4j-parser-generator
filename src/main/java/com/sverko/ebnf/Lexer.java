package com.sverko.ebnf;

import com.sverko.ebnf.tools.UTF8FileToStringArrayList;
import com.sverko.ebnf.tools.UnicodeString;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class Lexer {

  public LexerNode rootNode;
  private MatchType matchType;
  private BiFunction<DownRightNode, Integer, Boolean> matchTester;

  private enum MatchType {CASE_SENSITIVE, CASE_INSENSITIVE}

  public Lexer(Set<String> terminalStrings, boolean caseSensitive) {
    this.matchType = caseSensitive ? MatchType.CASE_SENSITIVE : MatchType.CASE_INSENSITIVE;
    buildLexerTree(terminalStrings);
    chooseMatchTester();
  }

  public Lexer(Set<String> terminalStrings) {
    this(terminalStrings, true);
  }

  public Lexer() {
    this(null, true);
  }

  private void chooseMatchTester() {
    if (matchType == MatchType.CASE_SENSITIVE) {
      matchTester = (n, c) -> n.codePoints[0] == Character.toChars(c)[0];
    } else {
      matchTester = (n, c) -> Character.toUpperCase(n.codePoints[0]) == Character.toUpperCase(
          Character.toChars(c)[0]);
    }
  }

  void buildLexerTrie(Set<String> terminalStrings) {
    if (terminalStrings == null) {
      return;
    }
    List<String> sortedTerminalStrings = new ArrayList<>(terminalStrings);
    Collections.sort(sortedTerminalStrings);
    rootNode = new LexerNode(null);
    buildSeparateChains(sortedTerminalStrings);
    consolidateLexerTrie(rootNode);
  }

  private void buildSeparateChains(List<String> sortedTerminalStrings) {
    LexerNode topLevelNode = rootNode;
    for (int k = 0; k < sortedTerminalStrings.size(); k++) {
      UnicodeString unicodeString = new UnicodeString(sortedTerminalStrings.get(k));
      LexerNode currentNode;
      if (k > 0) {
        topLevelNode.downNode = new LexerNode(topLevelNode);
        topLevelNode = topLevelNode.downNode;
        currentNode = topLevelNode;
      } else {
        currentNode = topLevelNode;
      }
      for (int i = 0; i < unicodeString.length(); i++) {
        String character = unicodeString.getStringAt(i);
        boolean isLastChar = (i == unicodeString.length() - 1);
        if (currentNode.content == null) {
          assignKeyword(currentNode, character, isLastChar);
        } else {
          currentNode.rightNode = new LexerNode(currentNode);
          currentNode = currentNode.rightNode;
          assignKeyword(currentNode, character, isLastChar);
        }
      }
    }
  }

  void consolidateLexerTrie(LexerNode node) {
    if (node == null || node.content == null) {
      if (node != null) {
        if (node.hasDownNode()) {
          consolidateLexerTrie(node.downNode);
        }
        if (node.hasRightNode()) {
          consolidateLexerTrie(node.rightNode);
        }
      }
      return;
    }

    LexerNode currentNode = node.downNode;
    while (currentNode != null) {
      LexerNode nextNode = currentNode.downNode;
      if (currentNode.content != null && node.content.equals(currentNode.content)) {
        if (node.rightNode == null) {
          node.rightNode = currentNode.rightNode;
          if (currentNode.rightNode != null) {
            currentNode.rightNode.parent = node;
          }
        } else if (currentNode.rightNode != null) {
          LexerNode lastNode = node.rightNode;
          while (lastNode.downNode != null) {
            lastNode = lastNode.downNode;
          }
          lastNode.downNode = currentNode.rightNode;
          currentNode.rightNode.parent = lastNode;
        }
        if (currentNode.stopMark) {
          node.stopMark = true;
        }
        if (currentNode.parent != null) {
          currentNode.parent.downNode = currentNode.downNode;
          if (currentNode.downNode != null) {
            currentNode.downNode.parent = currentNode.parent;
          }
        }
      }
      currentNode = nextNode;
    }

    if (node.hasDownNode()) {
      consolidateLexerTrie(node.downNode);
    }
    if (node.hasRightNode()) {
      consolidateLexerTrie(node.rightNode);
    }
  }

  void assignKeyword(LexerNode node, String keyword, boolean stopMark) {
    node.content = keyword;
    node.stopMark = stopMark;
  }

  public static class LexerNode {

    LexerNode parent;
    LexerNode downNode;
    LexerNode rightNode;
    String content;
    private boolean stopMark;

    LexerNode(LexerNode parent) {
      this.parent = parent;
    }

    boolean hasDownNode() {
      return downNode != null;
    }

    boolean hasRightNode() {
      return rightNode != null;
    }

    boolean hasStopMark() {
      return stopMark;
    }

    @Override
    public String toString() {
      if (content == null) {
        return "";
      }
      return content + (stopMark ? "*" : " ");
    }
  }

  void buildLexerTree(Set<String> tokens) {
    buildLexerTrie(tokens);
  }

  public TokenQueue lexText(Path location) throws IOException {
    return lexText(UTF8FileToStringArrayList.loadFileIntoStringList(location));
  }

  public TokenQueue lexText(String text) {
    return lexText(new UnicodeString(text));
  }

  public TokenQueue lexText(UnicodeString text) {
    List<String> tokens = new ArrayList<>();
    StringBuilder keywordLine = new StringBuilder();
    StringBuilder completeLine = new StringBuilder();

    LexerNode startNode = new LexerNode(null);
    startNode.rightNode = rootNode;
    LexerNode currentNode = startNode;

    for (int i = 0; i < text.length(); i++) {
      String character = text.getStringAt(i);

      // 1) Always try trie matching first (including whitespace)
      LexerNode nextNode = characterMatchesNodeInTrie(character, currentNode);

      if (nextNode != null) {
        currentNode = nextNode;
        keywordLine.append(character);

        if (currentNode.hasStopMark()) {
          completeLine.setLength(0);
          completeLine.append(keywordLine);
        }
        continue;
      }

      // 2) Trie miss: if we are in an ongoing match, flush
      if (keywordLine.length() > 0) {
        if (completeLine.length() > 0) {
          String lexeme = completeLine.toString();
          tokens.add(lexeme);

          String overflow = keywordLine.substring(completeLine.length());
          int overflowCp = overflow.codePointCount(0, overflow.length());

          keywordLine.setLength(0);
          completeLine.setLength(0);
          currentNode = startNode;

          i -= overflowCp + 1;
          continue;
        } else {
          int firstEnd = keywordLine.offsetByCodePoints(0, 1);
          String first = keywordLine.substring(0, firstEnd);
          tokens.add(first);

          String rest = keywordLine.substring(firstEnd);
          int restCp = rest.codePointCount(0, rest.length());

          keywordLine.setLength(0);
          completeLine.setLength(0);
          currentNode = startNode;

          i -= restCp + 1;
          continue;
        }
      }

      // 3) No ongoing match: emit single character as token
      tokens.add(character);
      currentNode = startNode;
    }

    // 4) Flush end
    if (keywordLine.length() > 0) {
      if (completeLine.length() > 0) {
        String lexeme = completeLine.toString();
        tokens.add(lexeme);

        String overflow = keywordLine.substring(completeLine.length());
        if (!overflow.isEmpty()) {
          // Emit overflow as single codepoints (same behavior as before)
          for (int k = 0; k < overflow.length(); ) {
            int cp = overflow.codePointAt(k);
            String one = new String(Character.toChars(cp));
            tokens.add(one);
            k += Character.charCount(cp);
          }
        }
      } else {
        String rest = keywordLine.toString();
        for (int k = 0; k < rest.length(); ) {
          int cp = rest.codePointAt(k);
          String one = new String(Character.toChars(cp));
          tokens.add(one);
          k += Character.charCount(cp);
        }
      }
    }
    return new TokenQueue(tokens);
  }

  LexerNode characterMatchesNodeInTrie (String character, LexerNode currentNode) {
    if (currentNode == null) { return null;}
        if (currentNode.hasRightNode()) {
        currentNode = currentNode.rightNode;
        while (currentNode != null) {
          if (Objects.equals(currentNode.content, character)) {
            return currentNode;
          }
          currentNode = currentNode.downNode; // weiter nach unten
        }
      }
    return null;
  }

  public TokenQueue lexText(List<String> lines) {
    ArrayList<String> tokens = new ArrayList<>();
    UnicodeString text = new UnicodeString(String.join("\n", lines));
    return lexText(text);
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

  private void printGraphFrom(LexerNode node, int line, int column, String prefix,
      List<String> outputGraph) {
    if (outputGraph.size() == 0) {
      insertNewLine(outputGraph);
      insertNode(column, line, prefix, node, outputGraph);
    } else {
      insertNode(column, line, prefix, node, outputGraph);
    }
    if (node.hasDownNode()) {
      if (!hasLinesBelowCurrentLine(line, outputGraph)) {
        insertPipeInNewLine(column, outputGraph);
        insertNewLine(outputGraph);
        printGraphFrom(node.downNode, line + 2, column, "   ", outputGraph);
      } else {
        duplicateLineWithPipes(line + 1, outputGraph);
        addPipeToLineWithPipes(line + 1, column, outputGraph);
        printGraphFrom(node.downNode, line + 2, column, "    ", outputGraph);
      }
    }
    if (node.hasRightNode()) {
      printGraphFrom(node.rightNode, line, column + 1, "-> ", outputGraph);
    }
  }

  Predicate<String> hasAnotherPipeInLine = s -> getPositionOfPipe(s) > -1;

  boolean hasLinesBelowCurrentLine(int line, List<String> outputGraph) {
    return outputGraph.size() - 1 > line;
  }

  int getPositionOfPipe(String outputLine) {
    return outputLine.indexOf('|');
  }

  void insertPipeInNewLine(int column, List<String> outputGraph) {
    String outputline = "";
    for (int i = 0; i < column - 1; i++) {
      outputline += "     ";
    }
    outputline += "   |";
    outputGraph.add(outputline);
  }

  void addPipeToLineWithPipes(int line, int column, List<String> outputGraph) {
    String outputLine = outputGraph.get(line);
    int lastOccupiedColumn = ceilDiv(outputLine.length(), 5);
    for (int i = lastOccupiedColumn; i < column - 1; i++) {
      outputLine += "     ";
    }
    outputGraph.set(line, outputLine += "    |");
  }

  void duplicateLineWithPipes(int line, List<String> outputGraph) {
    if (hasAnotherPipeInLine.test(outputGraph.get(line))) {
      outputGraph.add(line, outputGraph.get(line));
    } else {
      String inputLine = outputGraph.get(line - 1);
      StringBuilder outputLine = new StringBuilder();
      int index = 0;
      while (true) {
        index = inputLine.indexOf("|", index);
        if (index == -1) {
          break;
        }
        for (int i = 0; i < index; i++) {
          outputLine.append(" ");
        }
        outputLine.append("|");
        index++;
      }
      outputGraph.add(line, outputLine.toString());
      outputGraph.add(line, outputLine.toString());
    }
  }

  void insertNewLine(List<String> outputGraph) {
    outputGraph.add("");
  }

  void insertNode(int column, int line, String prefix, LexerNode node,
      List<String> outputGraph) {
    String outputLine = outputGraph.get(line);
    int lastOccupiedColumn = ceilDiv(outputLine.length(), 5);
    for (int i = lastOccupiedColumn; i < column - 1; i++) {
      outputLine += "     ";
    }
    outputGraph.set(line, outputLine += (prefix + node));
  }

  public static int ceilDiv(int x, int y) {
    if (x == 0) {
      return 0;
    }
    return (x + y - 1) / y;
  }

  // DOWNRIGHTNODE CLASS (für Kompatibilität)
  public static class DownRightNode {
    private DownRightNode firstSibling, firstChild;
    private char[] codePoints;
    private boolean stopMark;

    public DownRightNode(char[] codePoints) {
      this.codePoints = codePoints;
    }

    public DownRightNode getFirstSibling() {
      return firstSibling;
    }

    public void setFirstSibling(DownRightNode firstSibling) {
      this.firstSibling = firstSibling;
    }

    public DownRightNode getFirstChild() {
      return firstChild;
    }

    public void setFirstChild(DownRightNode firstChild) {
      this.firstChild = firstChild;
    }

    public char[] getcodePoints() {
      return codePoints;
    }

    public boolean hasSibling() {
      return firstSibling != null;
    }

    public boolean hasChildren() {
      return firstChild != null;
    }

    public boolean hasStopMark() {
      return stopMark;
    }

    public void setStopMark(boolean stopMark) {
      this.stopMark = stopMark;
    }

    @Override
    public String toString() {
      return codePoints == null ? "" : String.valueOf(codePoints) + (stopMark ? "*" : " ");
    }
  }
}