package com.sverko.ebnf.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    int startOffset = internalString.offsetByCodePoints(0, start);
    int endOffset = internalString.offsetByCodePoints(0, end);
    return internalString.substring(startOffset, endOffset);
  }

  public String getStringAt(int index) {
    final int length = internalString.length();
    int token = 0;
    for (int offset = 0; offset < length; ) {
      final int codepoint = internalString.codePointAt(offset);
      if (token == index) {
        return String.valueOf(Character.toChars(codepoint));
      }
      offset += Character.charCount(codepoint);
      token++;
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
    int offset = internalString.offsetByCodePoints(0, index);
    return internalString.codePointAt(offset);
  }

  @Override
  public String toString() {
    return internalString;
  }
}