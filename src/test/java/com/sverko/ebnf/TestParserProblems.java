package com.sverko.ebnf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;

public class TestParserProblems {
  @Test
  void testLoopNodeWsHandlingProblem(){
    Lexer lexer = new Lexer(Set.of("\\n"));
    TokenQueue schema = lexer.lexText("EXPRESSION=TERM,{'+',TERM};TERM=FACTOR,{'*',FACTOR};FACTOR=INTEGER|'(',EXPRESSION,')';INTEGER=DIGIT,{DIGIT};DIGIT=?DIGIT?;");
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser(schema,true);
    int tokensFound = parser.parse("1 + 2");
    assertEquals(5, tokensFound);
  }

  @Test
  void analyseProblem(){
    Lexer lexer = new Lexer(Set.of("\\n"));
    TokenQueue schema = lexer.lexText("INTEGER=DIGIT,{DIGIT};DIGIT=?DIGIT?;");
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser(schema,true);
    int tokensFound = parser.parse("12");
    assertEquals(2, tokensFound);
  }

  @Test
  void analyseStructuralProblem(){
    Lexer lexer = new Lexer(Set.of("\\n"));
    TokenQueue schema = lexer.lexText("TOP={TWO_ITEMS};TWO_ITEMS=FIRST|SECOND;FIRST='first';SECOND='second';");
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser(schema,true);
    int tokensFound = parser.parse("first second");
    assertEquals(3, tokensFound);
  }
}
