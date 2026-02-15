package com.sverko.ebnf;
import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.nio.file.Path;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

public class TestBiggerShemas implements ParseNodeEventListener{

  @Test
  public void testNumberPlateRecognition() throws IOException {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Predicate<Integer> allowedSigns = (i) -> Character.UnicodeBlock.of(i) == UnicodeBlock.BASIC_LATIN && Character.isUpperCase(i) ||  "ÜÄÖ".contains(String.valueOf(Character.toChars(i)));
    generator.addSpecialSequence("?GERMAN_CAPITALS?",allowedSigns);
    Parser kennzeichenParser = generator.getParser(Path.of("src/main/resources/kennzeichen.ebnf"));
    kennzeichenParser.assignNodeEventListener("KENNZEICHEN",this);
    SvgPrinter printer = new SvgPrinter(kennzeichenParser.startNode);
    printer.printParseTreeToFile("/tmp/parse-tree.svg");
    kennzeichenParser.parse(Path.of("src/main/resources/kennzeichen.txt"));
  }

  @Test
  public void testAddresbookParser() throws IOException {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.addSchemaListener(this);
    Parser addressBookParser = generator.getParser(Path.of("src/main/resources/addressbook.ebnf"));
    addressBookParser.assignNodeEventListener("PHONE",this);
    addressBookParser.parse(Path.of("src/main/resources/addressbook.txt"));
  }

  @Test
  public void testBasicIdentationShema() throws IOException {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser identParser = generator.getParser(Path.of("src/main/resources/simple-indentation.ebnf"),false);
    //SvgPrinter printer = new SvgPrinter(identParser.startNode);
    //printer.printParseTreeToFile("/tmp/indent-tree.svg");
    identParser.assignNodeEventListener("ELEMENT",this);
    identParser.assignNodeEventListener("SUBELEMENT",this);
    identParser.parse(Path.of("src/main/resources/indented-syntax.txt"));
  }


  @Override
  public void parseNodeEventOccurred(ParseNodeEvent e) {
    System.out.println(e.getNode().name + ": " + e.getTrimmed());
  }
}
