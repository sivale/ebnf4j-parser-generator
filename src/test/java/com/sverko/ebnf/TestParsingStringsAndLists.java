package com.sverko.ebnf;

import com.sverko.ebnf.tools.UnicodeString;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TestParsingStringsAndLists {
  @Test
  public void testParsingStrings() throws IOException {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Lexer lexer = Lexer.builder().preserveWhitespaceInQuotes(false).build();
    TokenQueue shema = lexer.lexText("A = 'abc' | 'def';");
    Parser parser = generator.getParser(shema,true);
    parser.parse("abc");
  }
}
