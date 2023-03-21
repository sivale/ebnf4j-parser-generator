package com.sverko.ebnf.tools;

import java.nio.charset.StandardCharsets;
import java.util.Stack;

public class StringUtils {
  public static String stripQuotes(String quotedString){
    UnicodeString us = new UnicodeString(quotedString);
    if(us.getStringAt(0).equals("\"") || us.getStringAt(0).equals("'")){
      quotedString = quotedString.substring(1);
    }
    if(us.getStringAt(us.length()-1).equals("\"") ||
        us.getStringAt(us.length()-1).equals("'")){
      quotedString = quotedString.substring(0,quotedString.length()-1);
    }
    return quotedString;
  }

  public static String[] convertStackToArray(Stack<String> stack){
    String[] strings = new String[stack.size()];
    for (int i=0; i<stack.size(); i++){
      strings[i] = stack.elementAt(i);
    }
    return strings;
  }
}
