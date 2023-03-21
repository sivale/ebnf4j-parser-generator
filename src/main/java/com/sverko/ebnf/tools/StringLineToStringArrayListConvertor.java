package com.sverko.ebnf.tools;

import java.util.ArrayList;
import java.util.List;

public class StringLineToStringArrayListConvertor {
  public static List<String> convert (String s){
    List<String> stringList = new ArrayList<>();
    UnicodeString us = new UnicodeString(s);
    for (int i=0; i<us.length(); i++){
      stringList.add(us.getStringAt(i));
    }
    return stringList;
  }
}
