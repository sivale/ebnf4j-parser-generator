package com.sverko.ebnf.tools;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class UnicodeString {

  private final String internalString;

  public UnicodeString(String inputString) {
    internalString = inputString;
  }

  public int length() {
    return internalString.codePointCount(0, internalString.length());
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

  @Override
  public String toString() {
    return internalString;
  }
}


