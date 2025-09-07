package com.sverko.ebnf;

import com.sverko.ebnf.tools.UnicodeString;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class Lexer {

  public static Builder builder() {
    return new Builder();
  }

  public LexerNode rootNode;
  private MatchType matchType;
  private boolean IGNORE_WHITESPACE = true;
  private BiFunction<DownRightNode, Integer, Boolean> matchTester;
  private boolean PRESERVE_WS_IN_QUOTES = false;

  private enum MatchType {CASE_SENSITIVE, CASE_INSENSITIVE}

  public Lexer(Set<String> keywords) {
    this(keywords, false, true, false);
  }

  public Lexer(Set<String> keywords, boolean caseSensitive) {
    this(keywords, caseSensitive, true, false);
  }

  public Lexer(Set<String> tokens, boolean caseSensitive, boolean ignoreWhitespace) {
    this(tokens, caseSensitive, ignoreWhitespace, false);
  }

  public Lexer(Set<String> tokens, boolean caseSensitive, boolean ignoreWhitespace,
      boolean preserveWhitespaceInQuotes) {
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
      matchTester = (n, c) -> Character.toUpperCase(n.codePoints[0]) == Character.toUpperCase(
          Character.toChars(c)[0]);
    }
  }
  void buildLexerTrie(Set<String> keywords) {
    if (keywords == null) { return; }
    List<String> sortedKeywords = new ArrayList<>(keywords);
    Collections.sort(sortedKeywords);
    rootNode = new LexerNode(null);
    buildSeparateChains(sortedKeywords);
    consolidateLexerTrie(rootNode);
  }

  private void buildSeparateChains(List<String> sortedKeywords) {
    LexerNode topLevelNode = rootNode;
    for (int k = 0; k < sortedKeywords.size(); k++) {
      UnicodeString unicodeString = new UnicodeString(sortedKeywords.get(k));
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
    boolean hasDownNode() { return downNode != null;}
    boolean hasRightNode() { return rightNode != null;}
    boolean hasStopMark() { return stopMark;}
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

  public List<String> lexText(String text) {
    return lexText(new UnicodeString(text));
  }

  private static boolean isQuote(String ch) {
    return "\"".equals(ch) || "'".equals(ch);
  }

  public List<String> lexText(UnicodeString text) {
    List<String> tokens = new ArrayList<>();
    StringBuilder keywordLine = new StringBuilder();
    StringBuilder completeLine = new StringBuilder();

    // Sentinel-Startknoten: erste echte Spalte hängt rechts an rootNode
    LexerNode startNode = new LexerNode(null);
    startNode.rightNode = rootNode;
    LexerNode currentNode = startNode;

    for (int i = 0; i < text.length(); i++) {
      String character = text.getStringAt(i); // EIN vollständiger Codepoint als String

      // ===== TERMINAL-STRINGS (nur wenn Flag aktiv) ===============================
      if (PRESERVE_WS_IN_QUOTES && ("\"".equals(character) || "'".equals(character))) {

        // Lookahead: gibt es ein passendes schließendes Quote gleichen Typs?
        String quote = character; // "'" oder "\""
        int j = i + 1;
        boolean closed = false;
        while (j < text.length()) {
          String ch = text.getStringAt(j);
          if (ch.equals(quote)) { closed = true; j++; break; }
          j++;
        }

        if (closed) {
          // Laufendes Keyword sauber an Token-Grenze beenden (wie Mismatch)
          if (keywordLine.length() > 0) {
            if (completeLine.length() > 0) {
              tokens.add(completeLine.toString());
              String overflow = keywordLine.substring(completeLine.length());
              int overflowCp = overflow.codePointCount(0, overflow.length());
              keywordLine.setLength(0);
              completeLine.setLength(0);
              currentNode = startNode;
              i -= overflowCp + 1; // for++ kompensiert +1 -> Quote bleibt „dran“
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

          // Quotes + Inhalt emittieren:
          // - öffnendes Quote
          tokens.add(quote);
          // - Inhalt als Einzeltokens (inkl. Spaces)
          for (int k = i + 1; k < j - 1; k++) {
            tokens.add(text.getStringAt(k));
          }
          // - schließendes Quote
          tokens.add(quote);

          // Reset & hinter das schließende Quote springen
          keywordLine.setLength(0);
          completeLine.setLength(0);
          currentNode = startNode;
          i = j - 1; // for++ danach
          continue;
        }
        // !closed: KEINE Sonderbehandlung → normaler Flow (inkl. evtl. Whitespace-Skip)
      }

      // ===== KONTEXTSENSITIVER WHITESPACE-SKIP ====================================
      if (IGNORE_WHITESPACE && Character.isWhitespace(character.codePointAt(0))) {
        // Skippe NUR wenn der Trie hier KEINEN Whitespace als nächstes Zeichen vorsieht.
        boolean whitespaceIsExpectedHere = false;
        if (currentNode != null && currentNode.hasRightNode()) {
          LexerNode col = currentNode.rightNode;
          while (col != null) {
            if (col.content != null && col.content.equals(character)) {
              whitespaceIsExpectedHere = true;
              break;
            }
            col = col.downNode;
          }
        }
        if (!whitespaceIsExpectedHere) {
          // Whitespace wird ignoriert, laufendes Match bleibt intakt
          continue;
        }
        // sonst: Whitespace ist Teil eines Keywords → NICHT skippen, normal matchen
      }

      // ===== NORMALER TRIE-FLOW ====================================================
      currentNode = characterMatchesNodeInTrie(character, currentNode);

      if (currentNode != null) {
        keywordLine.append(character);
        if (currentNode.hasStopMark()) {
          completeLine.setLength(0);
          completeLine.append(keywordLine);
        }
      } else {
        if (keywordLine.length() > 0) {
          if (completeLine.length() > 0) {
            // Längstes gültiges Match emittieren
            tokens.add(completeLine.toString());

            // Overflow zurückspulen & re-lexen
            String overflow = keywordLine.substring(completeLine.length());
            int overflowCp = overflow.codePointCount(0, overflow.length());

            keywordLine.setLength(0);
            completeLine.setLength(0);
            currentNode = startNode;

            i -= overflowCp + 1; // for++ kompensiert
            continue;
          } else {
            // Kein StopMark: erstes Codepoint sicher abtrennen (codepoint-sicher)
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
        } else {
          // Kein aktives Keyword – Einzelzeichen-Token
          tokens.add(character);
          currentNode = startNode;
        }
      }
    }

    // ===== EOF-FLUSH ===============================================================
    if (keywordLine.length() > 0) {
      if (completeLine.length() > 0) {
        tokens.add(completeLine.toString());
        String overflow = keywordLine.substring(completeLine.length());
        if (!overflow.isEmpty()) {
          tokens.addAll(this.lexText(new UnicodeString(overflow)));
        }
      } else {
        int firstEnd = keywordLine.offsetByCodePoints(0, 1);
        String first = keywordLine.substring(0, firstEnd);
        tokens.add(first);
        String rest = keywordLine.substring(firstEnd);
        if (!rest.isEmpty()) {
          tokens.addAll(this.lexText(new UnicodeString(rest)));
        }
      }
    }

    return tokens;
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

  public List<String> lexText(List<String> lines) {
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

  // BUILDER CLASS
  public static class Builder {
    Set<String> keywords = null;
    boolean ignoreWhitespace = true;
    boolean ignoreCase = false;
    boolean preserveWhitespaceInQuotes = false;

    public Builder tokens(Set<String> tokens) {
      this.keywords = tokens;
      return this;
    }

    public Builder keywords(Set<String> tokens) {
      this.keywords = tokens;
      return this;
    }

    public Builder ignoreWhitespace(boolean ignoreWhitespace) {
      this.ignoreWhitespace = ignoreWhitespace;
      return this;
    }

    public Builder ignoreCase(boolean ignoreCase) {
      this.ignoreCase = ignoreCase;
      return this;
    }

    public Builder preserveWhitespaceInQuotes(boolean preserve) {
      this.preserveWhitespaceInQuotes = preserve;
      return this;
    }

    public Lexer build() {
      return new Lexer(keywords, ignoreCase, ignoreWhitespace, preserveWhitespaceInQuotes);
    }
  }
}