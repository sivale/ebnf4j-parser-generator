package com.sverko.ebnf;

import com.sverko.ebnf.tools.StringLineToStringArrayListConvertor;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestIfGeneratedNodesParseCorrectly {

  public List<String> readInTestSamples(String path)
      throws IOException {
    FileInputStream fis = new FileInputStream(path);
    InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
    BufferedReader in = new BufferedReader(isr);
    List<String> stringList = new ArrayList<>();
    String line;
    while ((line = in.readLine()) != null) {
      stringList.add(line);
    }
    return stringList;
  }

  public static void main(String[] args) throws IOException {
    TestIfGeneratedNodesParseCorrectly tester = new TestIfGeneratedNodesParseCorrectly();
    System.out.println("Working Directory = " + System.getProperty("user.dir"));
    List<String> stringList = tester.readInTestSamples("src/main/resources/generated_tests.txt");

    int sampleNumber = 0;
    String sample="";
    StringBuilder parserDefinition = new StringBuilder();

    for(int i=0; i<stringList.size(); i++){
      if (!stringList.get(i).equals("")){
          String[] numberAndSample = stringList.get(i).split("\\s:\\s");
          if(numberAndSample.length > 1){
            sampleNumber = Integer.parseInt(numberAndSample[0]);
            sample = numberAndSample[1];
          } else {
            parserDefinition.append(stringList.get(i));
          }
      } else {

        List<String> sampleTokens = StringLineToStringArrayListConvertor.convert(sample);
        List<String> defTokens;
        defTokens = StringLineToStringArrayListConvertor.convert(parserDefinition.toString());
        parserDefinition.setLength(0);
        defTokens = new Lexer(Collections.EMPTY_SET).lexText(defTokens);
        EbnfParserGenerator generator = new EbnfParserGenerator();
        generator.startNode = EbnfParseTree.getStartNode();
        generator.loadEbnfSchema(defTokens);
        generator.processEbnfSchema();
        Parser parser = new Parser();
        int foundTokens = parser.parse(sampleTokens, generator.getFirstNode());
        if(foundTokens != sampleTokens.size()) {
          System.out.println(sampleNumber + ": ");
          System.out.println(
              "tokens searched " + sampleTokens.size() + " +  foundTokens + " + foundTokens);
        }
      }
    }
  }

}

