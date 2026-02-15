package com.sverko.ebnf;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TestSimpleEbnf implements ParseNodeEventListener{
   @Test
   public void testSimpleEbnf() throws IOException {
     EbnfParserGenerator generator = new EbnfParserGenerator();
     generator.addSchemaListener(this);
     Parser parser = generator.getParser(Path.of("src/main/resources/simple.ebnf"));
     Lexer lexer = new Lexer();
     int i = parser.parse(lexer.lexText("2 + 3 * 4"), parser.startNode);
   }

  @Override
  public void parseNodeEventOccurred(ParseNodeEvent e) {
    System.out.println(e.getNode().name + " " + e.getTrimmed());
  }
}