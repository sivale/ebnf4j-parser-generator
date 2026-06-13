package com.sverko.ebnf;

import com.sverko.ebnf.result.ResultNode;
import com.sverko.ebnf.result.ResultNodeType;
import com.sverko.ebnf.result.ResultTree;
import com.sverko.ebnf.result.TriviaResultNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ResultTreeHtmlPrinter {

  private static final int INITIAL_OPEN_DEPTH = 2;
  private static final int REPEAT_GROUP_THRESHOLD = 3;
  private static final int MAX_PREVIEW_LENGTH = 80;

  private final ResultTree resultTree;
  private int nextNodeId;

  public ResultTreeHtmlPrinter(ResultTree resultTree) {
    this.resultTree = Objects.requireNonNull(resultTree, "resultTree must not be null");
  }

  public String toHtml() {
    nextNodeId = 0;
    ResultNode root = resultTree.getRoot();
    int nodeCount = root == null ? 0 : resultTree.readSequentially().size();

    StringBuilder html = new StringBuilder();
    appendDocumentStart(html, nodeCount);
    if (root == null) {
      html.append("""
              <main class="empty-state">
                <h2>No parse result</h2>
                <p>The ResultTree does not contain a root node.</p>
              </main>
          """);
    } else {
      Map<ResultNode, Boolean> visited = new IdentityHashMap<>();
      html.append("""
              <main class="workspace">
                <section class="tree-panel" aria-label="Parse result tree">
                  <ul class="tree root-tree">
          """);
      appendNode(html, root, 0, false, visited);
      html.append("""
                  </ul>
                </section>
                <aside class="inspector" aria-live="polite">
                  <h2>Node details</h2>
                  <dl>
                    <dt>Name</dt><dd id="detail-name">Select a node</dd>
                    <dt>Type</dt><dd id="detail-type">-</dd>
                    <dt>Matched text</dt><dd><pre id="detail-value">-</pre></dd>
                    <dt>Token range</dt><dd id="detail-range">-</dd>
                    <dt>Children</dt><dd id="detail-children">-</dd>
                  </dl>
                </aside>
              </main>
          """);
    }
    appendScript(html);
    html.append("</body>\n</html>\n");
    return html.toString();
  }

  public void printResultTreeToFile(String path) throws IOException {
    Objects.requireNonNull(path, "path must not be null");
    printResultTreeToFile(Path.of(path));
  }

  public void printResultTreeToFile(Path path) throws IOException {
    Objects.requireNonNull(path, "path must not be null");
    Files.writeString(path, toHtml(), StandardCharsets.UTF_8);
  }

  private void appendDocumentStart(StringBuilder html, int nodeCount) {
    html.append("""
        <!doctype html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <title>ResultTree Inspector</title>
          <style>
            :root {
              color-scheme: light dark;
              --background: #f8fafc;
              --surface: #ffffff;
              --surface-muted: #f1f5f9;
              --border: #cbd5e1;
              --line: #94a3b8;
              --text: #0f172a;
              --muted: #64748b;
              --branch: #1d4ed8;
              --terminal: #15803d;
              --trivia: #b45309;
              --selection: #dbeafe;
              --match: #fef3c7;
            }

            * { box-sizing: border-box; }

            body {
              margin: 0;
              min-height: 100vh;
              background: var(--background);
              color: var(--text);
              font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont,
                  "Segoe UI", sans-serif;
            }

            .toolbar {
              position: sticky;
              z-index: 10;
              top: 0;
              display: flex;
              flex-wrap: wrap;
              align-items: center;
              gap: 0.55rem;
              min-height: 3.5rem;
              padding: 0.65rem 1rem;
              background: color-mix(in srgb, var(--surface) 94%, transparent);
              border-bottom: 1px solid var(--border);
              backdrop-filter: blur(8px);
            }

            .toolbar h1 {
              margin: 0 0.75rem 0 0;
              font-size: 1rem;
            }

            .node-total {
              margin-right: auto;
              color: var(--muted);
              font-size: 0.82rem;
            }

            button,
            input {
              border: 1px solid var(--border);
              border-radius: 0.4rem;
              background: var(--surface);
              color: var(--text);
              font: inherit;
            }

            button {
              padding: 0.38rem 0.65rem;
              cursor: pointer;
            }

            button:hover { background: var(--surface-muted); }
            button[aria-pressed="true"] {
              border-color: var(--branch);
              background: var(--selection);
            }

            .search {
              width: min(20rem, 42vw);
              padding: 0.4rem 0.65rem;
            }

            .workspace {
              display: grid;
              grid-template-columns: minmax(0, 1fr) minmax(16rem, 24rem);
              gap: 1rem;
              align-items: start;
              padding: 1rem;
            }

            .tree-panel,
            .inspector,
            .empty-state {
              border: 1px solid var(--border);
              border-radius: 0.65rem;
              background: var(--surface);
              box-shadow: 0 1px 2px rgb(15 23 42 / 0.06);
            }

            .tree-panel {
              min-width: 0;
              overflow: auto;
              padding: 0.75rem 1rem 1.5rem;
            }

            .inspector {
              position: sticky;
              top: 4.5rem;
              padding: 1rem;
            }

            .inspector h2 {
              margin: 0 0 1rem;
              font-size: 1rem;
            }

            .inspector dl {
              display: grid;
              grid-template-columns: max-content minmax(0, 1fr);
              gap: 0.55rem 0.8rem;
              margin: 0;
              font-size: 0.86rem;
            }

            .inspector dt { color: var(--muted); }
            .inspector dd {
              min-width: 0;
              margin: 0;
              overflow-wrap: anywhere;
            }

            .inspector pre {
              max-height: 18rem;
              margin: 0;
              overflow: auto;
              white-space: pre-wrap;
              font: 0.78rem/1.45 ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
            }

            .tree,
            .tree ul {
              margin: 0;
              padding: 0;
              list-style: none;
            }

            .tree ul {
              margin-left: 0.72rem;
              padding-left: 1.18rem;
              border-left: 1px solid var(--line);
            }

            .tree-item {
              position: relative;
              min-width: max-content;
              padding: 0.08rem 0;
            }

            .tree ul > .tree-item::before {
              position: absolute;
              top: 1.02rem;
              left: -1.18rem;
              width: 0.95rem;
              border-top: 1px solid var(--line);
              content: "";
            }

            details > summary {
              display: flex;
              align-items: center;
              list-style: none;
              cursor: pointer;
            }

            details > summary::-webkit-details-marker { display: none; }
            details > summary::before {
              width: 1rem;
              flex: 0 0 1rem;
              color: var(--muted);
              content: "\\25B8";
            }

            details[open] > summary::before { content: "\\25BE"; }

            .leaf-row {
              margin-left: 1rem;
            }

            .node-row {
              display: inline-flex;
              align-items: baseline;
              gap: 0.55rem;
              min-height: 1.9rem;
              max-width: min(76rem, calc(100vw - 8rem));
              padding: 0.3rem 0.5rem;
              border: 1px solid transparent;
              border-radius: 0.35rem;
              cursor: default;
            }

            .node-row:hover { background: var(--surface-muted); }
            .node-row.selected {
              border-color: #93c5fd;
              background: var(--selection);
            }

            .node-row.match { background: var(--match); }

            .node-name {
              color: var(--branch);
              font-weight: 650;
            }

            .terminal .node-name { color: var(--terminal); }
            .trivia .node-name {
              color: var(--trivia);
              font-style: italic;
            }
            .aggregate .node-name { color: #7c3aed; }

            .node-preview {
              max-width: 48rem;
              overflow: hidden;
              color: var(--text);
              font: 0.8rem ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
              text-overflow: ellipsis;
              white-space: nowrap;
            }

            .node-meta {
              color: var(--muted);
              font-size: 0.76rem;
              white-space: nowrap;
            }

            body.compact-mode .full-only,
            body.full-mode .compact-only,
            .search-hidden {
              display: none;
            }

            .empty-state {
              max-width: 36rem;
              margin: 3rem auto;
              padding: 2rem;
              text-align: center;
            }

            .empty-state h2 { margin-top: 0; }
            .empty-state p { color: var(--muted); }

            @media (max-width: 900px) {
              .workspace { grid-template-columns: 1fr; }
              .inspector { position: static; }
              .search { width: 100%; }
              .node-row { max-width: calc(100vw - 5rem); }
            }

            @media (prefers-color-scheme: dark) {
              :root {
                --background: #0f172a;
                --surface: #111827;
                --surface-muted: #1e293b;
                --border: #334155;
                --line: #475569;
                --text: #e2e8f0;
                --muted: #94a3b8;
                --branch: #93c5fd;
                --terminal: #86efac;
                --trivia: #fbbf24;
                --selection: #1e3a5f;
                --match: #713f12;
              }
            }
          </style>
        </head>
        <body class="compact-mode">
          <header class="toolbar">
            <h1>ResultTree Inspector</h1>
            <span class="node-total">
        """);
    html.append(nodeCount).append(" nodes</span>\n");
    html.append("""
            <button id="compact-button" type="button" aria-pressed="true">Compact</button>
            <button id="full-button" type="button" aria-pressed="false">Full</button>
            <button id="expand-button" type="button">Expand all</button>
            <button id="collapse-button" type="button">Collapse all</button>
            <input id="search-input" class="search" type="search"
                placeholder="Search rule or matched text" aria-label="Search tree">
          </header>
        """);
  }

  private void appendNode(StringBuilder html, ResultNode node, int depth,
      boolean fullOnly, Map<ResultNode, Boolean> visited) {
    ensureNotVisited(node, visited);

    List<ResultNode> children = directChildren(node);
    ResultNodeType type = node.getType();
    boolean hasChildren = !children.isEmpty();
    String itemClasses = "tree-item"
        + " " + cssClass(type)
        + (fullOnly ? " full-only" : "");

    html.append("<li class=\"").append(itemClasses).append("\">\n");
    if (hasChildren) {
      html.append("<details");
      if (depth < INITIAL_OPEN_DEPTH) {
        html.append(" open");
      }
      html.append(">\n<summary>");
      appendNodeRow(html, displayName(node), extractText(node), formatRange(node),
          children.size(), typeName(type), false);
      html.append("</summary>\n<ul>\n");
      appendChildren(html, children, depth + 1, visited);
      html.append("</ul>\n</details>\n");
    } else {
      html.append("<div class=\"leaf-row\">");
      appendNodeRow(html, displayName(node), extractText(node), formatRange(node),
          0, typeName(type), false);
      html.append("</div>\n");
    }
    html.append("</li>\n");
  }

  private void appendChildren(StringBuilder html, List<ResultNode> children, int depth,
      Map<ResultNode, Boolean> visited) {
    int index = 0;
    while (index < children.size()) {
      int runEnd = repeatedRunEnd(children, index);
      int runLength = runEnd - index;

      if (runLength >= REPEAT_GROUP_THRESHOLD) {
        appendAggregateNode(html, children.subList(index, runEnd));
        for (int childIndex = index; childIndex < runEnd; childIndex++) {
          appendNode(html, children.get(childIndex), depth, true, visited);
        }
      } else {
        for (int childIndex = index; childIndex < runEnd; childIndex++) {
          ResultNode child = children.get(childIndex);
          appendNode(html, child, depth, child.getType() == ResultNodeType.TERMINAL, visited);
        }
      }
      index = runEnd;
    }
  }

  private int repeatedRunEnd(List<ResultNode> children, int start) {
    ResultNode first = children.get(start);
    int end = start + 1;
    while (end < children.size() && sameRepeatGroup(first, children.get(end))) {
      end++;
    }
    return end;
  }

  private boolean sameRepeatGroup(ResultNode first, ResultNode candidate) {
    if (first.getType() != candidate.getType()
        || !Objects.equals(first.getName(), candidate.getName())) {
      return false;
    }
    if (first instanceof TriviaResultNode firstTrivia
        && candidate instanceof TriviaResultNode candidateTrivia) {
      return firstTrivia.getCategory().equals(candidateTrivia.getCategory());
    }
    return true;
  }

  private void appendAggregateNode(StringBuilder html, List<ResultNode> nodes) {
    ResultNode first = nodes.get(0);
    ResultNode last = nodes.get(nodes.size() - 1);
    String name = first.getName() + " x " + nodes.size();
    String text = extractText(first.getFromToken(), last.getToToken());
    String range = formatRange(first.getFromToken(), last.getToToken());

    html.append("<li class=\"tree-item aggregate compact-only\">\n")
        .append("<div class=\"leaf-row\">");
    appendNodeRow(html, name, text, range, nodes.size(), "repetition group", true);
    html.append("</div>\n</li>\n");
  }

  private void appendNodeRow(StringBuilder html, String name, String matchedText,
      String range, int childCount, String type, boolean aggregate) {
    String visibleName = visibleText(name);
    String visibleValue = visibleText(matchedText);
    String preview = abbreviate(visibleValue, MAX_PREVIEW_LENGTH);
    int nodeId = ++nextNodeId;

    html.append("<span class=\"node-row")
        .append(aggregate ? " aggregate" : "")
        .append("\" tabindex=\"0\" role=\"button\"")
        .append(" data-node-id=\"").append(nodeId).append("\"")
        .append(" data-name=\"").append(escapeAttribute(visibleName)).append("\"")
        .append(" data-type=\"").append(escapeAttribute(type)).append("\"")
        .append(" data-value=\"").append(escapeAttribute(visibleValue)).append("\"")
        .append(" data-range=\"").append(escapeAttribute(range)).append("\"")
        .append(" data-children=\"").append(childCount).append("\"")
        .append(" data-search=\"")
        .append(escapeAttribute((visibleName + " " + visibleValue).toLowerCase()))
        .append("\">")
        .append("<span class=\"node-name\">").append(escapeHtml(visibleName)).append("</span>");

    if (!preview.isEmpty()) {
      html.append("<span class=\"node-preview\">")
          .append(escapeHtml(preview))
          .append("</span>");
    }

    html.append("<span class=\"node-meta\">")
        .append(escapeHtml(range))
        .append(" | ")
        .append(childCount)
        .append(childCount == 1 ? " child" : " children")
        .append("</span>")
        .append("</span>\n");
  }

  private void ensureNotVisited(ResultNode node, Map<ResultNode, Boolean> visited) {
    if (visited.put(node, Boolean.TRUE) != null) {
      throw new IllegalArgumentException("ResultTree contains a cycle or a shared ResultNode");
    }
  }

  private String cssClass(ResultNodeType type) {
    return switch (type) {
      case NON_TERMINAL -> "branch";
      case TERMINAL -> "terminal";
      case TRIVIA -> "trivia";
    };
  }

  private String typeName(ResultNodeType type) {
    return type.name().toLowerCase().replace('_', ' ');
  }

  private String displayName(ResultNode node) {
    if (node instanceof TriviaResultNode trivia) {
      return "trivia (" + trivia.getCategory() + ")";
    }
    return node.getName();
  }

  private List<ResultNode> directChildren(ResultNode node) {
    List<ResultNode> children = new ArrayList<>();
    ResultNode child = node.getDownNode();
    while (child != null) {
      children.add(child);
      child = child.getRightNode();
    }
    return children;
  }

  private String extractText(ResultNode node) {
    return extractText(node.getFromToken(), node.getToToken());
  }

  private String extractText(int fromToken, int toToken) {
    TokenQueue tokenQueue = resultTree.getTokenQueue();
    if (tokenQueue != null && hasValidSpan(fromToken, toToken, tokenQueue)) {
      return tokenQueue.getSubstring(fromToken, toToken);
    }
    return "";
  }

  private String formatRange(ResultNode node) {
    return formatRange(node.getFromToken(), node.getToToken());
  }

  private String formatRange(int fromToken, int toToken) {
    TokenQueue tokenQueue = resultTree.getTokenQueue();
    if (tokenQueue != null && hasValidSpan(fromToken, toToken, tokenQueue)) {
      return "tokens [" + fromToken + ", " + toToken + ")";
    }
    return "token range unavailable";
  }

  private boolean hasValidSpan(int fromToken, int toToken, TokenQueue tokenQueue) {
    return fromToken >= 0 && toToken >= fromToken && toToken <= tokenQueue.rawSize();
  }

  private String visibleText(String value) {
    if (value == null) {
      return "";
    }

    StringBuilder visible = new StringBuilder();
    value.codePoints().forEach(codePoint -> {
      switch (codePoint) {
        case '\n' -> visible.append("\\n");
        case '\r' -> visible.append("\\r");
        case '\t' -> visible.append("\\t");
        default -> {
          if (Character.isISOControl(codePoint)) {
            visible.append(String.format("\\u%04X", codePoint));
          } else {
            visible.appendCodePoint(codePoint);
          }
        }
      }
    });
    return visible.toString();
  }

  private String abbreviate(String value, int maxLength) {
    if (value.codePointCount(0, value.length()) <= maxLength) {
      return value;
    }
    int end = value.offsetByCodePoints(0, maxLength - 3);
    return value.substring(0, end) + "...";
  }

  private String escapeHtml(String value) {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }

  private String escapeAttribute(String value) {
    return escapeHtml(value)
        .replace("\n", "&#10;")
        .replace("\r", "&#13;");
  }

  private void appendScript(StringBuilder html) {
    html.append("""
        <script>
          const body = document.body;
          const compactButton = document.getElementById("compact-button");
          const fullButton = document.getElementById("full-button");
          const searchInput = document.getElementById("search-input");

          function setMode(mode) {
            const compact = mode === "compact";
            body.classList.toggle("compact-mode", compact);
            body.classList.toggle("full-mode", !compact);
            if (compactButton) compactButton.setAttribute("aria-pressed", String(compact));
            if (fullButton) fullButton.setAttribute("aria-pressed", String(!compact));
          }

          compactButton?.addEventListener("click", () => setMode("compact"));
          fullButton?.addEventListener("click", () => setMode("full"));

          document.getElementById("expand-button")?.addEventListener("click", () => {
            document.querySelectorAll(".tree details").forEach(details => details.open = true);
          });

          document.getElementById("collapse-button")?.addEventListener("click", () => {
            document.querySelectorAll(".tree details").forEach(details => details.open = false);
          });

          function selectNode(row) {
            document.querySelectorAll(".node-row.selected")
                .forEach(selected => selected.classList.remove("selected"));
            row.classList.add("selected");
            document.getElementById("detail-name").textContent = row.dataset.name || "";
            document.getElementById("detail-type").textContent = row.dataset.type || "";
            document.getElementById("detail-value").textContent = row.dataset.value || "";
            document.getElementById("detail-range").textContent = row.dataset.range || "";
            document.getElementById("detail-children").textContent = row.dataset.children || "0";
          }

          document.querySelectorAll(".node-row").forEach(row => {
            row.addEventListener("click", () => selectNode(row));
            row.addEventListener("keydown", event => {
              if (event.key === "Enter" || event.key === " ") {
                event.preventDefault();
                selectNode(row);
              }
            });
          });

          searchInput?.addEventListener("input", () => {
            const query = searchInput.value.trim().toLowerCase();
            const items = Array.from(document.querySelectorAll(".tree-item"));
            items.forEach(item => item.classList.toggle("search-hidden", query.length > 0));
            document.querySelectorAll(".node-row.match")
                .forEach(row => row.classList.remove("match"));

            if (!query) {
              items.forEach(item => item.classList.remove("search-hidden"));
              return;
            }

            document.querySelectorAll(".node-row").forEach(row => {
              if (!(row.dataset.search || "").includes(query)) return;

              row.classList.add("match");
              let item = row.closest(".tree-item");
              while (item) {
                item.classList.remove("search-hidden");
                const details = item.querySelector(":scope > details");
                if (details) details.open = true;
                item = item.parentElement?.closest(".tree-item");
              }
            });
          });
        </script>
        """);
  }
}
