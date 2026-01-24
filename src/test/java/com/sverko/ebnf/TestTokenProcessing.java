package com.sverko.ebnf;

import com.sverko.ebnf.tools.TerminalNodeFactory;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTokenProcessing {

  @Test
  void matchExpectedTokenMarksPayload() {
    TokenQueue q = new TokenQueue(List.of(new Token("a", TokenType.UNKNOWN)));
    TerminalNode node = TerminalNodeFactory.createSimpleTerminalNode("a");
    node.tokens = q;
    int result = node.callReceived(0);
    assertEquals(1, result);
    assertEquals(TokenType.PAYLOAD, q.getTokenObject(0).getType());
  }

  @Test
  void nonMatchWhitespaceMarksTrivia() {
    TokenQueue q = new TokenQueue(List.of(new Token(" ", TokenType.UNKNOWN)));
    TerminalNode node = TerminalNodeFactory.createSimpleTerminalNode("a");
    node.tokens = q; // falls ParseNode.tokens protected/package-private ist
    int result = node.callReceived(0);
    assertEquals(ParseNode.NOT_FOUND, result);
    assertEquals(TokenType.TRIVIA, q.getTokenObject(0).getType());
  }
}
