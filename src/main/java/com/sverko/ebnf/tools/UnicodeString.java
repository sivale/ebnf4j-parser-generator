package com.sverko.ebnf.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

public class UnicodeString implements CharSequence {

  private final String internalString;

  public UnicodeString(String inputString) {
    internalString = inputString;
  }

  public int length() {
    return internalString.codePointCount(0, internalString.length());
  }

  @Override
  public char charAt(int index) {
    return Character.toChars(codePointAt(index))[0];
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return null;
  }

  public String getStringAt(int index) {
    final int length = internalString.length();
    int idx = 0;
    for (int offset = 0; offset < length; ) {
      final int codepoint = internalString.codePointAt(offset);
      if (idx == index) {
        return String.valueOf(Character.toChars(codepoint));
      }
      offset += Character.charCount(codepoint);
      idx++;
    }
    return "";
  }

  public static List<UnicodeString> listOf (String... list) {
    List<UnicodeString> unicodeStrings = new ArrayList<>();
    for (String element : list) {
      unicodeStrings.add(new UnicodeString(element));
    }
    return unicodeStrings;
  }

  public static Set<UnicodeString> setOf (String... list) {
    Set<UnicodeString> unicodeStrings = new HashSet<>();
    for (String element : list) {
      unicodeStrings.add(new UnicodeString(element));
    }
    return unicodeStrings;
  }

  public int codePointAt(int index){
    return internalString.codePointAt(index);
  }



  @Override
  public String toString() {
    return internalString;
  }
}