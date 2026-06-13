package com.sverko.ebnf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sverko.ebnf.result.ResultNodeType;
import com.sverko.ebnf.result.TriviaResultNode;
import java.util.List;
import org.junit.jupiter.api.Test;

class TriviaDirectiveTests {

  @Test
  void appliesNamedTriviaToFollowingSequenceAndChoice() {
    Parser parser = new EbnfParserGenerator().getParser("""
        @trivia(DIGITS);
        A = 'A', ('B' | 'C');
        @trivia(none);
        DIGITS = { ?DIGIT? };
        """);

    assertEquals(5, parser.parse("A123B"));
    assertNotNull(parser.getResultTree().getRoot());
    assertEquals(List.of("123"), triviaTexts(parser));
  }

  @Test
  void payloadWinsBeforeTriviaAtChoice() {
    Parser parser = new EbnfParserGenerator().getParser("""
        @trivia(DIGITS);
        A = 'A', ('1' | 'B');
        @trivia(none);
        DIGITS = { ?DIGIT? };
        """);

    assertEquals(2, parser.parse("A1"));
    assertEquals(List.of(), triviaTexts(parser));
  }

  @Test
  void choiceTriesAllPayloadAlternativesBeforeSkippingTrivia() {
    Parser parser = new EbnfParserGenerator().getParser("""
        @trivia(DIGITS);
        A = 'one' | 'two';
        @trivia(none);
        DIGITS = { ?DIGIT? };
        """);

    assertEquals(4, parser.parse("123two"));
    assertEquals(List.of("123"), triviaTexts(parser));
  }

  @Test
  void noneDisablesAutomaticTriviaForFollowingRules() {
    Parser parser = new EbnfParserGenerator().getParser("""
        @trivia(DIGITS);
        A = 'A', 'B';
        @trivia(none);
        C = 'C', 'D';
        DIGITS = { ?DIGIT? };
        """);

    assertEquals(3, parser.parse("A1B"));
    assertEquals(ParseNode.NOT_FOUND, parseRule(parser, "C", "C1D"));
    assertEquals(ParseNode.NOT_FOUND, parseRule(parser, "C", "C D"));
    assertNull(parser.getResultTree().getRoot());
  }

  @Test
  void referencedRuleKeepsPolicyFromItsDefinition() {
    Parser parser = new EbnfParserGenerator().getParser("""
        @trivia(DIGITS);
        A = 'A', B;
        @trivia(none);
        B = 'B', 'C';
        DIGITS = { ?DIGIT? };
        """);

    assertEquals(4, parser.parse("A1BC"));
    assertEquals(ParseNode.NOT_FOUND, parser.parse("A1B2C"));
  }

  @Test
  void emptyDirectiveDisablesTrivia() {
    Parser parser = new EbnfParserGenerator().getParser("""
        @trivia(DIGITS);
        A = 'A', 'B';
        @trivia();
        C = 'C', 'D';
        DIGITS = { ?DIGIT? };
        """);

    assertEquals(ParseNode.NOT_FOUND, parseRule(parser, "C", "C1D"));
  }

  @Test
  void failedPayloadRollsBackTrailingTrivia() {
    Parser parser = new EbnfParserGenerator().getParser("""
        @trivia(DIGITS);
        A = 'A', 'B';
        @trivia(none);
        DIGITS = { ?DIGIT? };
        """);

    assertTrue(parser.parse("A123") < 0);
    assertNull(parser.getResultTree().getRoot());
  }

  @Test
  void appliesTriviaBetweenRepeatedPayloadElements() {
    Parser parser = new EbnfParserGenerator().getParser("""
        @trivia(DIGITS);
        A = { 'A' | 'B' };
        @trivia(none);
        DIGITS = { ?DIGIT? };
        """);

    assertEquals(3, parser.parse("A1B"));
    assertEquals(List.of("1"), triviaTexts(parser));
    assertEquals(List.of("DIGITS"), parser.getResultTree().readSequentially().stream()
        .filter(node -> node.getType() == ResultNodeType.TRIVIA)
        .map(node -> ((TriviaResultNode) node).getCategory())
        .toList());
  }

  private int parseRule(Parser parser, String ruleName, String input) {
    return parser.parse(parser.getLexer().lexText(input), parser.nodeMap.get(ruleName));
  }

  private List<String> triviaTexts(Parser parser) {
    return parser.getResultTree().readSequentially().stream()
        .filter(node -> node.getType() == ResultNodeType.TRIVIA)
        .map(node -> parser.getTokenQueue().getSubstring(node.getFromToken(), node.getToToken()))
        .toList();
  }
}
