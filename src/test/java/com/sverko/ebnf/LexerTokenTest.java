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
    assertEquals(expected, result, "Der Lexer sollte auch Surrogates korrekt parsen");
  }

  @Test
  public void lexerShouldAlsoTokenizeUnicodeTextWhenKeywordsAreGiven() {
    Set<String> tokens = new HashSet<>(Arrays.asList("😀q😀q", "😀q"));
    List<String> input = List.of("😀q😀q😀q");
    Lexer lexer = new Lexer(tokens);
    List<String> result = lexer.lexText(input);
    List<String> expected = List.of("😀q😀q","😀q");
    assertEquals(expected, result, "Der Lexer sollte Texte mit Surrogates in Tokens umwandeln");
  }

  @Test
  public void lexerShouldHonourIgnoreWhitespace() {
    List<String> input = List.of("a b");
    Lexer lexer = Lexer.builder().ignoreWhitespace(true).build();
    List<String> result = lexer.lexText(input);
    List<String> expected = List.of("a","b");
    assertEquals(expected, result, "Der Lexer soll Whitespace überspringen");
  }
  @Test
  public void lexerShouldIgnorePureWhitespaceLinesWithoutError() {
    Set<String> tokens = Set.of("a","b");
    List<String> input = List.of("   ", "a", "b  ", "  ");
    Lexer lexer = Lexer.builder().ignoreWhitespace(true).build();
    List<String> result = lexer.lexText(input);
    List<String> expected = List.of("a", "b");
    assertEquals(expected, result, "Der Lexer soll Zeilen mit nur Whitespace ignorieren und korrekt verarbeiten.");
  }
  @Test
  public void lexerShouldHonourIgnoreWhitespace2() {
    Set<String> tokens = Set.of("ab");
    List<String> input = List.of("a b");
    Lexer lexer = Lexer.builder().tokens(tokens).ignoreWhitespace(true).build();
    List<String> result = lexer.lexText(input);
    List<String> expected = List.of("ab");
    assertEquals(expected, result, "Der Lexer soll Whitespace überspringen");
  }

  @Test
  public void lexerShouldMatchFullTokens() throws IOException {
    // Token-Vokabular vorbereiten
    Set<String> tokens = new HashSet<>(Arrays.asList("if", "else", "i", "f"));

    // Lexer mit Tokenliste initialisieren
    Lexer lexer = new Lexer(tokens);

    // Eingabe vorbereiten (wie Dateiinhalt – zeilenweise als einzelne Zeichen)
    List<String> input = Arrays.asList("ifelse");

    // lexText anwenden
    List<String> result = lexer.lexText(input);

    // Erwartetes Ergebnis
    List<String> expected = List.of("if", "else");

    // Test
    assertEquals(expected, result, "Der Lexer sollte 'ifelse' korrekt in ganze Tokens zerlegen.");
  }

  @Test
  public void lexerShouldPreferLongestMatch() throws IOException {
    Set<String> tokens = new HashSet<>(Arrays.asList("i", "n", "in", "int", "integer"));
    Lexer lexer = new Lexer(tokens);

    List<String> input = Arrays.asList("integer");

    List<String> result = lexer.lexText(input);

    List<String> expected = List.of("integer");

    assertEquals(expected, result, "Der Lexer sollte den längstmöglichen Token ('integer') erkennen.");
  }

  @Test
  public void lexerShouldFallbackToSingleCharactersWhenNoTokensProvided() throws IOException {
    Lexer lexer = new Lexer(); // Kein Token-Set

    List<String> input = List.of("ifelse");

    List<String> result = lexer.lexText(input);

    List<String> expected = Arrays.asList("i", "f", "e", "l", "s", "e");

    assertEquals(expected, result, "Ohne Tokenliste sollte der Lexer in Einzeichen-Tokens zerlegen.");
  }

  @Test
  public void lexerShouldFindTokenEmbeddedInText() throws IOException {
    Set<String> tokens = Set.of("abc");
    Lexer lexer = new Lexer(tokens);

    List<String> input = List.of("xabcy");

    List<String> result = lexer.lexText(input);

    List<String> expected = List.of("x", "abc", "y");

    assertEquals(expected, result,
        "Der Lexer sollte bekannte Tokens auch erkennen, wenn sie eingebettet sind.");
  }

  @Test
  public void testPrintoutOfLexerTrieNo1() {
    Set<String> tokens = new HashSet<>(Arrays.asList("abcd"));
    Lexer lexer = new Lexer(tokens);
    lexer.buildLexerTree(tokens);
    lexer.printNodeGraph();
  }
  @Test
  public void testPrintoutOfLexerTrieNo2() {
    Set<String> tokens = new HashSet<>(Arrays.asList("abcd", "abrg"));
    Lexer lexer = new Lexer(tokens);
    lexer.buildLexerTree(tokens);
    lexer.printNodeGraph();
  }
  @Test
  public void testPrintoutOfLexerTrieNo3() {
    Set<String> tokens = new HashSet<>(Arrays.asList("abc", "def"));
    Lexer lexer = new Lexer(tokens);
    lexer.buildLexerTree(tokens);
    lexer.printNodeGraph();
  }
  @Test
  public void testPrintoutOfLexerTrieNo4() {
    Set<String> tokens = new HashSet<>(Arrays.asList("abc", "ank", "trg"));
    Lexer lexer = new Lexer(tokens);
    lexer.buildLexerTree(tokens);
    lexer.printNodeGraph();
  }
  @Test
  public void testPrintoutOfLexerTrieNo5() {
    Set<String> tokens = new HashSet<>(Arrays.asList("abcd", "awmu", "abdh", "trgf"));
    Lexer lexer = new Lexer(tokens);
    lexer.buildLexerTree(tokens);
    lexer.printNodeGraph();
  }
  @Test
  public void testLexerAcceptStrings() {
    Set<String> keywords = new HashSet<>(Arrays.asList("si","no"));
    Lexer lexer = new Lexer(keywords);
    List<String> tokens = lexer.lexText("abc\nsi\nef");
    assert(tokens.contains("si"));
  }
}


