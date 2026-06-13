package com.sverko.ebnf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sverko.ebnf.result.ResultNode;
import com.sverko.ebnf.result.ResultTree;
import com.sverko.ebnf.result.SimpleResultNode;
import com.sverko.ebnf.result.TriviaResultNode;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class TestResultTreeHtmlPrinter {

  @Test
  void createsStandaloneInteractiveTree() {
    Parser parser = new EbnfParserGenerator().getParser(
        "SENTENCE={WORD};WORD={: ?WHITESPACE? : ?LETTER? :};", true);
    assertEquals(13, parser.parse("one two three"));

    String html = new ResultTreeHtmlPrinter(parser.getResultTree()).toHtml();

    assertTrue(html.startsWith("<!doctype html>"));
    assertTrue(html.contains("ResultTree Inspector"));
    assertTrue(html.contains("class=\"tree root-tree\""));
    assertTrue(html.contains("<details open>"));
    assertTrue(html.contains(">SENTENCE</span>"));
    assertTrue(html.contains("one two three"));
    assertTrue(html.contains("Compact"));
    assertTrue(html.contains("Full"));
    assertTrue(html.contains("Expand all"));
    assertTrue(html.contains("Collapse all"));
    assertTrue(html.contains("Search rule or matched text"));
  }

  @Test
  void createsCompactGroupsForRepeatedSiblingNodes() {
    Parser parser = new EbnfParserGenerator().getParser(
        "CONTENT={LETTER};LETTER=?LETTER?;", true);
    assertEquals(6, parser.parse("abcdef"));

    String html = new ResultTreeHtmlPrinter(parser.getResultTree()).toHtml();

    assertTrue(html.contains("LETTER x 6"));
    assertTrue(html.contains("aggregate compact-only"));
    assertTrue(html.contains("tree-item branch full-only"));
  }

  @Test
  void keepsTerminalNodesForFullMode() {
    Parser parser = new EbnfParserGenerator().getParser("ROOT='a';", true);
    assertEquals(1, parser.parse("a"));

    String html = new ResultTreeHtmlPrinter(parser.getResultTree()).toHtml();

    assertTrue(html.contains("tree-item terminal full-only"));
    assertTrue(html.contains("body class=\"compact-mode\""));
    assertTrue(html.contains("body.full-mode .compact-only"));
  }

  @Test
  void escapesNodeContent() {
    ResultNode root = new SimpleResultNode("<ROOT&").setSpan(0, 2);
    root.setDownNode(new SimpleResultNode("<&", 0).setSpan(0, 2));
    ResultTree resultTree = new ResultTree(root);
    resultTree.setTokenQueue(TokenQueue.ofList("<", "&"));

    String html = new ResultTreeHtmlPrinter(resultTree).toHtml();

    assertTrue(html.contains("&lt;ROOT&amp;"));
    assertTrue(html.contains("&lt;&amp;"));
    assertFalse(html.contains("<ROOT&"));
  }

  @Test
  void rendersEmptyResultTree() {
    String html = new ResultTreeHtmlPrinter(new ResultTree()).toHtml();

    assertTrue(html.contains("No parse result"));
  }

  @Test
  void rendersAddressbookInCompactMode() throws Exception {
    Parser parser = new EbnfParserGenerator().getParser(
        Path.of("src/main/resources/addressbook.ebnf"));
    assertEquals(269, parser.parse(Path.of("src/main/resources/addressbook.txt")));
    assertEquals(478, parser.getResultTree().readSequentially().size());

    String html = new ResultTreeHtmlPrinter(parser.getResultTree()).toHtml();

    assertTrue(html.contains("478 nodes"));
    assertTrue(html.contains("LETTER x 9"));
    assertTrue(html.contains("aggregate compact-only"));
    assertTrue(html.contains("tree-item terminal full-only"));
    assertTrue(html.contains("trivia (ws)"));
  }

  @Test
  void rendersTriviaInCompactModeWithDedicatedType() {
    ResultNode root = new SimpleResultNode("ROOT").setSpan(0, 3);
    root.setDownNode(new TriviaResultNode("comment", 0, 3));
    ResultTree resultTree = new ResultTree(root);
    resultTree.setTokenQueue(TokenQueue.ofList("/", "/", "x"));

    String html = new ResultTreeHtmlPrinter(resultTree).toHtml();

    assertTrue(html.contains("tree-item trivia"));
    assertFalse(html.contains("tree-item trivia full-only"));
    assertTrue(html.contains("trivia (comment)"));
    assertTrue(html.contains("data-type=\"trivia\""));
  }

  @Test
  void writesUsingPathAndStringOverloads() throws Exception {
    ResultTreeHtmlPrinter printer = new ResultTreeHtmlPrinter(
        new ResultTree(new SimpleResultNode("ROOT")));
    Path pathOutput = Files.createTempFile("result-tree-path-", ".html");
    Path stringOutput = Files.createTempFile("result-tree-string-", ".html");

    printer.printResultTreeToFile(pathOutput);
    printer.printResultTreeToFile(stringOutput.toString());

    assertEquals(printer.toHtml(), Files.readString(pathOutput));
    assertEquals(printer.toHtml(), Files.readString(stringOutput));
  }
}
