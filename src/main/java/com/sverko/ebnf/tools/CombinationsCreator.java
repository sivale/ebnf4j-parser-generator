package com.sverko.ebnf.tools;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class CombinationsCreator {
  public static List<String> elements1 = List.of("\"a\"","P","?EMOTICONS?");
  public static List<String> elements2 = List.of("\"b\"","Q","?CYRILLIC?");
  public static List<String> elements3 = List.of("\"c\"","R","?BASIC_LATIN?");
  public static List<String> exceptionElements = List.of("J - \"k\"", "J - K"); //, "(-\"k\")", "(-K)" are not allowed by the ebnf syntax
  public static List<String> openingBraces = List.of("{","[","(");
  public static List<String> closingBraces = List.of("}","]",")");
  public static List<String> operators = List.of("|",",");
  public static String terminator = ";";

  public static List<String[]> createSingleElementCombinations (){
    List<String[]> combinations = new ArrayList<>();
    for (String element : elements1){
      String input = createSingleElementInputStrings(element);
      String ebnfDefinition = ("A = ");
      ebnfDefinition += (element+" ");
      ebnfDefinition += (terminator);
      if (element.equals("P") || element.equals("-p")){
        ebnfDefinition += ("P = \"p\" ;");
      }
    }
    return combinations;
  }

  public static List<String[]> createSingleExceptionElementCombinations (){
    List<String[]> combinations = new ArrayList<>();
    for (String element : exceptionElements){
      String input = createSingleElementInputStrings(element);
      String ebnfDefinition = "A = ";
      ebnfDefinition +=  element+" ";
      ebnfDefinition += (terminator);
      if (element.contains("J")){
        ebnfDefinition += ("J = \"j\"|\"k\"|\"l\" ;");
      }
      if (element.contains("K")){
        ebnfDefinition += ("K = \"k\"|\"l\" ;");
      }
      combinations.add(new String[]{input, ebnfDefinition});
    }
  return combinations;
  }

  public static List<String[]> createEmbracedSingleElementCombinations (){
    List<String[]> combinations = new ArrayList<>();
    for(int i=0; i<openingBraces.size(); i++) {
      for (String element : elements1) {
        String input = createSingleEmbracedElementInputStrings(element, openingBraces.get(i));
        String ebnfDefinition = ("A = ");
        ebnfDefinition += (openingBraces.get(i)+" ");
        ebnfDefinition += (element + " ");
        ebnfDefinition += (closingBraces.get(i)+" ");
        ebnfDefinition += (terminator);
        if (element.equals("P") || element.equals("-P")){
          ebnfDefinition += ("P = \"p\" ;");
        }
        combinations.add(new String[]{input, ebnfDefinition});
      }
    }
    return combinations;
  }

  public static List<String[]> createTwoElementsCombinations (){
    List<String[]> combinations = new ArrayList<>();
      for (String element1 : elements1) {
        for (String element2 : elements2) {
          for (String operator : operators) {
              String input = createTwoCombinedElementsInputStrings(element1, element2, operator);
              String ebnfDefinition = ("A = ");
              ebnfDefinition += (element1 + " ");
              ebnfDefinition += (operator + " ");
              ebnfDefinition += (element2 + " ");
              ebnfDefinition += (terminator);
            if (element1.equals("P") || element1.equals("-P")) {
              ebnfDefinition += ("P = \"p\" ;");
            }
            if (element2.equals("Q") || element2.equals("-Q")) {
              ebnfDefinition += ("Q = \"q\" ;");
            }
            combinations.add(new String[]{input, ebnfDefinition});
          }
        }
      }
    return combinations;
    }

  public static List<String[]> createTwoEmbracedElementsCombinations (){
  List<String[]> combinations = new ArrayList<>();
  for (int i = 0; i < openingBraces.size(); i++) {
    for (String element1 : elements1) {
      for (String element2 : elements2) {
        for (String operator : operators) {
          String input = createTwoCombinedElementsInputStrings(openingBraces.get(i), element1, element2, operator);
          String ebnfDefinition = ("A = ");
          ebnfDefinition += (openingBraces.get(i)+" ");
          ebnfDefinition += (element1 + " ");
          ebnfDefinition += (operator + " ");
          ebnfDefinition += (element2 + " ");
          ebnfDefinition += (closingBraces.get(i)+" ");
          ebnfDefinition += (terminator);
          if (element1.equals("P") || element1.equals("-P")) {
            ebnfDefinition += ("P = \"p\" ;");
          }
          if (element2.equals("Q") || element2.equals("-Q")) {
            ebnfDefinition += ("Q = \"q\" ;");
          }
          combinations.add(new String[]{input, ebnfDefinition});
        }
      }
    }
  }
  return combinations;
}

  public static List<String[]> createOneElementBeforeTwoEmbracedElementsCombinations() {
    List<String[]> combinations = new ArrayList<>();
    for (int i = 0; i < openingBraces.size(); i++) {
      for (String element1 : elements1) {
        for (String element2 : elements2) {
          for (String element3 : elements3) {
            for (String operator1 : operators) {
              for (String operator2 : operators) {
                String input = createOneElemPrecedingBracedTwoElementsInputString(openingBraces.get(i),element1,element2,element3,operator1,operator2);
                String ebnfDefinition = "A = " + element1 + " " + operator1 + " " + openingBraces.get(i) + " "
                                               + element2 + " " + operator2 + " "
                                               + element3 + " " + closingBraces.get(i) + " " + terminator;

                createOneElemPrecedingBracedTwoElementsInputString(openingBraces.get(i),element1,element2,element3,operator1,operator2);

                if (element1.equals("P") || element1.equals("-P")) {
                  ebnfDefinition += "P = \"p\" ;";
                }
                if (element2.equals("Q") || element2.equals("-Q")) {
                  ebnfDefinition += "Q = \"q\" ;";
                }
                if (element3.equals("R") || element3.equals("-R")) {
                  ebnfDefinition += "R = \"r\" ;";
                }
                combinations.add(new String[]{input, ebnfDefinition});
              }
            }
          }
        }
      }
    }
    return combinations;
  }

  private static List<String[]> createOneElementAfterTwoEmbracedElementsCombinations() {
    List<String[]> combinations = new ArrayList<>();
    for (int i = 0; i < openingBraces.size(); i++) {
      for (String element1 : elements1) {
        for (String element2 : elements2) {
          for (String element3 : elements3) {
            for (String operator1 : operators) {
              for (String operator2 : operators) {
                String input = createOneElementAfterTwoBracedElementsInputString(openingBraces.get(i), element1, element2,element3,operator1,operator2);
                String ebnfDefinition = "A = ";
                ebnfDefinition += (openingBraces.get(i) + " ");
                ebnfDefinition += (element1 + " ");
                ebnfDefinition += (operator1 + " ");
                ebnfDefinition += (element2 + " ");
                ebnfDefinition += (closingBraces.get(i) + " ");
                ebnfDefinition += (operator2 + " ");
                ebnfDefinition += (element3 + " ");
                ebnfDefinition += (terminator);
                if (element1.equals("P") || element1.equals("-P")) {
                  ebnfDefinition += ("P = \"p\" ;");
                }
                if (element2.equals("Q") || element2.equals("-Q")) {
                  ebnfDefinition += ("Q = \"q\" ;");
                }
                if (element3.equals("R") || element3.equals("-R")) {
                  ebnfDefinition += ("R = \"r\" ;");
                }
                combinations.add(new String[]{input, ebnfDefinition});
              }
            }
          }
        }
      }
    }
  return combinations;
  }

  public static String createSingleElementInputStrings(String element){
    return getInputStringForElement(element);
  }

  public static String createSingleEmbracedElementInputStrings(String embracedElement, String braceType){
    String output="";
    String element = getInputStringForElement(embracedElement);
    switch (braceType){
      case "(" :
      case "[" : output += element; break;
      case "{" : output += element + element + element; break;
    }
    return output;
  }

  public static String createTwoCombinedElementsInputStrings(String elem1, String elem2, String operator){
    String output="";
    if (operator.equals(",")) {
      output = getInputStringForElement(elem1) + getInputStringForElement(elem2);
    }
    if (operator.equals("|")) {
      output = getInputStringForElement(elem2);
    }
    return output;
  }

  public static String createTwoCombinedElementsInputStrings(String braceType, String elem1, String elem2, String operator){
    String output="";
    if (braceType.equals("{")) {
      if (operator.equals(",")) {
        output += (getInputStringForElement(elem1) + getInputStringForElement(elem2));
        output += (getInputStringForElement(elem1) + getInputStringForElement(elem2));
        output += (getInputStringForElement(elem1) + getInputStringForElement(elem2));
      }
      if (operator.equals("|")) {
        output += (getInputStringForElement(elem2));
        output += (getInputStringForElement(elem2));
        output += (getInputStringForElement(elem2));
      }
    } else {
      if (operator.equals(",")) {
        output += (getInputStringForElement(elem1) + getInputStringForElement(elem2));
      }
      if (operator.equals("|")) {
        output += (getInputStringForElement(elem2));
      }
    }
    return output;
  }

  public static String createOneElemPrecedingBracedTwoElementsInputString(String braceType, String elem1, String elem2, String elem3, String operator1, String operator2){
    String output="";
    if (operator1.equals(",")){
      output += (getInputStringForElement(elem1));
    }
    if (braceType.equals("{")){
      if (operator2.equals(",")){
        output += (getInputStringForElement(elem2));
        output += (getInputStringForElement(elem3));
        output += (getInputStringForElement(elem2));
        output += (getInputStringForElement(elem3));
        output += (getInputStringForElement(elem2));
        output += (getInputStringForElement(elem3));
      } else {
        output += (getInputStringForElement(elem3));
        output += (getInputStringForElement(elem3));
        output += (getInputStringForElement(elem3));
      }
    } else {
      if (operator2.equals(",")){
        output += (getInputStringForElement(elem2));
        output += (getInputStringForElement(elem3));
      } else {
        output += (getInputStringForElement(elem3));
      }
    }
    return output;
  }

  public static String createOneElementAfterTwoBracedElementsInputString(String braceType, String elem1, String elem2, String elem3, String operator1, String operator2){
    String output = "";
    if (braceType.equals("{")){
      if (operator1.equals(",")){
        output += (getInputStringForElement(elem1));
        output += (getInputStringForElement(elem2));
        output += (getInputStringForElement(elem1));
        output += (getInputStringForElement(elem2));
        output += (getInputStringForElement(elem1));
        output += (getInputStringForElement(elem2));
      } else {
        output += (getInputStringForElement(elem2));
        output += (getInputStringForElement(elem2));
        output += (getInputStringForElement(elem2));
      }
    } else {
      if (operator1.equals(",")){
        output += (getInputStringForElement(elem1));
        output += (getInputStringForElement(elem2));
      } else {
        output += (getInputStringForElement(elem2));
      }
    }
    if (operator2.equals(",")){
      output += (getInputStringForElement(elem3));
    }
    return output;
  }

  public static String getInputStringForElement(String element) {
    switch (element) {
      case "\"a\"": return  "a";
      case "\"b\"": return  "b";
      case "\"c\"": return  "c";
      case "J" : return "jkl";
      case "J - \"k\"" : return "l";
      case "J - K" : return "j";
      case "(-\"k\")" : return "n";
      case "(-K)" : return "m";
      case "P": return "p";
      case "-P": return "s";
      case "Q": return "q";
      case "-Q": return "t";
      case "R": return "r";
      case "-R": return "v";
      case "?CYRILLIC?": return "л";
      case "-?CYRILLIC?": return  "l";
      case "?BASIC_LATIN?": return "z";
      case "-?BASIC_LATIN?": return  "ψ";
      case "?EMOTICONS?": return "\uD83D\uDE00";
      case "-?EMOTICONS?": return "z";
      default: return "";
    }
  }
  public static List<String[]> getAllCombos(){
    List<String[]> combos = new ArrayList<>();
    combos.addAll(createTwoEmbracedElementsCombinations());
    combos.addAll(createOneElementBeforeTwoEmbracedElementsCombinations());
    combos.addAll(createEmbracedSingleElementCombinations());
    combos.addAll(createTwoElementsCombinations());
    combos.addAll(createOneElementAfterTwoEmbracedElementsCombinations());
    combos.addAll(createSingleExceptionElementCombinations());
    return combos;
  }
  public static void main(String[] args) {
    createSingleElementCombinations();
    createSingleExceptionElementCombinations();
    createEmbracedSingleElementCombinations();
    createTwoElementsCombinations();
    createTwoEmbracedElementsCombinations();
    createOneElementBeforeTwoEmbracedElementsCombinations();
    createOneElementAfterTwoEmbracedElementsCombinations();
  }


}
