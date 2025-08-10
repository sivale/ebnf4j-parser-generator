package com.sverko.ebnf.tools;

import com.sverko.ebnf.TerminalNode;
import java.util.Arrays;
import java.util.function.Predicate;

public class TerminalNodeFactory {
  public static TerminalNode createSimpleTerminalNode(String allowed){
    return new TerminalNode(allowed, (s) -> s.equals(allowed),
        "== " + quote(allowed));
  }

  public static TerminalNode cstn(String allowed){
    return createSimpleTerminalNode(allowed);
  }


  public static TerminalNode createArrayBasedTerminalNode(String[] allowed){
    String tag = "in " + prettySet(allowed);
    return new TerminalNode((s) -> {
      for(String allowedToken:allowed){
        if (s != null && s.length() == 1) {
          if (s.equals(allowedToken)) return true;
        }
      }
      return false;
    }, tag);
  }

  public static TerminalNode createCharacterRangeBasedTerminalNode(Predicate<Integer> allowed, String tag){
    return new TerminalNode((s) -> allowed.test(s.codePointAt(0)), tag);
  }

  private static String quote(String s){
    // einfache Escapes für unsichtbare Whitespace-Zeichen
    return "\"" + s
        .replace("\t","\\t")
        .replace("\n","\\n")
        .replace("\r","\\r")
        .replace("\f","\\f")
        .replace("\b","\\b") + "\"";
  }
  private static String prettySet(String[] items){
    // Nur 1-Zeichen-Strings werden sinnvoll gruppiert
    int[] cps = Arrays.stream(items)
        .filter(s -> s != null && s.length()==1)
        .mapToInt(s -> s.codePointAt(0))
        .sorted()
        .toArray();

    StringBuilder sb = new StringBuilder("{");
    int i = 0;
    boolean first = true;

    while (i < cps.length) {
      int start = cps[i];
      int end = start;
      while (i+1 < cps.length && cps[i+1] == end + 1) {
        end = cps[++i];
      }
      if (!first) sb.append(", ");
      first = false;

      if (start == end) {
        sb.append(sym(start));
      } else {
        sb.append(sym(start)).append("–").append(sym(end));
      }
      i++;
    }

    // verbleibende Einträge, die KEIN 1-Zeichen sind, anhängen
    String[] rest = Arrays.stream(items).filter(s -> s == null || s.length()!=1).toArray(String[]::new);
    for (String r : rest) {
      if (!first) sb.append(", ");
      first = false;
      sb.append(quote(r));
    }

    sb.append("}");
    return sb.toString();
  }

  private static String sym(int cp){
    switch (cp) {
      case 0x20: return "SP"; // Space kurz benennen
      case 0x09: return "\\t";
      case 0x0A: return "\\n";
      case 0x0D: return "\\r";
      default:
        String s = new String(Character.toChars(cp));
        // Sichtbare ASCII direkt zeigen, sonst als U+XXXX
        if (cp >= 0x21 && cp <= 0x7E) return s;
        return String.format("U+%04X", cp);
    }
  }
}
