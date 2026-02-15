package com.sverko.ebnf.tools;

import com.sverko.ebnf.TokenQueue;
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

  public static String trimUnhandledWhitespace(TokenQueue q) {
    int n = q.rawSize();
    if (n == 0) return "";

    // first index that is NOT unhandled whitespace
    int start = q.indexStream()
        .filter(i -> !q.isUnhandledWhitespace(i))
        .findFirst()
        .orElse(n);

    // if everything is whitespace, return an empty string
    if (start == n) return "";

    // last index that is NOT unhandled whitespace (per reduce to last occurrence)
    int last = q.indexStream()
        .filter(i -> !q.isUnhandledWhitespace(i))
        .reduce((a, b) -> b)
        .orElse(start);

    int endExclusive = last + 1;

    return q.getSubstring(start, endExclusive);
  }
}