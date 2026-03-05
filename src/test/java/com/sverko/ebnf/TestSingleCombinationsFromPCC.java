package com.sverko.ebnf;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestSingleCombinationsFromPCC {

  private boolean parse(String input, String ebnfDefinition) throws IOException {
    Lexer lexer = new Lexer();
    TokenQueue sampleTokens = lexer.lexText(input);
    TokenQueue defTokens = lexer.lexText(ebnfDefinition);

    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.propagateTokenQueueToAllNodes(defTokens);
    generator.processEbnfSchema();
    Parser parser = new Parser();

    int foundTokens = parser.parse(sampleTokens, generator.getFirstNode());
    return foundTokens == sampleTokens.size();
  }

  @Test
  public void test_bbb_with_repetition_or() throws IOException {
    String input = "bbb";
    String ebnfDefinition = "A = { \"a\" | \"b\" } ;";

    Assertions.assertTrue(
        parse(input, ebnfDefinition),
        "Parsing failed for input: " + input + " with definition: " + ebnfDefinition
    );
  }

  @Test
  public void test_ababab_with_repetition_concat() throws IOException {
    String input = "ababab";
    String ebnfDefinition = "A = { \"a\" , \"b\" } ;";

    Assertions.assertTrue(
        parse(input, ebnfDefinition),
        "Parsing failed for input: " + input + " with definition: " + ebnfDefinition
    );
  }

  @Test
  public void test_b_with_parentheses_or() throws IOException {
    String input = "b";
    String ebnfDefinition = "A = ( \"a\" | \"b\" ) ;";

    Assertions.assertTrue(
        parse(input, ebnfDefinition),
        "Parsing failed for input: " + input + " with definition: " + ebnfDefinition
    );
  }
}
