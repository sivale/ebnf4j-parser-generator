package com.sverko.ebnf;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TestSimpleEbnf {
   @Test
   public void testSimpleEbnf() throws IOException {
     EbnfParserGenerator generator = new EbnfParserGenerator();
     Parser parser = generator.getParser(Path.of("src/main/resources/simple.ebnf"));
     int i = parser.parse(List.of("A + B * 2"), parser.startNode);
   }
}