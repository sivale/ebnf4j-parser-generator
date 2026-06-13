package com.sverko.ebnf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.sverko.ebnf.result.ResultNode;
import com.sverko.ebnf.result.ResultNodeType;
import com.sverko.ebnf.result.ResultTree;
import com.sverko.ebnf.result.TriviaResultNode;
import com.sverko.ebnf.tools.TerminalNodeFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class ResultTreeTests {

  @Test
  void testThreeLevelResultTree() {
    Lexer lexer = new Lexer(Set.of("\\n"));
    TokenQueue schema = lexer.lexText("A=B; B=C; C='a','b';");
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser(schema, true);
    int tokensFound = parser.parse("ab");
    assertEquals(2, tokensFound);

    ResultNode root = parser.getLastResultStrand();
    assertNotNull(root);

    ResultNode bNode = root.getDownNode();
    ResultNode cNode = bNode.getDownNode();
    ResultNode textLeaf = cNode.getDownNode();

    assertEquals("A", root.getName());
    assertEquals("B", bNode.getName());
    assertEquals("C", cNode.getName());
    assertEquals("ab", textLeaf.getName());
    assertNull(textLeaf.getRightNode());
  }

  @Test
  void testRepeatedNonTerminalChildrenBuildATree() {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser("SENTENCE={WORD};WORD={: ?WHITESPACE? : ?LETTER? :};", true);
    int tokensFound = parser.parse("one two three");
    assertEquals(13, tokensFound);

    ResultNode root = parser.getLastResultStrand();
    assertNotNull(root);
    assertEquals("SENTENCE", root.getName());

    ResultNode word1 = root.getDownNode();
    ResultNode trivia1 = word1.getRightNode();
    ResultNode word2 = trivia1.getRightNode();
    ResultNode trivia2 = word2.getRightNode();
    ResultNode word3 = trivia2.getRightNode();

    assertEquals("WORD", word1.getName());
    assertEquals(ResultNodeType.TRIVIA, trivia1.getType());
    assertEquals("WORD", word2.getName());
    assertEquals(ResultNodeType.TRIVIA, trivia2.getType());
    assertEquals("WORD", word3.getName());
    assertNull(word3.getRightNode());

    assertEquals(3, trivia1.getFromToken());
    assertEquals(4, trivia1.getToToken());
    assertEquals(7, trivia2.getFromToken());
    assertEquals(8, trivia2.getToToken());
    assertEquals("one", word1.getDownNode().getName());
    assertEquals("two", word2.getDownNode().getName());
    assertEquals("three", word3.getDownNode().getName());
    assertNull(word1.getDownNode().getDownNode());
    assertNull(word2.getDownNode().getDownNode());
    assertNull(word3.getDownNode().getDownNode());
  }

  @Test
  void testResultNodesStoreTokenSpans() {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser("A=B;B='a','b';", true);
    int tokensFound = parser.parse("ab");
    assertEquals(2, tokensFound);

    ResultNode aNode = parser.getLastResultStrand();
    assertNotNull(aNode);
    ResultNode bNode = aNode.getDownNode();
    ResultNode textLeaf = bNode.getDownNode();

    assertEquals(0, aNode.getFromToken());
    assertEquals(2, aNode.getToToken());
    assertEquals(0, bNode.getFromToken());
    assertEquals(2, bNode.getToToken());
    assertEquals(0, textLeaf.getFromToken());
    assertEquals(2, textLeaf.getToToken());
  }

  @Test
  void testResultTreeReadsSequentially() {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser("A=B;B='a','b';", true);
    int tokensFound = parser.parse("ab");
    assertEquals(2, tokensFound);

    ResultTree resultTree = parser.getResultTree();
    assertNotNull(resultTree);
    assertNotNull(resultTree.getRoot());

    List<String> names = resultTree.readSequentially().stream()
        .map(ResultNode::getName)
        .collect(Collectors.toList());

    assertEquals(List.of("A", "B", "ab"), names);
  }

  @Test
  void testResultTreeReadsSequentialAssignments() {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser("A=B;B='a','b';", true);
    int tokensFound = parser.parse("ab");
    assertEquals(2, tokensFound);

    ResultTree resultTree = parser.getResultTree();
    assertNotNull(resultTree);

    assertEquals(List.of("A=ab", "B=ab", "ab=ab"),
        resultTree.readSequentiallyAsAssignments());
  }

  @Test
  void testResultTreeReadsCondensedAssignments() {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser("SENTENCE={WORD};WORD={: ?WHITESPACE? : ?LETTER? :};", true);
    int tokensFound = parser.parse("one two three");
    assertEquals(13, tokensFound);

    ResultTree resultTree = parser.getResultTree();
    assertNotNull(resultTree);
    assertEquals(List.of(
            "SENTENCE=one two three",
            "WORD=one",
            "WORD=two",
            "WORD=three"),
        resultTree.readSequentiallyAsCondensedAssignments());
  }

  @Test
  void testLoopBouncerAddsMergedTriviaToResultTree() {
    Parser parser = parserWithLetterLoopAndSeparatorBouncer();

    assertEquals(4, parser.parse(TokenQueue.ofList("a", "-", "-", "b")));

    List<ResultNode> nodes = parser.getResultTree().readSequentially();
    assertEquals(4, nodes.size());
    assertEquals(List.of(
            ResultNodeType.NON_TERMINAL,
            ResultNodeType.TERMINAL,
            ResultNodeType.TRIVIA,
            ResultNodeType.TERMINAL),
        nodes.stream().map(ResultNode::getType).collect(Collectors.toList()));

    TriviaResultNode trivia = (TriviaResultNode) nodes.get(2);
    assertEquals("separator", trivia.getCategory());
    assertEquals(1, trivia.getFromToken());
    assertEquals(3, trivia.getToToken());
    assertEquals("a", nodes.get(1).getName());
    assertEquals("trivia", nodes.get(2).getName());
    assertEquals("b", nodes.get(3).getName());
    assertEquals(List.of("ROOT=a--b", "a=a", "trivia=--", "b=b"),
        parser.getResultTree().readSequentiallyAsAssignments());
    assertEquals(List.of("ROOT=a--b"),
        parser.getResultTree().readSequentiallyAsCondensedAssignments());
  }

  @Test
  void testTrailingBouncerMatchIsNotAddedWhenLoopDoesNotConsumeIt() {
    Parser parser = parserWithLetterLoopAndSeparatorBouncer();

    assertEquals(1, parser.parse(TokenQueue.ofList("a", "-", "-")));

    List<ResultNode> nodes = parser.getResultTree().readSequentially();
    assertEquals(2, nodes.size());
    assertEquals(List.of(ResultNodeType.NON_TERMINAL, ResultNodeType.TERMINAL),
        nodes.stream().map(ResultNode::getType).collect(Collectors.toList()));
  }

  @Test
  void testFailedAlternativeRollsBackTrivia() {
    LoopNode loop = letterLoopWithSeparatorBouncer();
    PositionNode leftAlternative = new PositionNode();
    leftAlternative.setDownNode(loop);
    leftAlternative.setRightNode(new PositionNode()
        .setDownNode(TerminalNodeFactory.cstn("c")));

    OrNode alternatives = new OrNode();
    alternatives.setDownNode(leftAlternative);
    alternatives.setRightNode(new OrNode()
        .setDownNode(new PositionNode().setDownNode(TerminalNodeFactory.cstn("a"))));

    NonTerminalNode root = new NonTerminalNode("ROOT");
    root.setDownNode(alternatives);
    Parser parser = new Parser(root, Map.of("ROOT", root), Set.of("-"), true);

    assertEquals(1, parser.parse(TokenQueue.ofList("a", "-", "b")));
    assertEquals(List.of(ResultNodeType.NON_TERMINAL, ResultNodeType.TERMINAL),
        parser.getResultTree().readSequentially().stream()
            .map(ResultNode::getType)
            .collect(Collectors.toList()));
  }

  private Parser parserWithLetterLoopAndSeparatorBouncer() {
    NonTerminalNode root = new NonTerminalNode("ROOT");
    root.setDownNode(letterLoopWithSeparatorBouncer());
    return new Parser(root, Map.of("ROOT", root), Set.of("-"), true);
  }

  private LoopNode letterLoopWithSeparatorBouncer() {
    LoopNode loop = new LoopNode();
    loop.setDownNode(TerminalNodeFactory.createCharacterRangeBasedTerminalNode(
        Character::isLetter, "letter"));
    loop.setBouncerParseNode(new PositionNode("separator bouncer")
        .setDownNode(TerminalNodeFactory.createCharacterRangeBasedTerminalNode(
            codePoint -> codePoint == '-', "separator")));
    return loop;
  }
}
