## GitHub-Release erstellen

Der Workflow `.github/workflows/maven-publish.yml` startet ausschließlich beim
Push eines neuen Tags, dessen Name mit `v` beginnt.

Nach dem Merge aller Änderungen:

```bash
git switch main
git pull --ff-only origin main
mvn clean test

VERSION=0.3.1
git tag -a "v${VERSION}" -m "Release v${VERSION}"
git push origin "v${VERSION}"
```

Danach den Lauf unter
[GitHub Actions](https://github.com/sivale/ebnf4j-parser-generator/actions)
kontrollieren. Der Workflow setzt die Maven-Version aus dem Tag, veröffentlicht
das Artefakt in GitHub Packages und erstellt ein GitHub Release mit den JARs.

Vor dem Taggen mit `git status` prüfen, dass alle benötigten Dateien committed
und gepusht wurden. Nicht `git push --tags` verwenden, sondern immer nur den
neuen Tag explizit pushen.