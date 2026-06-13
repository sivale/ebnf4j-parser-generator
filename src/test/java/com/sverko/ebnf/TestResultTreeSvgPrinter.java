package com.sverko.ebnf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sverko.ebnf.result.ResultNode;
import com.sverko.ebnf.result.ResultTree;
import com.sverko.ebnf.result.SimpleResultNode;
import com.sverko.ebnf.result.TriviaResultNode;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

public class TestResultTreeSvgPrinter {

  @Test
  void printsParsedResultAsSvgTree() throws Exception {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser(
        "SENTENCE={WORD};WORD={: ?WHITESPACE? : ?LETTER? :};", true);

    assertEquals(7, parser.parse("one two"));

    String svg = new ResultTreeSvgPrinter(parser.getResultTree()).toSvg();
    var document = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(new InputSource(new StringReader(svg)));

    assertEquals("svg", document.getDocumentElement().getNodeName());
    assertEquals(6, document.getElementsByTagName("rect").getLength());
    assertEquals(5, document.getElementsByTagName("path").getLength());
    assertTrue(svg.contains(">SENTENCE<"));
    assertTrue(svg.contains(">one two<"));
    assertTrue(svg.contains(">WORD<"));
    assertTrue(svg.contains(">trivia (ws)<"));
    assertTrue(svg.contains(">tokens [0, 7)<"));
  }

  @Test
  void escapesNamesAndMatchedTextForXml() throws Exception {
    ResultNode root = new SimpleResultNode("ROOT<&").setSpan(0, 2);
    root.setDownNode(new SimpleResultNode("<&", 0).setSpan(0, 2));
    ResultTree resultTree = new ResultTree(root);
    resultTree.setTokenQueue(TokenQueue.ofList("<", "&"));

    String svg = new ResultTreeSvgPrinter(resultTree).toSvg();
    DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(new InputSource(new StringReader(svg)));

    assertTrue(svg.contains("ROOT&lt;&amp;"));
    assertTrue(svg.contains("&lt;&amp;"));
  }

  @Test
  void printsEmptyResultTree() {
    String svg = new ResultTreeSvgPrinter(new ResultTree()).toSvg();

    assertTrue(svg.contains("No parse result"));
  }

  @Test
  void displaysEmptyNonTerminalAsBranchNode() {
    String svg = new ResultTreeSvgPrinter(
        new ResultTree(new SimpleResultNode("EMPTY_RULE"))).toSvg();

    assertTrue(svg.contains("class=\"node branch\""));
  }

  @Test
  void displaysTriviaWithDedicatedStyle() {
    ResultNode root = new SimpleResultNode("ROOT").setSpan(0, 1);
    root.setDownNode(new TriviaResultNode("comment", 0, 1));
    ResultTree resultTree = new ResultTree(root);
    resultTree.setTokenQueue(TokenQueue.ofList("#"));

    String svg = new ResultTreeSvgPrinter(resultTree).toSvg();

    assertTrue(svg.contains("class=\"node trivia\""));
    assertTrue(svg.contains(">trivia (comment)<"));
  }

  @Test
  void writesSvgToFile() throws Exception {
    Parser parser = new EbnfParserGenerator().getParser(
        "SENTENCE={WORD};WORD={: ?WHITESPACE? : ?LETTER? :};", true);
    assertEquals(13, parser.parse("one two three"));

    ResultTreeSvgPrinter printer = new ResultTreeSvgPrinter(parser.getResultTree());
    Path output = Path.of("/tmp/result-tree.svg");
    printer.printResultTreeToFile(output);

    assertEquals(printer.toSvg(), Files.readString(output));
    assertTrue(Files.readString(output).contains(">SENTENCE<"));
    assertTrue(Files.readString(output).contains(">one two three<"));
  }

  @Test
  void writesSvgToFileUsingStringPath() throws Exception {
    ResultTreeSvgPrinter printer = new ResultTreeSvgPrinter(
        new ResultTree(new SimpleResultNode("ROOT")));
    Path output = Files.createTempFile("result-tree-string-path-", ".svg");

    printer.printResultTreeToFile(output.toString());

    assertEquals(printer.toSvg(), Files.readString(output));
  }
}
