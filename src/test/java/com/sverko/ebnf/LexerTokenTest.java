package com.sverko.ebnf;

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
    List<String> expected = List.of("😀","q","😀","q","😀","q");
    assertEquals(expected, result, "lexer should handle surrogate pairs in text");
  }

  @Test
  public void lexerShouldAlsoTokenizeUnicodeTextWhenKeywordsAreGiven() {
    Set<String> tokens = new HashSet<>(Arrays.asList("😀q😀q", "😀q"));
    List<String> input = List.of("😀q😀q😀q");
    Lexer lexer = new Lexer(tokens);
    List<String> result = lexer.lexText(input);
    List<String> expected = List.of("😀q😀q","😀q");
    assertEquals(expected, result, "lexer should convert surrogate pairs in tokens");
  }

  @Test
  public void lexerShouldHonourIgnoreWhitespace() {
    List<String> input = List.of("a b");
    Lexer lexer = Lexer.builder().ignoreWhitespace(true).build();
    List<String> result = lexer.lexText(input);
    List<String> expected = List.of("a","b");
    assertEquals(expected, result, "lexer should ignore whitespace");
  }
  @Test
  public void lexerShouldIgnorePureWhitespaceLinesWithoutError() {
    Set<String> tokens = Set.of("a","b");
    List<String> input = List.of("   ", "a", "b  ", "  ");
    Lexer lexer = Lexer.builder().ignoreWhitespace(true).build();
    List<String> result = lexer.lexText(input);
    List<String> expected = List.of("a", "b");
    assertEquals(expected, result, "lexer should ignore pure whitespace lines");
  }
  @Test
  public void lexerShouldHonourIgnoreWhitespace2() {
    Set<String> tokens = Set.of("ab");
    List<String> input = List.of("a b");
    Lexer lexer = Lexer.builder().tokens(tokens).ignoreWhitespace(true).build();
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
    assertEquals(expected, result, "lexer should fallback to single characters when no tokens provided");
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
    -> a -> b -> r -> g*
                 |
                 c -> d*""";
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
    lexer.buildLexerTree(tokens);
    String expected = """
    -> a -> w -> m -> u*
       |    |
       |    b -> d -> h*
       |         |
       |         c -> d*
       t -> r -> g -> f*""";
    assertEquals(expected, lexer.getOutputGraph());
  }
  @Test
  public void testLexerAcceptStrings() {
    Set<String> keywords = new HashSet<>(Arrays.asList("si","no"));
    Lexer lexer = new Lexer(keywords);
    List<String> tokens = lexer.lexText("abc\nsi\nef");
    assert(tokens.contains("si"));
  }
  @Test
  public void lexerShouldGroupQuotedTerminalStrings_noKeywords_usingStringInput() {
    String input = "MYDEF = \"one two three\" , \"four\", \"five\";";
    Lexer lexer = Lexer.builder()
        .ignoreWhitespace(true)
        .preserveWhitespaceInQuotes(true)
        .build();
    List<String> result = lexer.lexText(input);
    List<String> quoted = new ArrayList<>();
    for (String t : result) {
      if (t.length() >= 2 && t.startsWith("\"") && t.endsWith("\"")) {
        quoted.add(t);
      }
    }

    List<String> expectedQuoted = List.of("\"one two three\"", "\"four\"", "\"five\"");
    assertEquals(expectedQuoted, quoted,
        "Each double-quoted terminal string must be a single token including quotes and inner spaces.");
  }

  @Test
  public void lexerShouldGroupQuotedTerminalStrings_withKeywords_usingStringInput() {
    String input = "MYDEF = \"one two three\" , \"four\", \"five\";";
    Set<String> tokens = new HashSet<>(Arrays.asList("MYDEF", "=", ",", ";"));
    Lexer lexer = Lexer.builder()
        .tokens(tokens)
        .ignoreWhitespace(true)
        .preserveWhitespaceInQuotes(true)
        .build();
    List<String> result = lexer.lexText(input);
    List<String> quoted = new ArrayList<>();
    for (String t : result) {
      if (t.length() >= 2 && t.startsWith("\"") && t.endsWith("\"")) {
        quoted.add(t);
      }
    }
    List<String> expectedQuoted = List.of("\"one two three\"", "\"four\"", "\"five\"");
    assertEquals(expectedQuoted, quoted,
        "With keywords active, each double-quoted terminal string must still be a single token.");
  }

  @Test
  public void lexerShouldNotGroupQuotedTerminalStrings_whenPreserveDisabled_noKeywords() {
    String input = "MYDEF = \"one two three\" , \"four\", \"five\";";
    Lexer lexer = Lexer.builder()
        .ignoreWhitespace(true)
        .preserveWhitespaceInQuotes(false)
        .build();
    List<String> result = lexer.lexText(input);
    boolean hasGroupedQuotedToken = result.stream()
        .anyMatch(t -> t.length() >= 2 && t.startsWith("\"") && t.endsWith("\""));
    assertFalse(hasGroupedQuotedToken,
        "Without preserveWhitespaceInQuotes, quoted regions must not become single tokens.");
  }
}


