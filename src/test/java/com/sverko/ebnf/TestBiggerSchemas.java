package com.sverko.ebnf;
import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.nio.file.Path;
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
  }

  @Test
  public void testAddresbookParser() throws IOException {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser addressBookParser = generator.getParser(Path.of("src/main/resources/addressbook.ebnf"));
    addressBookParser.parse(Path.of("src/main/resources/addressbook.txt"));
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
