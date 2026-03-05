package com.sverko.ebnf;

public class LoopNode extends ParseNode {
  public enum LoopType { COLLECTOR, STRUCTURAL }
  LoopType loopType = LoopType.STRUCTURAL;
  int min = 0, max = 0;

  public LoopNode(int min, int max) {
    this.min = min;
    this.max = max;
    loopType = LoopType.COLLECTOR;
  }

  public LoopNode(int max) {
    this.min = 0;
    this.max = max;
    loopType = LoopType.COLLECTOR;
  }

  public LoopNode() { }


// DESIGN NOTE (Suffix-UW handling) UW=Unhandled Whitespace
// -------------------------------
// Problem: PositionNode may auto-skip ("pump") unhandled whitespace (UW) on NOT_FOUND,
// which can make collector-style loops (e.g. {?LETTER?}) accidentally span across
// token boundaries ("first second" gets merged).
//
// At the same time, structural loops (e.g. {"foo","bar","baz"}) must be able to
// continue across UW/newlines so that multiple iterations can be parsed from
// multi-line inputs.
//
// Additionally, UW is not always a separator: it can be DATA inside an atomic
// terminal (e.g. "United Kingdom") or inside char-classes like ?BMP?.
//
// Solution:
// 1) If we stand on suffix-UW after having matched payload, we first run a PROBE
//    starting exactly on this UW token. During the probe, we mark the token with
//    isLoopProbe so PositionNode will NOT pump it away (and node-events can be
//    suppressed if needed).
//    - Probe SUCCESS => UW is DATA in this context => treat probe result as a real match.
//    - Probe NOT_FOUND => UW is a separator candidate.
// 2) Only if probe fails, we apply the heuristic:
//    lastTokensFound == 1 => collector-like => stop on suffix-UW (leave UW for upper nodes)
//    lastTokensFound  > 1 => structural-like => skip UW and continue with next iteration.
//
// This keeps collectors from merging tokens, allows structural repeats across
// whitespace/newlines, and preserves terminals/char-classes that include whitespace.

  @Override
  public int callReceived(int token) {
    if (token == END_OF_QUEUE) return token;

    final boolean hasMin = min != 0;

    if (!tokens.checkIndex(token)) {
      return hasMin ? END_OF_QUEUE : token;
    }
    int sent = token;

    // Track: did we already match real payload (non-UW start token)?
    boolean hadNonWhitespaceMatch = false;

    // Last successful end-index
    int furthestMatch = token;

    // For min>0: how many iterations have matched
    int matchedIterations = 0;

    while (true) {
      if (!tokens.checkIndex(sent)) {
        // If we're out of input:
        if (hasMin && matchedIterations < min) return NOT_FOUND;
        return Math.max(furthestMatch, token);
      }

      // ===== Suffix-UW handling (the core) =====
      if (hadNonWhitespaceMatch && tokens.isUnhandledWhitespace(sent)) {

        // 1) PROBE: can the body consume/match starting ON this whitespace token?
        //    (PN must not pump it away, and events should be suppressed during probe)
        boolean oldProbe = tokens.isLoopProbe(sent);
        tokens.setLoopProbe(sent, true);
        int probeResult;
        try {
          probeResult = callDown(sent);
        } finally {
          tokens.setLoopProbe(sent, oldProbe);
        }

        if (probeResult > sent) {
          // Whitespace is DATA in this context (e.g. "United Kingdom", ?BMP?, etc.)
          int curResult = probeResult;

          // progress
          furthestMatch = curResult;

          // Count iteration if needed (min>0 case)
          matchedIterations++;

          // Respect max (if max==0 -> treat as unbounded)
          if (max != 0 && matchedIterations >= max) return curResult;

          // Advance to next token after the last consumed token
          sent = tokens.getNextToken(curResult - 1);
          if (sent < 0) {
            if (hasMin && matchedIterations < min) return NOT_FOUND;
            return Math.max(furthestMatch, token);
          }

          // Note: hadNonWhitespaceMatch stays true (we already had payload)
          // lastTokensFound is not updated from probe; we keep the heuristic based on real iterations
          continue;
        }

        if (loopType == LoopType.COLLECTOR) {
          if (hasMin && matchedIterations < min) return NOT_FOUND;
          return furthestMatch;
        }

        // structural: skip one or more UW tokens so next iteration can start
        int s = sent;
        do {
          s = tokens.getNextToken(s);
          if (s < 0 || !tokens.checkIndex(s)) {
            if (hasMin && matchedIterations < min) return NOT_FOUND;
            return Math.max(furthestMatch, token);
          }
        } while (tokens.isUnhandledWhitespace(s));

        sent = s;
        continue;
      }

      // ===== Normal iteration (not standing on suffix-UW) =====
      int curResult = callDown(sent);

      // same-position => done (no progress)
      if (curResult == sent) {
        // If we haven't met min yet, this is a failure
        if (hasMin && matchedIterations < min) return NOT_FOUND;
        return curResult;
      }

      // not found / error
      if (curResult < 0) {
        if (hasMin && matchedIterations < min) return NOT_FOUND;
        return Math.max(furthestMatch, token);
      }

      matchedIterations++;

      // mark that we matched real payload (using your original “start token isn’t UW” heuristic)
      if (!tokens.isUnhandledWhitespace(sent)) {
        hadNonWhitespaceMatch = true;
      }

      // reached max iterations (if max==0 -> unbounded)
      if (max != 0 && matchedIterations >= max) {
        return curResult;
      }

      furthestMatch = curResult;

      // advance to the next token after the last consumed token
      sent = tokens.getNextToken(curResult - 1);
      if (sent < 0) {
        if (hasMin && matchedIterations < min) return NOT_FOUND;
        return Math.max(furthestMatch, token);
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof LoopNode)) return false;
    LoopNode other = (LoopNode) o;
    return this.max == other.max
        && this.min == other.min
        && this.loopType == other.loopType;
  }
}
