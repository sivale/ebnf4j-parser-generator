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
    SvgPrinter printer = new SvgPrinter(kennzeichenParser.startNode);
    printer.printParseTreeToFile("/tmp/kennzeichen.svg");
    kennzeichenParser.assignNodeEventListener("KENNZEICHEN",this);
    //kennzeichenParser.assignNodeEventListener("LASTNAME",this);
    kennzeichenParser.parse("src/main/resources/kennzeichen.txt");
  }
  @Test
  public void testAddresbookParser() throws IOException {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser addressBookParser = generator.getParser(Path.of("src/main/resources/addressbook.ebnf"));
    //SvgPrinter printer = new SvgPrinter(kennzeichenParser.startNode);
    addressBookParser.assignNodeEventListener("PHONE",this);
    //kennzeichenParser.assignNodeEventListener("LASTNAME",this);
    addressBookParser.parse("src/main/resources/addressbook.txt");
  }
  @Test
  public void testBasicIdentationShema() throws IOException {
    EbnfParserGenerator generator = new EbnfParserGenerator();
    Parser identParser = generator.getParser(Path.of("src/main/resources/simple-indentation.ebnf"),false);
    //SvgPrinter printer = new SvgPrinter(identParser.startNode);
    //printer.printParseTreeToFile("/tmp/indent-tree.svg");
    identParser.assignNodeEventListener("ELEMENT",this);
    identParser.assignNodeEventListener("SUBELEMENT",this);
    identParser.parse("src/main/resources/indented-syntax.txt");
  }

  @Override
  public void parseNodeEventOccurred(ParseNodeEvent e) {
    System.out.println(e.parseNode.name);
    System.out.println(e.resultString);
  }
}
