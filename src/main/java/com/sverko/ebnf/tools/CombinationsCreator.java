package com.sverko.ebnf.tools;

import java.util.List;

public class CombinationsCreator {
  public static List<String> elements1 = List.of("\"a\"","P","?EMOTICONS?");
  public static List<String> elements2 = List.of("\"b\"","Q","?CYRILLIC?");
  public static List<String> elements3 = List.of("\"c\"","R","?BASIC_LATIN?");
  public static List<String> exceptionElements = List.of("J - k", "J - K", "(-k)", "(-K)");
  public static List<String> openingBraces = List.of("{","[","(");
  public static List<String> closingBraces = List.of("}","]",")");
  public static List<String> operators = List.of("|",",");
  public static String terminator = ";";
  public static int cnt=0;

  public static void createSingleElementCombinations (){
    for (String element : elements1){
      System.out.print(++cnt + " : ");
      createSingleElementInputStrings(element);
      System.out.print("A = ");
      System.out.print(element+" ");
      System.out.println(terminator);
      if (element.equals("P") || element.equals("-p")){
        System.out.println("P = \"p\" ;");
      }
      System.out.println();
    }
  }

  public static void createSingleExceptionElementCombinations (){
    for (String element : exceptionElements){
      System.out.print(++cnt + " : ");
      createSingleElementInputStrings(element);
      System.out.print("A = ");
      System.out.print(element+" ");
      System.out.println(terminator);
      if (element.contains("J")){
        System.out.println("J = \"j\", \"k\", \"l\", ;");
      }
      if (element.contains("K")){
        System.out.println("K = \"k\", \"l\" ;");
      }
      System.out.println();
    }
  }

  public static void createEmbracedSingleElementCombinations (){
    for(int i=0; i<openingBraces.size(); i++) {
      for (String element : elements1) {
        System.out.print(++cnt + " : ");
        createSingleEmbracedElementInputStrings(element, openingBraces.get(i));
        System.out.print("A = ");
        System.out.print(openingBraces.get(i)+" ");
        System.out.print(element + " ");
        System.out.print(closingBraces.get(i)+" ");
        System.out.println(terminator);
        if (element.equals("P") || element.equals("-P")){
          System.out.println("P = \"p\" ;");
        }
        System.out.println();
      }
    }
  }

  public static void createTwoElementsCombinations (){

      for (String element1 : elements1) {
        for (String element2 : elements2) {
          for (String operator : operators) {
              System.out.print(++cnt + " : ");
              createTwoCombinedElementsInputStrings(element1, element2, operator);
              System.out.print("A = ");
              System.out.print(element1 + " ");
              System.out.print(operator + " ");
              System.out.print(element2 + " ");
              System.out.println(terminator);
            if (element1.equals("P") || element1.equals("-P")) {
              System.out.println("P = \"p\" ;");
            }
            if (element2.equals("Q") || element2.equals("-Q")) {
              System.out.println("Q = \"q\" ;");
            }
              System.out.println();
          }
        }
      }
    }

  public static void createTwoEmbracedElementsCombinations (){
  for (int i = 0; i < openingBraces.size(); i++) {
    for (String element1 : elements1) {
      for (String element2 : elements2) {
        for (String operator : operators) {
          System.out.print(++cnt + " : ");
          createTwoCombinedElementsInputStrings(openingBraces.get(i), element1, element2, operator);
          System.out.print("A = ");
          System.out.print(openingBraces.get(i)+" ");
          System.out.print(element1 + " ");
          System.out.print(operator + " ");
          System.out.print(element2 + " ");
          System.out.print(closingBraces.get(i)+" ");
          System.out.println(terminator);
          if (element1.equals("P") || element1.equals("-P")) {
            System.out.println("P = \"p\" ;");
          }
          if (element2.equals("Q") || element2.equals("-Q")) {
            System.out.println("Q = \"q\" ;");
          }
          System.out.println();
        }
      }
    }
  }
}

  private static void createOneElementBeforeTwoEmbracedElementsCombinations() {
    for (int i = 0; i < openingBraces.size(); i++) {
      for (String element1 : elements1) {
        for (String element2 : elements2) {
          for (String element3 : elements3) {
            for (String operator1 : operators) {
              for (String operator2 : operators) {
                System.out.print(++cnt + " : ");
                createOneElemPrecedingBracedTwoElementsInputString(openingBraces.get(i),element1,element2,element3,operator1,operator2);
                System.out.print("A = " + element1 + " ");
                System.out.print(operator1 + " ");
                System.out.print(openingBraces.get(i) + " ");
                System.out.print(element2 + " ");
                System.out.print(operator2 + " ");
                System.out.print(element3 + " ");
                System.out.print(closingBraces.get(i) + " ");
                System.out.println(terminator);
                if (element1.equals("P") || element1.equals("-P")) {
                  System.out.println("P = \"p\" ;");
                }
                if (element2.equals("Q") || element2.equals("-Q")) {
                  System.out.println("Q = \"q\" ;");
                }
                if (element3.equals("R") || element3.equals("-R")) {
                  System.out.println("R = \"r\" ;");
                }
                System.out.println();
              }
            }
          }
        }
      }
    }
  }

  private static void createOneElementAfterTwoEmbracedElementsCombinations() {
    for (int i = 0; i < openingBraces.size(); i++) {
      for (String element1 : elements1) {
        for (String element2 : elements2) {
          for (String element3 : elements3) {
            for (String operator1 : operators) {
              for (String operator2 : operators) {
                System.out.print(++cnt + " : ");
                createOneElementAfterTwoBracedElementsInputString(openingBraces.get(i), element1, element2,element3,operator1,operator2);
                System.out.print("A = ");
                System.out.print(openingBraces.get(i) + " ");
                System.out.print(element1 + " ");
                System.out.print(operator1 + " ");
                System.out.print(element2 + " ");
                System.out.print(closingBraces.get(i) + " ");
                System.out.print(operator2 + " ");
                System.out.print(element3 + " ");
                System.out.println(terminator);
                if (element1.equals("P") || element1.equals("-P")) {
                  System.out.println("P = \"p\" ;");
                }
                if (element2.equals("Q") || element2.equals("-Q")) {
                  System.out.println("Q = \"q\" ;");
                }
                if (element3.equals("R") || element3.equals("-R")) {
                  System.out.println("R = \"r\" ;");
                }
                System.out.println();
              }
            }
          }
        }
      }
    }
  }

  public static void createSingleElementInputStrings(String element){
    System.out.println(getInputStringForElement(element));
  }

  public static void createSingleEmbracedElementInputStrings(String embracedElement, String braceType){
    String element = getInputStringForElement(embracedElement);
    switch (braceType){
      case "(" :
      case "[" : System.out.println(element); break;
      case "{" : System.out.println(element + element + element); break;
    }
  }

  public static void createTwoCombinedElementsInputStrings(String elem1, String elem2, String operator){
    if (operator.equals(",")) {
      System.out.println(getInputStringForElement(elem1) + getInputStringForElement(elem2));
    }
    if (operator.equals("|")) {
      System.out.println(getInputStringForElement(elem2));
    }
  }

  public static void createTwoCombinedElementsInputStrings(String braceType, String elem1, String elem2, String operator){
    if (braceType.equals("{")) {
      if (operator.equals(",")) {
        System.out.print(getInputStringForElement(elem1) + getInputStringForElement(elem2));
        System.out.print(getInputStringForElement(elem1) + getInputStringForElement(elem2));
        System.out.println(getInputStringForElement(elem1) + getInputStringForElement(elem2));
      }
      if (operator.equals("|")) {
        System.out.print(getInputStringForElement(elem2));
        System.out.print(getInputStringForElement(elem2));
        System.out.println(getInputStringForElement(elem2));
      }
    } else {
      if (operator.equals(",")) {
        System.out.println(getInputStringForElement(elem1) + getInputStringForElement(elem2));
      }
      if (operator.equals("|")) {
        System.out.println(getInputStringForElement(elem2));
      }
    }
  }

  public static void createOneElemPrecedingBracedTwoElementsInputString(String braceType, String elem1, String elem2, String elem3, String operator1, String operator2){
    if (operator1.equals(",")){
      System.out.print(getInputStringForElement(elem1));
    }
    if (braceType.equals("{")){
      if (operator2.equals(",")){
        System.out.print(getInputStringForElement(elem2));
        System.out.print(getInputStringForElement(elem3));
        System.out.print(getInputStringForElement(elem2));
        System.out.print(getInputStringForElement(elem3));
        System.out.print(getInputStringForElement(elem2));
        System.out.println(getInputStringForElement(elem3));
      } else {
        System.out.print(getInputStringForElement(elem3));
        System.out.print(getInputStringForElement(elem3));
        System.out.println(getInputStringForElement(elem3));
      }
    } else {
      if (operator2.equals(",")){
        System.out.print(getInputStringForElement(elem2));
        System.out.println(getInputStringForElement(elem3));
      } else {
        System.out.println(getInputStringForElement(elem3));
      }
    }
  }

  public static void createOneElementAfterTwoBracedElementsInputString(String braceType, String elem1, String elem2, String elem3, String operator1, String operator2){
    if (braceType.equals("{")){
      if (operator1.equals(",")){
        System.out.print(getInputStringForElement(elem1));
        System.out.print(getInputStringForElement(elem2));
        System.out.print(getInputStringForElement(elem1));
        System.out.print(getInputStringForElement(elem2));
        System.out.print(getInputStringForElement(elem1));
        System.out.print(getInputStringForElement(elem2));
      } else {
        System.out.print(getInputStringForElement(elem2));
        System.out.print(getInputStringForElement(elem2));
        System.out.print(getInputStringForElement(elem2));
      }
    } else {
      if (operator1.equals(",")){
        System.out.print(getInputStringForElement(elem1));
        System.out.print(getInputStringForElement(elem2));
      } else {
        System.out.print(getInputStringForElement(elem2));
      }
    }
    if (operator2.equals(",")){
      System.out.println(getInputStringForElement(elem3));
    } else {
      System.out.println();
    }
  }

  public static String getInputStringForElement(String element) {
    switch (element) {
      case "\"a\"": return  "a";
      case "\"b\"": return  "b";
      case "\"c\"": return  "c";
      case "J" : return "jkl";
      case "J - k" : return "l";
      case "J - K" : return "j";
      case "(-k)" : return "n";
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
