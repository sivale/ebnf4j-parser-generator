package com.sverko.ebnf;

import com.sverko.ebnf.Lexer.LexerNode;
import com.sverko.ebnf.tools.UnicodeString;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class LexerTokenTest {

  @Test
  public void lexerShouldHandleUnicodeText() {
    Lexer lexer = new Lexer();
    List<String> input = List.of("😀q😀q😀q");
    List<String> result = lexer.lexText(input);
    List<String> expected = List.of("😀", "q", "😀", "q", "😀", "q");
    assertEquals(expected, result, "lexer should handle surrogate pairs in text");
  }

  @Test
  public void lexerShouldAlsoTokenizeUnicodeTextWhenKeywordsAreGiven() {
    Set<String> tokens = new HashSet<>(Arrays.asList("😀q😀q", "😀q"));
    List<String> input = List.of("😀q😀q😀q");
    Lexer lexer = new Lexer(tokens);
    List<String> result = lexer.lexText(input);
    List<String> expected = List.of("😀q😀q", "😀q");
    assertEquals(expected, result, "lexer should convert surrogate pairs in tokens");
  }

  @Test
  public void testEmojiAsToken() {
    // Emoji selbst ist ein Keyword
    Set<String> keywords = new HashSet<>(Arrays.asList("😊"));
    Lexer lexer = new Lexer(keywords);
    List<String> tokens = lexer.lexText(new UnicodeString("😊"));
    assertEquals(1, tokens.size());
    assertEquals("😊", tokens.get(0));
  }

  @Test
  public void testEmojiBetweenKeywords() {
    Set<String> keywords = new HashSet<>(Arrays.asList("A", "B"));
    Lexer lexer = new Lexer(keywords);
    List<String> tokens = lexer.lexText(new UnicodeString("A😊B"));
    assertTrue(tokens.contains("A"));
    assertTrue(tokens.contains("B"));
    assertTrue(tokens.contains("😊"));
    assertFalse(tokens.contains("�"));
  }

  @Test
  public void lexerShouldHonourIgnoreWhitespace() {
    List<String> input = List.of("a b");
    Lexer lexer = Lexer.builder().ignoreWhitespace(true).build();
    List<String> result = lexer.lexText(input);
    List<String> expected = List.of("a", "b");
    assertEquals(expected, result, "lexer should ignore whitespace");
  }

  @Test
  public void lexerShouldIgnorePureWhitespaceLinesWithoutError() {
    Set<String> tokens = Set.of("a", "b");
    List<String> input = List.of("   ", "a", "b  ", "  ");
    Lexer lexer = Lexer.builder().ignoreWhitespace(true).build();
    List<String> result = lexer.lexText(input);
    List<String> expected = List.of("a", "b");
    assertEquals(expected, result, "lexer should ignore pure whitespace lines");
  }
  @Test
  public void testLexerAcceptKeywords() {
    Set<String> keywords = new HashSet<>(Arrays.asList("si", "no"));
    Lexer lexer = new Lexer(keywords);
    List<String> tokens = lexer.lexText(new UnicodeString("abc\nsi\nef"));
    assert (tokens.contains("si"));
  }

  @Test
  public void testLexerOverflow_Re_Lexing() {
    Set<String> keywords = new HashSet<>(Arrays.asList("Haus", "meister"));
    Lexer lexer = new Lexer(keywords);
    List<String> tokens = lexer.lexText(new UnicodeString("Hausmeister"));
    assertTrue(tokens.contains("Haus"));
    assertTrue(tokens.contains("meister"));
    assertFalse(tokens.contains("m"));
    assertFalse(tokens.contains("e"));
  }

  @Test
  public void testLexerRemainingCharacter() {
    Set<String> keywords = new HashSet<>(Arrays.asList("Haus"));
    Lexer lexer = new Lexer(keywords);
    List<String> tokens = lexer.lexText(new UnicodeString("HausX"));
    assertTrue(tokens.contains("Haus"));
    assertTrue(tokens.contains("X"));
  }

  @Test
  public void lexerShouldHonourIgnoreWhitespace2() {
    Set<String> keywords = Set.of("ab");
    List<String> input = List.of("a b");
    Lexer lexer = Lexer.builder().keywords(keywords).ignoreWhitespace(true).preserveWhitespaceInQuotes(false).build();
    List<String> result = lexer.lexText(input);
    List<String> expected = List.of("ab");
    assertEquals(expected, result, "lexer should ignore whitespace");
  }

  @Test
  public void lexerShouldMatchFullTokens() throws IOException {
    Set<String> tokens = new HashSet<>(Arrays.asList("if", "else", "i", "f"));
    Lexer lexer = new Lexer(tokens);
    List<String> input = Arrays.asList("ifelse");
    List<String> result = lexer.lexText(input);
    List<String> expected = List.of("if", "else");
    assertEquals(expected, result, "lexer should match full tokens");
  }

  @Test
  public void lexerShouldPreferLongestMatch() throws IOException {
    Set<String> tokens = new HashSet<>(Arrays.asList("i", "n", "in", "int", "integer"));
    Lexer lexer = new Lexer(tokens);
    List<String> input = Arrays.asList("integer");
    List<String> result = lexer.lexText(input);
    List<String> expected = List.of("integer");
    assertEquals(expected, result, "lexer should prefer longest match");
  }

  @Test
  public void lexerShouldFallbackToSingleCharactersWhenNoTokensProvided() throws IOException {
    Lexer lexer = new Lexer(); // No Token-Set
    List<String> input = List.of("ifelse");
    List<String> result = lexer.lexText(input);
    List<String> expected = Arrays.asList("i", "f", "e", "l", "s", "e");
    assertEquals(expected, result,
        "lexer should fallback to single characters when no tokens provided");
  }

  @Test
  public void lexerShouldFindTokenEmbeddedInText() throws IOException {
    Set<String> tokens = Set.of("abc");
    Lexer lexer = new Lexer(tokens);
    List<String> input = List.of("xabcy");
    List<String> result = lexer.lexText(input);
    List<String> expected = List.of("x", "abc", "y");
    assertEquals(expected, result,
        "lexer should find token embedded in other text");
  }

  @Test
  public void testPrintoutOfLexerTrieNo1() {
    Set<String> tokens = new HashSet<>(Arrays.asList("abcd"));
    Lexer lexer = new Lexer(tokens);
    lexer.buildLexerTree(tokens);
    assertEquals("-> a -> b -> c -> d*", lexer.getOutputGraph());
  }

  @Test
  public void testPrintoutOfLexerTrieNo2() {
    Set<String> tokens = new HashSet<>(Arrays.asList("abcd", "abrg"));
    Lexer lexer = new Lexer(tokens);
    lexer.buildLexerTree(tokens);
    String expected = """
        -> a -> b -> c -> d*
                     |
                     r -> g*""";
    assertEquals(expected, lexer.getOutputGraph());
  }

  @Test
  public void testPrintoutOfLexerTrieNo3() {
    Set<String> tokens = new HashSet<>(Arrays.asList("abc", "def"));
    Lexer lexer = new Lexer(tokens);
    lexer.buildLexerTree(tokens);
    String expected = """
        -> a -> b -> c*
           |
           d -> e -> f*""";
    assertEquals(expected, lexer.getOutputGraph());
  }

  @Test
  public void testPrintoutOfLexerTrieNo4() {
    Set<String> tokens = new HashSet<>(Arrays.asList("abc", "ank", "trg"));
    Lexer lexer = new Lexer(tokens);
    lexer.buildLexerTree(tokens);
    String expected = """
        -> a -> b -> c*
           |    |
           |    n -> k*
           t -> r -> g*""";
    assertEquals(expected, lexer.getOutputGraph());
  }

  @Test
  public void testPrintoutOfLexerTrieNo5() {
    Set<String> tokens = new HashSet<>(Arrays.asList("abcd", "awmu", "abdh", "trgf"));
    Lexer lexer = new Lexer(tokens);
    lexer.buildLexerTrie(tokens);
    String expected = """
    -> a -> b -> c -> d*
       |    |    |
       |    |    d -> h*
       |    w -> m -> u*
       t -> r -> g -> f*""";
    assertEquals(expected, lexer.getOutputGraph());
  }

  @Test
  public void lexerShouldPreserveWhitespaceInTerminalStringsIfToldSo() throws IOException {
    String input = "MYDEF = \"one two three\" , \"four\", \"five\";";
    EbnfParserGenerator shemaParser = new EbnfParserGenerator();
    shemaParser.lexer = Lexer.builder().preserveWhitespaceInQuotes(true).build();
    List<String> singleCodepointList = shemaParser.lexer.lexText(new UnicodeString(input));
    Parser textParser = shemaParser.getParser(singleCodepointList, true);
    int foundTokens = textParser.parse("one two three four five");
    assert (foundTokens == 3);
  }

  @Test
  public void lexerShouldNotPreserveWhitespaceInTerminalStringsIfNotToldSo() {
    List<String> input = List.of("'a b'");
    Lexer lexer = Lexer.builder().ignoreWhitespace(true).preserveWhitespaceInQuotes(false).build();
    List<String> result = lexer.lexText(input);
    List<String> expected = List.of("'", "a", "b", "'");
    assertEquals(expected, result, "lexer should ignore whitespace");
  }

  @Test
  public void testLexerTrieBuild() {
    Set<String> keywords = new HashSet<>(Arrays.asList("abcd", "abce", "abde", "acde"));
    Lexer lexer = new Lexer();
    lexer.buildLexerTrie(keywords);
    lexer.printNodeGraph();
  }

  @Test
  public void testLexerTrieFailure_NullPointerException() {
    Set<String> keywords = new HashSet<>(Arrays.asList("a", "ab"));
    Lexer lexer = new Lexer();
    assertDoesNotThrow(() -> {
      lexer.buildLexerTrie(keywords);
    });
  }

  @Test
  public void testLexerTrie_ComplexPrefixes() {
    Set<String> keywords = new HashSet<>(Arrays.asList("abc", "abd", "acd", "bcd"));
    Lexer lexer = new Lexer();
    lexer.buildLexerTrie(keywords);
    String result = lexer.getOutputGraph();
    String expected = """
        -> a -> b -> c*
           |    |    |
           |    |    d*
           |    c -> d*
           b -> c -> d*""";
    lexer.printNodeGraph();
    assertEquals(expected, result, "Complex prefixes are not handled correctly");
  }
  @Test
  public void testLexerTrieParentReferencesIntact() {
    Set<String> keywords = new HashSet<>(Arrays.asList("aa", "aaa", "aaaa"));
    Lexer lexer = new Lexer();
    lexer.buildLexerTrie(keywords);
    assertDoesNotThrow(() -> {
      verifyParentReferences(lexer.rootNode);
    }, "Parent references should remain intact");
  }

  private void verifyParentReferences(LexerNode node) {
    if (node == null)
      return;
    if (node.rightNode != null) {
      assertEquals(node, node.rightNode.parent, "Right node parent reference is broken");
      verifyParentReferences(node.rightNode);
    }
    if (node.downNode != null) {
      assertNotNull(node.downNode.parent, "Down node parent reference is null");
      verifyParentReferences(node.downNode);
    }
  }

  private int countStopMarks(LexerNode node) {
    if (node == null)
      return 0;
    int count = node.hasStopMark() ? 1 : 0;
    count += countStopMarks(node.rightNode);
    count += countStopMarks(node.downNode);
    return count;
  }

  @Test
  public void testLexerTrieSuccess_NoCyclicReferences() {
    Set<String> keywords = new HashSet<>(Arrays.asList("abab", "baba"));
    Lexer lexer = new Lexer();
    lexer.buildLexerTrie(keywords);
    Set<LexerNode> visitedNodes = new HashSet<>();
    assertDoesNotThrow(() -> {
      checkForCycles(lexer.rootNode, visitedNodes);
    }, "Trie should not contain cyclic references");
  }


  @Test
  public void testLexerTrieFailure_LostStopMarks() {
    Set<String> keywords = new HashSet<>(Arrays.asList("a", "aa", "aaa"));
    Lexer lexer = new Lexer();
    lexer.buildLexerTrie(keywords);
    int stopMarkCount = countStopMarks(lexer.rootNode);
    lexer.printNodeGraph();
    assertEquals(3, stopMarkCount,
      "Expected 3 stop marks, but found " + stopMarkCount + " - marks are preserved incorrectly");
  }

  private void checkForCycles(LexerNode node, Set<LexerNode> visited) {
    if (node == null)
      return;
    if (visited.contains(node)) {
      throw new RuntimeException("Cycle detected!");
    }
    visited.add(node);
    checkForCycles(node.rightNode, new HashSet<>(visited));
    checkForCycles(node.downNode, new HashSet<>(visited));
  }

  @Test
  public void testLexerTrieFailure_IncompleteConsolidation() {
    Set<String> keywords = new HashSet<>(Arrays.asList(
        "test", "testing", "tester", "tests", "testimony", "testament"
    ));
    Lexer lexer = new Lexer();
    lexer.buildLexerTrie(keywords);
    String result = lexer.getOutputGraph();
    long tNodeCount = result.lines()
        .filter(line -> line.contains("-> t"))
        .count();
    assertEquals(1, tNodeCount,
        "Multiple 't' nodes found - consolidation incomplete. Found: " + tNodeCount);
  }

  @Test
  public void testLexerTrieNoNullInOutput() {
    Set<String> keywords = new HashSet<>(Arrays.asList("abc", "def"));
    Lexer lexer = new Lexer();
    lexer.buildLexerTrie(keywords);
    String output = lexer.getOutputGraph();
    assertFalse(output.contains("null"),
        "Output should not contain 'null', but got: '" + output + "'");
  }
}


