package com.sverko.ebnf;

import java.io.IOException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Assertions;
import com.sverko.ebnf.tools.CombinationsCreator;
import com.sverko.ebnf.tools.StringLineToStringArrayListConvertor;
import java.util.List;
import java.util.Collections;

public class TestProgramaticallyCreatedCombinations {
  @ParameterizedTest
  @MethodSource("provideCombinations")
  public void testParserWithCombinations(String input, String ebnfDefinition) throws IOException {
    // Step 1: Tokenize the input and EBNF definition using the existing logic
    List<String> sampleTokens = StringLineToStringArrayListConvertor.convert(input); // Tokenize input
    List<String> defTokens = StringLineToStringArrayListConvertor.convert(ebnfDefinition); // Tokenize EBNF definition

    // Step 2: Lex the tokens of the EBNF definition
    defTokens = new Lexer(Collections.emptySet()).lexText(defTokens);

    // Step 3: Generate the parser using the EBNF schema
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(defTokens);
    generator.processEbnfSchema();

    // Create the parser from the generated schema
    Parser parser = new Parser();

    // Step 4: Parse the tokenized input using the generated parser
    boolean parsingSuccessful;
    try {
      int foundTokens = parser.parse(sampleTokens, generator.getFirstNode());
      // Check if all tokens were successfully parsed
      parsingSuccessful = foundTokens == sampleTokens.size();
    } catch (Exception e) {
      parsingSuccessful = false; // Parsing failed, capture the error
    }

    // Assert that parsing was successful
    Assertions.assertTrue(parsingSuccessful, "Parsing failed for input: " + input + " with definition: " + ebnfDefinition);
  }

  // Method source to provide input-EBNF pairs
  public static List<String[]> provideCombinations() {
    return CombinationsCreator.getAllCombos(); // Fetch combinations
  }
}
