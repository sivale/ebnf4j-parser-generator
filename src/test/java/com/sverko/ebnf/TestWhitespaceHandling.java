package com.sverko.ebnf;

import static com.sverko.ebnf.ParseNode.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

public class TestWhitespaceHandling implements ParseNodeEventListener {

  @Test
  public void testUnhandledWhitespaceRecognitionPositive(){
    Lexer lexer = new Lexer();
    TokenQueue shema = lexer.lexText("A=' ';");
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser(shema,true);
    int tokensFound = parser.parse(" ");
    assert(tokensFound == 1);
  }

  @Test
  public void testUnhandledWhitespaceRecognitionNegative(){
    Lexer lexer = new Lexer();
    TokenQueue shema = lexer.lexText(" A= 'a';");
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser(shema,true);
    int tokensFound = parser.parse(" ");
    assert(tokensFound == NOT_FOUND);
  }

  @Test
  public void testUnhandledWhitespaceJumpOver(){
    Lexer lexer = new Lexer();
    TokenQueue shema = lexer.lexText("A='a';");
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser(shema,true);
    int tokensFound = parser.parse(" a");
    assert(tokensFound == 2);
  }

  @Test
  public void testUnhandledWhitespaceInBetween(){
    Lexer lexer = new Lexer();
    TokenQueue shema = lexer.lexText("A='a','b';");
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser(shema,true);
    int tokensFound = parser.parse("a b");
    assert(tokensFound == 3);
  }

  @Test
  void testUnhandledWhitespaceBeforeAndAfter(){
    Lexer lexer = new Lexer();
    String shema = ("A='a';");
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser(shema);
    int tokensFound = parser.parse(" a ");
    assert(tokensFound == 2); // the tokens beyond the end of findable tokens are not counted
  }

  @Test
  void testUnhandledWhitespaceInBetweenAndAfter(){
    Lexer lexer = new Lexer();
    String shema = ("A='a','b';");
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser(shema);
    int tokensFound = parser.parse("a b ");
    assert(tokensFound == 3);
  }

  @Test
  void testUnhandledWhitespaceInBetweenAndBefore(){
    Lexer lexer = new Lexer();
    String shema = ("A='a','b';");
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser(shema);
    int tokensFound = parser.parse(" a b");
    assert(tokensFound == 4);
  }

  @Test
  void testUnhandledWhitespaceInBetweenAndBeforeAndAfter(){
    Lexer lexer = new Lexer();
    String shema = ("A='a','b';");
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser(shema);
    int tokensFound = parser.parse(" a b ");
    assert(tokensFound == 4);
  }

  @Test
  void testSequenceWhereWhitespaceIsExpectedAsSecondInput(){
    Lexer lexer = new Lexer();
    TokenQueue shema = lexer.lexText("A='a',' ';");
    Parser parser = new EbnfParserGenerator().getParser(shema,true);
    int tokensFound = parser.parse("a ");
    assert(tokensFound == 2);
  }

  @Test
  void testMultipleUnhandledWhitespaceJumpOver(){
    Lexer lexer = new Lexer();
    TokenQueue shema = lexer.lexText("A='a';");
    Parser parser = new EbnfParserGenerator().getParser(shema,true);
    int tokensFound = parser.parse("   a");  // multiple WS
    assert(tokensFound == 4);
  }

  @Test
  void testHandledWhitespaceCountsAsPayload(){
    Lexer lexer = new Lexer();
    TokenQueue shema = lexer.lexText("A=' ','a';");
    Parser parser = new EbnfParserGenerator().getParser(shema,true);
    int tokensFound = parser.parse(" a");
    assert(tokensFound == 2); // ' ' (handled) + 'a'
  }

  @Test
  void testOrRollbackRestoresUnhandledWhitespace(){
    Lexer lexer = new Lexer();
    // example: A = (' ', 'x') | 'a';
    TokenQueue shema = lexer.lexText("A=(' ','x')|'a';");
    Parser parser = new EbnfParserGenerator().getParser(shema,true);
    int tokensFound = parser.parse(" a");
    assert(tokensFound == 2); //only 'a' is counted, leading WS is unhandled
  }

  @Test
  void testAntiNodeAllowsWsPrefixedAllowedToken(){
    Lexer lexer = new Lexer();
    TokenQueue shema = lexer.lexText("A=ANY-B,'b';B='b';ANY=?LETTER?;");
    Parser parser = new EbnfParserGenerator().getParser(shema,true);
    int tokensFound = parser.parse(" ab"); // UW before 'b'
    assert(tokensFound == 3);
  }

  @Test
  void testAntiNodeForbidsWsPrefixedForbiddenToken(){
    Lexer lexer = new Lexer();
    TokenQueue shema = lexer.lexText("A=ANY-B,'b';B='b';ANY=?LETTER?;");
    Parser parser = new EbnfParserGenerator().getParser(shema,true);
    int tokensFound = parser.parse(" bb"); // UW before 'b'
    assert(tokensFound == NOT_FOUND);
  }

  @Test
  void testWhitespaceOnlyFails(){
    Lexer lexer = new Lexer();
    TokenQueue shema = lexer.lexText("A='a';");
    Parser parser = new EbnfParserGenerator().getParser(shema,true);
    int tokensFound = parser.parse("   ");
    assert(tokensFound == NOT_FOUND);
  }
  @Test
  void testBreakingOfSimilarTokensByWhitespace(){
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser("A=FIRST,' ',SECOND;FIRST={:?LETTER?};SECOND={?LETTER?};");
    parser.assignNodeEventListeners(this);
    int tokensFound = parser.parse("first second");
    assert(tokensFound == 12);
  }

  @Test
  void testCombiningSimilarTokensWithWhitespace(){
    Lexer lexer = new Lexer();
    String shema = "A=FIRST,SECOND;FIRST={:?LETTER?};SECOND={:?LETTER?};";
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser(shema);
    parser.assignNodeEventListeners(this);
    int tokensFound = parser.parse("first second");
    assert(tokensFound == 12);
  }
  @Test
  void testCompositionWithParentAndChild(){
    Lexer lexer = new Lexer();
    TokenQueue shema = lexer.lexText("SENTENCE = {WORD};WORD = {?LETTER?};");
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser(shema,true);
    int tokensFound = parser.parse("abc def");
    assert(tokensFound == 7);
  }

  @Test
  void testTerminalStringsWithWsRecognition(){
    Lexer lexer = new Lexer();
    TokenQueue shema = lexer.lexText("SL = 'hello world';");
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser(shema,true);
    int tokensFound = parser.parse("hello world");
    assert(tokensFound == 1);
  }

  @Test
  void testLineSeparationLiterals(){
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser("LINES = LINE1, '\n', LINE2; LINE1 = {:?LETTER?}; LINE2 = {:?LETTER?};");
    int tokensFound = parser.parse("first\nsecond");
    assertEquals(12, tokensFound);
  }

  @Test
  void testNewLineTerminalStringExtension(){
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser("LINES = LINE1, \\n, LINE2; LINE1 = {:?LETTER?}; LINE2 = {:?LETTER?};");
    int tokensFound = parser.parse("first\nsecond");
    assertEquals(12, tokensFound);
  }

  @Test
  void testNewLineTerminalStringConversion(){
    Lexer lexer = new Lexer();
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser("LINES = LINE1, '\\n', LINE2; LINE1 = {:?LETTER?}; LINE2 = {:?LETTER?};");
    int tokensFound = parser.parse("first\nsecond");
    assertEquals(12, tokensFound);
  }

  @Override
  public void parseNodeEventOccurred(ParseNodeEvent e) {
    System.out.println(e.getNode().name + " " + e.getTrimmed());
  }
}
