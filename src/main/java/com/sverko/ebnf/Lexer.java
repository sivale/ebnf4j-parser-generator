package com.sverko.ebnf;
import com.sverko.ebnf.tools.UnicodeString;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class Lexer {
  public static Builder builder() {
    return new Builder();
  }
  private DownRightNode rootNode;
  private MatchType matchType;
  private boolean IGNORE_WHITESPACE = true;
  private BiFunction<DownRightNode, Integer, Boolean> matchTester;
  private boolean PRESERVE_WS_IN_QUOTES = false;

  private enum MatchType {
    CASE_SENSITIVE,
    CASE_INSENSITIVE
  }

  public Lexer(Set<String> tokens) {
    this(tokens, false,  true, false);
  }

  public Lexer(Set<String> tokens, boolean caseSensitive) {
    this(tokens, caseSensitive,  true, false);
  }

  public Lexer (Set<String> tokens, boolean caseSensitive, boolean ignoreWhitespace) {
    this(tokens, caseSensitive, ignoreWhitespace, false);
  }

  public Lexer (Set<String> tokens, boolean caseSensitive, boolean ignoreWhitespace, boolean preserveWhitespaceInQuotes) {
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
    if (keywords == null) { return; }
    for (String token : keywords) {
      if (token == null || token.isEmpty()) continue;
      char[] chars = token.toCharArray();

      if (rootNode == null) {
        rootNode = new DownRightNode(new char[]{chars[0]});
      }

      DownRightNode current = rootNode;
      DownRightNode previous = null;

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
        boolean inString = false;
        char currentQuote = 0;
        UnicodeString unicodeLine = new UnicodeString(line);
        for (int idx = 0; idx < unicodeLine.length(); idx++) {
          String chStr = unicodeLine.getStringAt(idx);
          char ch = chStr.charAt(0);

          if (PRESERVE_WS_IN_QUOTES && (ch == '"' || ch == '\'')) {
            // When preserve is enabled and we encounter a quote, collect the whole quoted region
            // into a single token INCLUDING the surrounding quotes and inner whitespace
            StringBuilder quoted = new StringBuilder();
            char quote = ch;
            inString = true;
            currentQuote = quote;
            quoted.append(chStr); // opening quote
            idx++;
            while (idx < unicodeLine.length()) {
              String inner = unicodeLine.getStringAt(idx);
              quoted.append(inner);
              if (inner.charAt(0) == quote) {
                // closing quote reached
                inString = false;
                currentQuote = 0;
                break;
              }
              idx++;
            }
            tokens.add(quoted.toString());
            continue;
          }

          if (Character.isWhitespace(ch)) {
            if (PRESERVE_WS_IN_QUOTES && inString) {
              // This branch is kept for robustness if unbalanced quotes occur; otherwise
              // quoted paths are handled above and won't come here while inString == true.
              tokens.add(chStr);
            } else if (!IGNORE_WHITESPACE) {
              tokens.add(chStr);
            }
            continue;
          }

          tokens.add(chStr);
        }
      }
      return tokens;
    }

    for (String line : lines) {
      int i = 0;
      while (i < line.length()) {
        // 1) Skip leading whitespace of the next token if configured to ignore it
        if (IGNORE_WHITESPACE) {
          while (i < line.length() && Character.isWhitespace(line.charAt(i))) {
            i++;
          }
          if (i >= line.length()) break;
        }

        // 2) If preserve-in-quotes is enabled and the next char starts a quoted block,
        //    consume the entire quoted string and emit it as ONE token (including quotes)
        if (PRESERVE_WS_IN_QUOTES && (line.charAt(i) == '"' || line.charAt(i) == '\'')) {
          char quote = line.charAt(i);
          int start = i;
          i++; // consume opening quote
          boolean closed = false;
          while (i < line.length()) {
            char c = line.charAt(i);
            if (c == quote) {
              i++; // consume closing quote
              closed = true;
              break;
            }
            i++;
          }
          if (closed) {
            String grouped = line.substring(start, i);
            String inner = grouped.substring(1, grouped.length() - 1);
            boolean hasAlnum = inner.chars().anyMatch(Character::isLetterOrDigit);
            if (hasAlnum) {
              tokens.add(grouped);
            } else {
              // Fallback: don't group pseudo-strings like "\" , \""
              tokens.add(Character.toString(quote));
              // Reprocess the remainder starting right after opening quote
              i = start + 1;
            }
          } else {
            // Unterminated quote -> emit only the opening quote
            tokens.add(Character.toString(quote));
          }
          continue;
        }

        // 3) Normal longest-match via trie
        int longestMatchLength = 0;
        String longestMatch = null;
        DownRightNode currentNode = rootNode;
        int j = i;
        int c;
        StringBuilder currentMatch = new StringBuilder();

        while (j < line.length()) {
          c = line.charAt(j);
          if (Character.isWhitespace(c) && IGNORE_WHITESPACE) {
            if (j == i) {
              // safeguard: should rarely happen due to step 1, but keep behavior consistent
              i++;
            }
            j++;
            continue;
          }
          while (currentNode != null && !matchTester.apply(currentNode, c)) {
            currentNode = currentNode.getFirstSibling();
          }
          if (currentNode == null) break;

          currentMatch.append((char) c);
          if (currentNode.hasStopMark()) {
            longestMatchLength = j - i + 1;
            longestMatch = currentMatch.toString();
          }
          currentNode = currentNode.getFirstChild();
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
        addPipeToLineWithPipes( line+1, column, outputGraph);
        printGraphFrom(node.getFirstSibling(), line+2, column, "    ", outputGraph);
      }

    }
    if (node.hasChildren()) {
      printGraphFrom(node.getFirstChild(), line, column+1,"-> ", outputGraph);
    }
  }

  Predicate<String> hasAnotherPipeInLine = s -> getPositionOfPipe(s) > -1;
  boolean hasLinesBelowCurrentLine(int line, List<String> outputGraph) {
    return outputGraph.size()-1 > line;
  }
  int getPositionOfPipe(String outputLine) {
    return outputLine.indexOf('|');
  }
  void insertPipeInNewLine(int column, List<String> outputGraph) {
    String outputline="";
    for (int i=0; i < column-1; i++) {
      outputline+="     ";
    }
    outputline+="   |";
    outputGraph.add(outputline);
  }

  void addPipeToLineWithPipes(int line, int column, List<String> outputGraph) {
    //add padding if there is something in line already
    String outputLine = outputGraph.get(line);
    int lastOccupiedColumn = ceilDiv(outputLine.length(), 5);
    for (int i = lastOccupiedColumn; i < column-1; i++) {
      outputLine+="     ";
    }
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

  void insertNode (int column, int line, String prefix, DownRightNode node, List<String> outputGraph) {
    //add padding if there is something in line already
    String outputLine = outputGraph.get(line);
    int lastOccupiedColumn = ceilDiv(outputLine.length(), 5);
    for (int i = lastOccupiedColumn; i < column-1; i++) {
      outputLine+="     ";
    }
    outputGraph.set(line, outputLine+=(prefix+node));
  }

  public static int ceilDiv(int x, int y) {
    if (x == 0) return 0;
    return (x + y - 1) / y;
  }

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

  public static class Builder {
    Set<String> tokens = null;
    boolean ignoreWhitespace = true;
    boolean ignoreCase = false;
    boolean preserveWhitespaceInQuotes = false;

    Builder tokens(Set<String> tokens){
      this.tokens = tokens;
      return this;
    }
    Builder ignoreWhitespace(boolean ignoreWhitespace){
      this.ignoreWhitespace = ignoreWhitespace;
      return this;
    }
    Builder ignoreCase(boolean ignoreCase){
      this.ignoreCase = ignoreCase;
      return this;
    }
    // Neuer Builder-Schalter
    Builder preserveWhitespaceInQuotes(boolean preserve){
      this.preserveWhitespaceInQuotes = preserve;
      return this;
    }
    Lexer build(){
      return new Lexer(tokens, ignoreCase, ignoreWhitespace, preserveWhitespaceInQuotes);
    }
  }
}
