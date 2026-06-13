package com.sverko.ebnf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSvgPrinter {

  private static final Pattern NON_TERMINAL_TEXT_PATTERN = Pattern.compile(
      "<text x='([0-9.]+)' y='([0-9.]+)' font-family='sans-serif' font-size='([0-9.]+)px' text-anchor='middle' dominant-baseline='middle'>([^<]*)</text>"
  );

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

  @Test
  public void testNonTerminalTextScalesToAvailableWidth() throws IOException {
    SvgPrinter printer = new SvgPrinter(new NonTerminalNode("WWWWWWWWWW"));
    Path outputFile = Files.createTempFile("svg-printer-width-", ".svg");
    printer.printParseTreeToFile(outputFile.toString());

    List<SvgTextLine> textLines = extractNonTerminalTextLines(Files.readString(outputFile));

    assertEquals(1, textLines.size());
    assertEquals(125.0, textLines.get(0).x(), 0.0001);
    assertEquals(125.0, textLines.get(0).y(), 0.0001);
    assertEquals(12.0 * (60.0 / printer.getStringWidth("WWWWWWWWWW")), textLines.get(0).fontSize(), 0.0001);
    assertEquals("WWWWWWWWWW", textLines.get(0).text());
  }

  @Test
  public void testNonTerminalTextScalesToAvailableHeightAndStaysCentered() throws IOException {
    SvgPrinter printer = new SvgPrinter(new NonTerminalNode("ONE TWO THREE FOUR"));
    Path outputFile = Files.createTempFile("svg-printer-height-", ".svg");
    printer.printParseTreeToFile(outputFile.toString());

    List<SvgTextLine> textLines = extractNonTerminalTextLines(Files.readString(outputFile));

    assertEquals(4, textLines.size());
    assertEquals("ONE", textLines.get(0).text());
    assertEquals("TWO", textLines.get(1).text());
    assertEquals("THREE", textLines.get(2).text());
    assertEquals("FOUR", textLines.get(3).text());

    double expectedFontSize = 40.0 / (4 * 1.2);
    assertEquals(expectedFontSize, textLines.get(0).fontSize(), 0.0001);
    assertEquals(expectedFontSize, textLines.get(1).fontSize(), 0.0001);
    assertEquals(expectedFontSize, textLines.get(2).fontSize(), 0.0001);
    assertEquals(expectedFontSize, textLines.get(3).fontSize(), 0.0001);

    assertEquals(125.0, textLines.get(0).x(), 0.0001);
    assertEquals(125.0, textLines.get(1).x(), 0.0001);
    assertEquals(125.0, textLines.get(2).x(), 0.0001);
    assertEquals(125.0, textLines.get(3).x(), 0.0001);

    assertEquals(110.0, textLines.get(0).y(), 0.0001);
    assertEquals(120.0, textLines.get(1).y(), 0.0001);
    assertEquals(130.0, textLines.get(2).y(), 0.0001);
    assertEquals(140.0, textLines.get(3).y(), 0.0001);
  }

  private List<SvgTextLine> extractNonTerminalTextLines(String svg) {
    Matcher matcher = NON_TERMINAL_TEXT_PATTERN.matcher(svg);
    List<SvgTextLine> textLines = new ArrayList<>();
    while (matcher.find()) {
      textLines.add(new SvgTextLine(
          Double.parseDouble(matcher.group(1)),
          Double.parseDouble(matcher.group(2)),
          Double.parseDouble(matcher.group(3)),
          matcher.group(4)
      ));
    }
    return textLines;
  }

  private record SvgTextLine(double x, double y, double fontSize, String text) {}

}
