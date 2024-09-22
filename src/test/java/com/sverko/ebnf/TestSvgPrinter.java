package com.sverko.ebnf;

import java.io.IOException;

public class TestSvgPrinter {

  public void testPrintingOfAntiNodes() throws IOException {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser testParser = generator.getParser("src/main/resources/testSvgPrinter.ebnf");
    SvgPrinter printer = new SvgPrinter(testParser.startNode);
    printer.printParseTreeToFile("/tmp/parse-tree.svg");
  }

  public static void main(String[] args) throws IOException {
    SvgPrinter printer = new SvgPrinter(EbnfParseTree.getStartNode());
    printer.printParseTreeToFile("/tmp/main-parse-tree.svg");
  }

}
