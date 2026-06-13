# ebnf4j-parser-generator

**ebnf4j** ist ein flexibler Parser-Generator für Java, der EBNF-Grammatiken einliest und daraus zur Laufzeit einen vollständigen Parser samt Parsebaum-API erzeugt.

📜 Schreibe deine Grammatik in EBNF.  
⚙️ Generiere einen Parser mit nur einer Methode.  
🌳 Erhalte ein vollständiges Parse-Tree-Objekt inkl. Events, Visualisierung (SVG) und AST-Vergleich.

---

## ✨ Features

- 🔧 EBNF-Parser zur Laufzeit (kein Code-Gen, keine ANTLR-Tools nötig)
- 📄 Unterstützt rekursive Regeln, Wiederholungen, Gruppen, Alternativen
- 🌳 ParseTree-Objekte mit benannten Knoten und strukturierter Navigation
- 🔍 Event-API für Tokens und Knoten
- 🧪 Vollständig testgetrieben (TDD), inkl. SVG-Visualisierung
- 💡 Programmgesteuertes Erzeugen von Grammatiken möglich

---

## Trivia-Direktive

Mit `@trivia(NAME);` wird eine Regel festgelegt, die zwischen den Elementen aller
nachfolgenden EBNF-Regeln automatisch übersprungen und als Trivia im ResultTree
erfasst wird:

```ebnf
@trivia(WS);

A = 'one' | 'two';
B = 'A', 'B';

@trivia(none);

C = 'strict';
WS = { ?WHITESPACE? };
```

Die Direktive gilt bis zur nächsten `@trivia`-Direktive. `@trivia(none);` und
`@trivia();` schalten sie aus. Payload wird immer vor Trivia geprüft.

Collector-Sequenzen behalten ihren eigenen Bouncer. Am Eintritt in deren Payload
ist die Regel daher exakt; innerhalb einer erfolgreich begonnenen Payload gelten
wieder die Trivia-Einstellungen ihrer Definition.
