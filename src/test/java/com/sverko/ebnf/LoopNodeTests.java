package com.sverko.ebnf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.sverko.ebnf.result.ResultTree;
import com.sverko.ebnf.tools.TerminalNodeFactory;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import org.junit.jupiter.api.Test;


public class LoopNodeTests {
  private ParseNode wsBouncer() {
    return new PositionNode("ws bouncer")
        .setDownNode(TerminalNodeFactory.createCharacterRangeBasedTerminalNode(Character::isWhitespace, "ws"));
  }

  private void setWhitespaceBouncerRecursively(ParseNode node) {
    setWhitespaceBouncerRecursively(node, Collections.newSetFromMap(new IdentityHashMap<>()));
  }

  private void setWhitespaceBouncerRecursively(ParseNode node, Set<ParseNode> visited) {
    if (node == null || !visited.add(node)) {
      return;
    }
    if (node instanceof LoopNode) {
      ((LoopNode) node).setBouncerParseNode(wsBouncer());
    }
    setWhitespaceBouncerRecursively(node.getDownNode(), visited);
    setWhitespaceBouncerRecursively(node.getRightNode(), visited);
  }

  @Test
  void testSimpleNegation(){
    Lexer lexer = new Lexer(Set.of("\\n"));
    TokenQueue schema = lexer.lexText("A=\u0027{\u0027,B,\u0027}\u0027; B=NO_BRACE, {NO_BRACE}; NO_BRACE=ANY-BRACES; BRACES=\u0027{\u0027|\u0027}\u0027; ANY=?BMP?;");
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser(schema,true);
    setWhitespaceBouncerRecursively(parser.startNode);
    int tokensFound = parser.parse("{ abc }");
    assertEquals(7, parser.tokenQueue.getLastTokenFound());
  }

  @Test
  void testBoundaryAccessorsRoundTrip() {
    LoopNode ln = new LoopNode();
    ParseNode bouncer = new PositionNode().setDownNode(TerminalNodeFactory.createSimpleTerminalNode("-"));
    ParseNode kickout = new PositionNode().setDownNode(TerminalNodeFactory.createSimpleTerminalNode("."));

    assertSame(ln, ln.setBouncerParseNode(bouncer));
    assertSame(ln, ln.setKickoutParseNode(kickout));
    assertSame(bouncer, ln.getBouncerParseNode());
    assertSame(kickout, ln.getKickoutParseNode());
  }

  @Test
  void testStructuralLoopUsesCustomBouncerToSkipSeparators() {
    LoopNode ln = new LoopNode();
    ln.tokens = TokenQueue.ofList("a", "-", "b");
    ln.setDownNode(TerminalNodeFactory.createCharacterRangeBasedTerminalNode(Character::isLetter, "letter"));
    ln.setBouncerParseNode(new PositionNode("dash bouncer")
        .setDownNode(TerminalNodeFactory.createSimpleTerminalNode("-")));

    assertEquals(3, ln.callReceived(0));
  }

  @Test
  void testCollectorLoopStopsAtSeparatorInsteadOfPumpingFurther() {
    LoopNode ln = new LoopNode(0, 0);
    ln.tokens = TokenQueue.ofList("a", " ", "b");
    ln.setDownNode(TerminalNodeFactory.createCharacterRangeBasedTerminalNode(Character::isLetter, "letter"));
    ln.setBouncerParseNode(wsBouncer());

    assertEquals(1, ln.callReceived(0));
  }

  @Test
  void testMatchWinsOverBouncerWhenPayloadIncludesWhitespace() {
    LoopNode ln = new LoopNode(0, 0);
    ln.tokens = TokenQueue.ofList("a", " ", "b");
    ln.setDownNode(TerminalNodeFactory.createCharacterRangeBasedTerminalNode(Character::isBmpCodePoint, "bmp"));
    ln.setBouncerParseNode(wsBouncer());

    assertEquals(3, ln.callReceived(0));
  }

  @Test
  void testKickoutWinsBeforeMatchAndBouncer() {
    LoopNode ln = new LoopNode();
    ln.tokens = TokenQueue.ofList("a", ".", "b");
    ln.setDownNode(TerminalNodeFactory.createCharacterRangeBasedTerminalNode(Character::isBmpCodePoint, "bmp"));
    ln.setKickoutParseNode(new PositionNode("dot kickout")
        .setDownNode(TerminalNodeFactory.createSimpleTerminalNode(".")));

    assertEquals(1, ln.callReceived(0));
  }

  @Test
  void testCharacterRanges_1() {
    LoopNode ln = new LoopNode();
    ln.tokens = TokenQueue.ofList("a", "}", "c");
    ln.setDownNode(TerminalNodeFactory.createCharacterRangeBasedTerminalNode(Character::isLetter, "letter"));
    assertEquals(1, ln.callReceived(0));
  }

  @Test
  void testCharacterRanges_2() {
    LoopNode ln = new LoopNode();
    ln.tokens = TokenQueue.ofList("a", "}", "c");
    ln.setDownNode(TerminalNodeFactory.createCharacterRangeBasedTerminalNode(Character::isLetter, "letter"));
    ln.setBouncerParseNode(TerminalNodeFactory.createCharacterRangeBasedTerminalNode(Character::isBmpCodePoint, "bmp"));
    assertEquals(3, ln.callReceived(0));
  }

  @Test
  void testCharacterRanges_3() {
    LoopNode ln = new LoopNode();
    ln.tokens = TokenQueue.ofList("a", "}", "c");
    ln.setDownNode(TerminalNodeFactory.createCharacterRangeBasedTerminalNode(Character::isLetter, "letter"));
    ln.setBouncerParseNode(TerminalNodeFactory.createCharacterRangeBasedTerminalNode(Character::isBmpCodePoint, "bmp"));
    ln.setKickoutParseNode(TerminalNodeFactory.cstn("}"));
    assertEquals(1, ln.callReceived(0));
  }

  @Test
  void testSchemaBasedCollectorLoop_1() {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    String schema = "A = {:?WHITESPACE?: B :'}': }; B = 'outside','{','inside',  '}';    ";
    String input = "outside { inside }";
    Parser parser = generator.getParser(schema);
    int tokensFound = parser.parse(input);
    assertEquals(4, tokensFound);
  }
}
