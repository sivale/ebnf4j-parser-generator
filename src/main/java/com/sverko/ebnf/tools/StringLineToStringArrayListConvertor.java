package com.sverko.ebnf.tools;

import com.sverko.ebnf.Token;
import com.sverko.ebnf.TokenQueue;
import com.sverko.ebnf.TokenType;
import java.util.ArrayList;
import java.util.List;

public class StringLineToStringArrayListConvertor {
  public static TokenQueue convert (String s){
    List<Token> stringList = new ArrayList<>();
    UnicodeString us = new UnicodeString(s);
    for (int i=0; i<us.length(); i++){
      stringList.add(new Token(us.getStringAt(i), TokenType.UNKNOWN));
    }
    return new TokenQueue(stringList);
  }
}
