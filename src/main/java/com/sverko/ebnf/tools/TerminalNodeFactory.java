package com.sverko.ebnf.tools;

import com.sverko.ebnf.TerminalNode;
import java.util.function.Predicate;

public class TerminalNodeFactory {
  public static TerminalNode createSimpleTerminalNode(String allowed){
    return new TerminalNode(allowed, (s) -> s.equals(allowed));
  }

  public static TerminalNode cstn(String allowed){
    return createSimpleTerminalNode(allowed);
  }


  public static TerminalNode createArrayBasedTerminalNode(String[] allowed){
    return new TerminalNode((s) -> {
      for(String allowedToken:allowed){
        if (s != null && s.length() == 1) {
          if (s.equals(allowedToken)) return true;
        }
      }
      return false;
    });
  }

  public static TerminalNode createCharacterRangeBasedTerminalNode(Predicate<Integer> allowed){
    return new TerminalNode((s) -> allowed.test(s.codePointAt(0)));
  }
}
