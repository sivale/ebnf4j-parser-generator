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

## 🚀 Beispiel

```java
EbnfParserGenerator generator = new EbnfParserGenerator("src/test/resources/simple.ebnf");
Parser parser = generator.generate();

ParseNode result = parser.parse("1 + 2 * 3");
System.out.println(result.toStringTree());
