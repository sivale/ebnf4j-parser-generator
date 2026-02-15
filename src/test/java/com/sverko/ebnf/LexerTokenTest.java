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
    String input = "😀q😀q😀q";
    TokenQueue result = lexer.lexText(input);
    TokenQueue expected = TokenQueue.ofList("😀", "q", "😀", "q", "😀", "q");
    assertEquals(expected, result, "lexer should handle surrogate pairs in text");
  }

  @Test
  public void lexerShouldAlsoTokenizeUnicodeTextWhenKeywordsAreGiven() {
    Set<String> tokens = new HashSet<>(Arrays.asList("😀q😀q", "😀q"));
    String input = "😀q😀q😀q";
    Lexer lexer = new Lexer(tokens);
    TokenQueue result = lexer.lexText(input);
    TokenQueue expected = TokenQueue.ofList("😀q😀q", "😀q");
    assertEquals(expected, result, "lexer should convert surrogate pairs in tokens");
  }

  @Test
  public void testEmojiAsToken() {
    // Emoji selbst ist ein Keyword
    Set<String> keywords = new HashSet<>(Arrays.asList("😊"));
    Lexer lexer = new Lexer(keywords);
    TokenQueue tokens = lexer.lexText("😊");
    assertEquals(1, tokens.rawSize());
    assertEquals("😊", tokens.get(0));
  }

  @Test
  public void testEmojiBetweenKeywords() {
    Set<String> keywords = new HashSet<>(Arrays.asList("A", "B"));
    Lexer lexer = new Lexer(keywords);
    TokenQueue tokens = lexer.lexText("A😊B");
    assertTrue(tokens.contains("A"));
    assertTrue(tokens.contains("B"));
    assertTrue(tokens.contains("😊"));
    assertFalse(tokens.contains("�"));
  }

  @Test
  public void testLexerAcceptKeywords() {
    Set<String> keywords = new HashSet<>(Arrays.asList("si", "no"));
    Lexer lexer = new Lexer(keywords);
    TokenQueue tokens = lexer.lexText("abc\nsi\nef");
    assert (tokens.contains("si"));
  }

  @Test
  public void testLexerOverflow_Re_Lexing() {
    Set<String> keywords = new HashSet<>(Arrays.asList("Haus", "meister"));
    Lexer lexer = new Lexer(keywords);
    TokenQueue tokens = lexer.lexText("Hausmeister");
    assertTrue(tokens.contains("Haus"));
    assertTrue(tokens.contains("meister"));
    assertFalse(tokens.contains("m"));
    assertFalse(tokens.contains("e"));
  }

  @Test
  public void testLexerRemainingCharacter() {
    Set<String> keywords = Set.of("Haus");
    Lexer lexer = new Lexer(keywords);
    TokenQueue tokens = lexer.lexText("HausX");
    assertTrue(tokens.contains("Haus"));
    assertTrue(tokens.contains("X"));
  }

  @Test
  public void lexerShouldMatchFullTokens() throws IOException {
    Set<String> tokens = new HashSet<>(Arrays.asList("if", "else", "i", "f"));
    Lexer lexer = new Lexer(tokens);
    String input = "ifelse";
    TokenQueue result = lexer.lexText(input);
    TokenQueue expected = TokenQueue.ofList("if", "else");
    assertEquals(expected, result, "lexer should match full tokens");
  }

  @Test
  public void lexerShouldPreferLongestMatch() throws IOException {
    Set<String> tokens = new HashSet<>(Arrays.asList("i", "n", "in", "int", "integer"));
    Lexer lexer = new Lexer(tokens);
    String input = "integer";
    TokenQueue result = lexer.lexText(input);
    TokenQueue expected = TokenQueue.ofList("integer");
    assertEquals(expected, result, "lexer should prefer longest match");
  }

  @Test
  public void lexerShouldFallbackToSingleCharactersWhenNoTokensProvided() throws IOException {
    Lexer lexer = new Lexer(); // No Token-Set
    String input = "ifelse";
    TokenQueue result = lexer.lexText(input);
    TokenQueue expected = TokenQueue.ofList("i", "f", "e", "l", "s", "e");
    assertEquals(expected, result,
        "lexer should fallback to single characters when no tokens provided");
  }

  @Test
  public void lexerShouldFindTokenEmbeddedInText() throws IOException {
    Set<String> tokens = Set.of("abc");
    Lexer lexer = new Lexer(tokens);
    String input = "xabcy";
    TokenQueue result = lexer.lexText(input);
    TokenQueue expected = TokenQueue.ofList("x", "abc", "y");
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

