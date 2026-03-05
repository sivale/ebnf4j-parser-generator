package com.sverko.ebnf;

import com.sverko.ebnf.tools.UnicodeString;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TestParsingStringsAndLists {
  @Test
  public void testParsingStrings() throws IOException {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Lexer lexer = new Lexer();
    TokenQueue schema = lexer.lexText("A = 'abc' | 'def';");
    Parser parser = generator.getParser(schema,true);
    parser.parse("abc");
  }
}
