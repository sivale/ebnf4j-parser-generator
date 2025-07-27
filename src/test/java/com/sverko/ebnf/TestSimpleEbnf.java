package com.sverko.ebnf;

import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

public class TestSimpleEbnf {
   @Test
   public void testSimpleEbnf() throws IOException {
     EbnfParserGenerator generator = new EbnfParserGenerator();
     Parser parser = generator.getParser("src/main/resources/simple.ebnf");
     int i = parser.parse(List.of("A + B * 2"), parser.startNode);
   }
}
