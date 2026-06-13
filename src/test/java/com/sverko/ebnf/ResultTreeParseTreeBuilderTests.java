package com.sverko.ebnf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sverko.ebnf.result.ResultNodeType;
import com.sverko.ebnf.result.TriviaResultNode;
import com.sverko.ebnf.tools.NodeTreeComparisons;
import com.sverko.ebnf.tools.TerminalNodeFactory;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ResultTreeParseTreeBuilderTests {

  private ResultTreeParseTreeBuilder buildFromSchema(EbnfParserGenerator generator, String schema) {
    generator.getParser(schema, true);
    ResultTreeParseTreeBuilder builder = new ResultTreeParseTreeBuilder(generator);
    builder.build(generator.getResultTree());
    return builder;
  }

  @Test
  void testBuildsSimpleSchemaLikeEventBuilder() {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    ResultTreeParseTreeBuilder builder = buildFromSchema(generator, "A=B,'c';B='d','e';");

    assertTrue(NodeTreeComparisons.isSameStructure(generator.getFirstNode(), builder.getStartNode()));
  }

  @Test
  void testReusesCanonicalDefinitionsForRepeatedReferences() {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    ResultTreeParseTreeBuilder builder = buildFromSchema(generator, "A=B,B;B='x';");

    NonTerminalNode start = (NonTerminalNode) builder.getStartNode();
    PositionNode sequence = (PositionNode) start.getDownNode();
    ParseNode firstB = sequence.getDownNode();
    PositionNode secondPosition = (PositionNode) sequence.getRightNode();
    ParseNode secondB = secondPosition.getDownNode();

    assertSame(firstB, secondB);
    assertSame(builder.getNamedNodes().get("B"), firstB);
    assertSame(sequence, firstB.parent);
  }

  @Test
  void testBuildsCollectorBouncerBoundary() {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    ResultTreeParseTreeBuilder builder = buildFromSchema(generator, "A={:?WHITESPACE?:B};B='x';");

    LoopNode collector = (LoopNode) builder.getStartNode().getDownNode();
    ParseNode expectedBoundary = new PositionNode()
        .setDownNode(TerminalNodeFactory.createCharacterRangeBasedTerminalNode(
            Character::isWhitespace, "?WHITESPACE?"));

    assertNotNull(collector.getBouncerParseNode());
    assertTrue(NodeTreeComparisons.isSameStructure(expectedBoundary, collector.getBouncerParseNode()));
  }

  @Test
  void testBuildsCollectorKickoutBoundary() {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    ResultTreeParseTreeBuilder builder = buildFromSchema(generator, "A={:(?BMP?):'}':};");

    LoopNode collector = (LoopNode) builder.getStartNode().getDownNode();
    ParseNode expectedKickout = new PositionNode().setDownNode(TerminalNodeFactory.cstn("}"));
    Parser parser = new Parser(builder.getStartNode(), builder.getNamedNodes(),
        builder.getLexerTokens(), true);

    assertNotNull(collector.getKickoutParseNode());
    assertTrue(NodeTreeComparisons.isSameStructure(expectedKickout, collector.getKickoutParseNode()));
    assertEquals(2, parser.parse("ab}"));
  }

  @Test
  void testBuildsCollectorBouncerAndKickoutBoundary() {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    ResultTreeParseTreeBuilder builder = buildFromSchema(generator, "A={:?BMP?:?LETTER?:'}':};");

    LoopNode collector = (LoopNode) builder.getStartNode().getDownNode();
    ParseNode expectedKickout = new PositionNode().setDownNode(TerminalNodeFactory.cstn("}"));
    ParseNode expectedBoundary = new PositionNode().setDownNode(TerminalNodeFactory.createCharacterRangeBasedTerminalNode(Character::isBmpCodePoint, "?BMP?"));
    Parser parser = new Parser(builder.getStartNode(), builder.getNamedNodes(),
        builder.getLexerTokens(), true);

    assertNotNull(collector.getKickoutParseNode());
    assertNotNull(collector.getBouncerParseNode());
    assertTrue(NodeTreeComparisons.isSameStructure(expectedKickout, collector.getKickoutParseNode()));
    assertTrue(NodeTreeComparisons.isSameStructure(expectedBoundary, collector.getBouncerParseNode()));
    assertEquals("a 123 b 456".length(), parser.parse("a 123 b 456 }"));
  }

  @Test
  void testCollectorCapturesLeadingAndTrailingTriviaAroundPayload() {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    ResultTreeParseTreeBuilder builder = buildFromSchema(generator, "A={:?BMP?:B};B='x';");
    Parser parser = new Parser(builder.getStartNode(), builder.getNamedNodes(),
        builder.getLexerTokens(), true);

    int tokensFound = parser.parse("before x after");

    assertEquals(parser.getTokenQueue().rawSize(), tokensFound);
    assertNotNull(parser.getResultTree().getRoot());
    List<TriviaResultNode> triviaNodes = parser.getResultTree().readSequentially().stream()
        .filter(node -> node.getType() == ResultNodeType.TRIVIA)
        .map(TriviaResultNode.class::cast)
        .toList();
    assertEquals(2, triviaNodes.size());
    assertEquals("before ", parser.getTokenQueue().getSubstring(
        triviaNodes.get(0).getFromToken(), triviaNodes.get(0).getToToken()));
    assertEquals(" after", parser.getTokenQueue().getSubstring(
        triviaNodes.get(1).getFromToken(), triviaNodes.get(1).getToToken()));
  }

  @Test
  void testCollectorDoesNotSucceedWithoutPayload() {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    ResultTreeParseTreeBuilder builder = buildFromSchema(generator, "A={:?BMP?:B};B='x';");
    Parser parser = new Parser(builder.getStartNode(), builder.getNamedNodes(),
        builder.getLexerTokens(), true);

    assertEquals(0, parser.parse("only trivia"));
    assertNull(parser.getResultTree().getRoot());
  }

  @Test
  void testBuildsRulesWithTriviaDirective() {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    ResultTreeParseTreeBuilder builder = buildFromSchema(generator, """
        @trivia(DIGITS);
        A = 'A', ('B' | 'C');
        @trivia(none);
        DIGITS = { ?DIGIT? };
        """);
    Parser parser = new Parser(builder.getStartNode(), builder.getNamedNodes(),
        builder.getLexerTokens(), true);

    assertEquals(5, parser.parse("A123C"));
    List<TriviaResultNode> triviaNodes = parser.getResultTree().readSequentially().stream()
        .filter(node -> node.getType() == ResultNodeType.TRIVIA)
        .map(TriviaResultNode.class::cast)
        .toList();
    assertEquals(1, triviaNodes.size());
    assertEquals("DIGITS", triviaNodes.get(0).getCategory());
    assertEquals("123", parser.getTokenQueue().getSubstring(
        triviaNodes.get(0).getFromToken(), triviaNodes.get(0).getToToken()));
  }

  @Test
  void testCollectorOwnsOuterTriviaWhilePayloadUsesRuleTrivia() {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    ResultTreeParseTreeBuilder builder = buildFromSchema(generator, """
        @trivia(DIGITS);
        ROOT = {: ?BMP? : PAYLOAD };
        PAYLOAD = 'A', 'B';
        @trivia(none);
        DIGITS = { ?DIGIT? };
        """);
    Parser parser = new Parser(builder.getStartNode(), builder.getNamedNodes(),
        builder.getLexerTokens(), true);

    assertEquals(7, parser.parse("xxA1Byy"));
    List<TriviaResultNode> triviaNodes = parser.getResultTree().readSequentially().stream()
        .filter(node -> node.getType() == ResultNodeType.TRIVIA)
        .map(TriviaResultNode.class::cast)
        .toList();
    assertEquals(List.of("?BMP?", "DIGITS", "?BMP?"),
        triviaNodes.stream().map(TriviaResultNode::getCategory).toList());
    assertEquals(List.of("xx", "1", "yy"), triviaNodes.stream()
        .map(node -> parser.getTokenQueue().getSubstring(node.getFromToken(), node.getToToken()))
        .toList());
  }

  @Test
  void testAppliesRuleTriviaBetweenRepeatedPayloadElements() {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    ResultTreeParseTreeBuilder builder = buildFromSchema(generator, """
        @trivia(DIGITS);
        A = { 'A' | 'B' };
        @trivia(none);
        DIGITS = { ?DIGIT? };
        """);
    Parser parser = new Parser(builder.getStartNode(), builder.getNamedNodes(),
        builder.getLexerTokens(), true);

    assertEquals(3, parser.parse("A1B"));
    List<TriviaResultNode> triviaNodes = parser.getResultTree().readSequentially().stream()
        .filter(node -> node.getType() == ResultNodeType.TRIVIA)
        .map(TriviaResultNode.class::cast)
        .toList();
    assertEquals(1, triviaNodes.size());
    assertEquals("DIGITS", triviaNodes.get(0).getCategory());
  }
}
