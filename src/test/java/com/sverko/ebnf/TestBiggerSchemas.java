package com.sverko.ebnf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sverko.ebnf.result.ResultNodeType;
import com.sverko.ebnf.result.TriviaResultNode;
import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

public class TestBiggerSchemas {

  @Test
  public void testNumberPlateRecognition() throws IOException {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Predicate<Integer> allowedSigns = (i) -> Character.UnicodeBlock.of(i) == UnicodeBlock.BASIC_LATIN && Character.isUpperCase(i) ||  "ÜÄÖ".contains(String.valueOf(Character.toChars(i)));
    generator.addSpecialSequence("?GERMAN_CAPITALS?",allowedSigns);
    Parser kennzeichenParser = generator.getParser(Path.of("src/main/resources/kennzeichen.ebnf"));
    SvgPrinter printer = new SvgPrinter(kennzeichenParser.startNode);
    printer.printParseTreeToFile("/tmp/parse-tree.svg");
    kennzeichenParser.parse(Path.of("src/main/resources/kennzeichen.txt"));
    new ResultTreeHtmlPrinter(kennzeichenParser.getResultTree())
        .printResultTreeToFile("/tmp/kennzeichen.html");
  }

  @Test
  public void testAddresbookParser() throws IOException {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser addressBookParser = generator.getParser(Path.of("src/main/resources/addressbook.ebnf"));
    addressBookParser.parse(Path.of("src/main/resources/addressbook.txt"));
    new ResultTreeHtmlPrinter(addressBookParser.getResultTree())
        .printResultTreeToFile("/tmp/addresbook.html");
  }

  @Test
  void testJenkinsfilePartsWithBmpTrivia() throws IOException {
    Path schemaPath = Path.of("src/test/resources/jenkinsfile.ebnf");
    Path inputPath = Path.of("src/test/resources/Jenkinsfile");

    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.getParser(schemaPath);

    ResultTreeParseTreeBuilder builder = new ResultTreeParseTreeBuilder(generator);
    builder.build(generator.getResultTree());
    Parser parser = new Parser(builder.getStartNode(), builder.getNamedNodes(),
        builder.getLexerTokens(), true);

    String input = Files.readString(inputPath).stripTrailing();
    int tokensFound = parser.parse(input);

    assertNotNull(parser.getResultTree().getRoot());
    assertEquals(parser.getTokenQueue().rawSize(), tokensFound);
    assertEquals(3, parser.getResultTree().readSequentially().stream()
        .filter(node -> "STAGE".equals(node.getName()))
        .count());

    List<TriviaResultNode> triviaNodes = parser.getResultTree().readSequentially().stream()
        .filter(node -> node.getType() == ResultNodeType.TRIVIA)
        .map(TriviaResultNode.class::cast)
        .toList();
    assertEquals(4, triviaNodes.size());
    assertTrue(triviaNodes.stream().allMatch(node -> "?BMP?".equals(node.getCategory())));
    assertTrue(triviaNodes.stream()
        .map(node -> parser.getTokenQueue().getSubstring(node.getFromToken(), node.getToToken()))
        .anyMatch(text -> text.contains("#!groovy")));
    assertTrue(triviaNodes.stream()
        .map(node -> parser.getTokenQueue().getSubstring(node.getFromToken(), node.getToToken()))
        .anyMatch(text -> text.contains("void promoteParentWithPullRequest")));

    new ResultTreeHtmlPrinter(parser.getResultTree())
        .printResultTreeToFile("/tmp/jenkinsfile.html");
  }

  @Test void testAmericanHistory() throws IOException {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser addressBookParser = generator.getParser(Path.of("src/main/resources/sentences.ebnf"));
    addressBookParser.parse(Path.of("src/main/resources/sentences.txt"));
  }

  @Test
  public void testBasicIndentationShema() throws IOException {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser identParser = generator.getParser(Path.of("src/main/resources/simple-indentation.ebnf"),false);
    //SvgPrinter printer = new SvgPrinter(identParser.startNode);
    //printer.printParseTreeToFile("/tmp/indent-tree.svg");
    identParser.parse(Path.of("src/main/resources/indented-syntax.txt"));
  }

  @Test
  public void testSimpleEbnf() throws IOException {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser parser = generator.getParser(Path.of("src/main/resources/simple.ebnf"));
    Lexer lexer = new Lexer();
    int i = parser.parse(lexer.lexText("2 + 3 * 4"), parser.startNode);
  }
}
