package com.sverko.ebnf;

import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

public class TestParsingStringsAndLists {
  @Test
  public void testParsingStrings() throws IOException {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Lexer lexer = Lexer.builder().preserveWhitespaceInQuotes(false).build();
    List<String> shema = lexer.lexText("A = 'abc' | 'def';");
    Parser parser = generator.getParser(shema,true);
    parser.parse("abc");
  }
}
