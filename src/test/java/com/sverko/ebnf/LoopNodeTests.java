package com.sverko.ebnf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;

public class LoopNodeTests {
  @Test
  void testSimpleNegation(){
    Lexer lexer = new Lexer(Set.of("\\n"));
    TokenQueue shema = lexer.lexText("A='{',B,'}'; B=NO_BRACE, {NO_BRACE}; NO_BRACE=ANY-BRACES; BRACES='{'|'}'; ANY=?BMP?;");
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser(shema,true);
    int tokensFound = parser.parse("{ abc }");
    assertEquals(7, parser.tokenQueue.getLastTokenFound());
  }

}
