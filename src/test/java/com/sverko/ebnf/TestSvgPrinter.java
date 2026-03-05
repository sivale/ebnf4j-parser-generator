package com.sverko.ebnf;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class TestSvgPrinter {
  @Test
  public void testPrintingOfAntiNodes() throws IOException {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser testParser = generator.getParser(Path.of("src/main/resources/testSvgPrinter.ebnf"));
    SvgPrinter printer = new SvgPrinter(testParser.startNode);
    printer.printParseTreeToFile("/tmp/parse-tree.svg");
  }

  @Test
  public void testPrintingOfEbnfParseTree() throws IOException {
    SvgPrinter printer = new SvgPrinter(EbnfParseTree.getStartNode());
    printer.printParseTreeToFile("/tmp/main-parse-tree.svg");
  }


}
