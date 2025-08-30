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
    Set<String> keywords = Set.of("ab");
    List<String> input = List.of("a b");
    Lexer lexer = Lexer.builder().keywords(keywords).ignoreWhitespace(true).build();
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
  public void lexerShouldPreserveWhitespaceInTerminalStringsIfToldSo() {
    String input = "MYDEF = \"one two three\" , \"four\", \"five\";";
    EbnfParserGenerator shemaParser = new EbnfParserGenerator();
    shemaParser.lexer = Lexer.builder().preserveWhitespaceInQuotes(true).build();
    List<String> singleCodepointList = shemaParser.lexer.lexText(input);
    Parser textParser = shemaParser.getParser(singleCodepointList,true);
    int foundTokens = textParser.parse("one two three four five");
    assert (foundTokens == 3);
  }

  @Test
  public void lexerShouldNotPreserveWhitespaceInTerminalStringsIfNotToldSo() {
    List<String> input = List.of("'a b'");
    Lexer lexer = Lexer.builder().ignoreWhitespace(true).preserveWhitespaceInQuotes(false).build();
    List<String> result = lexer.lexText(input);
    List<String> expected = List.of("'","a","b","'");
    assertEquals(expected, result, "lexer should ignore whitespace");
  }
}


