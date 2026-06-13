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

public class ResultTreeSvgPrinter {

  private static final double NODE_WIDTH = 220.0;
  private static final double NODE_HEIGHT = 86.0;
  private static final double HORIZONTAL_GAP = 40.0;
  private static final double VERTICAL_GAP = 70.0;
  private static final double MARGIN = 30.0;

  private static final int MAX_NAME_LENGTH = 32;
  private static final int MAX_TEXT_LENGTH = 42;

  private final ResultTree resultTree;

  public ResultTreeSvgPrinter(ResultTree resultTree) {
    this.resultTree = Objects.requireNonNull(resultTree, "resultTree must not be null");
  }

  public String toSvg() {
    ResultNode root = resultTree.getRoot();
    if (root == null) {
      return emptyResultSvg();
    }

    LayoutNode layoutRoot = buildLayoutTree(root, new IdentityHashMap<>());
    LayoutState layoutState = new LayoutState();
    position(layoutRoot, 0, layoutState);

    double width = Math.max(
        (layoutState.leafCount * (NODE_WIDTH + HORIZONTAL_GAP)) - HORIZONTAL_GAP
            + (2 * MARGIN),
        NODE_WIDTH + (2 * MARGIN));
    double height = ((layoutState.maxDepth + 1) * NODE_HEIGHT)
        + (layoutState.maxDepth * VERTICAL_GAP)
        + (2 * MARGIN);

    StringBuilder svg = new StringBuilder();
    appendHeader(svg, width, height);
    appendStyles(svg);
    appendEdges(svg, layoutRoot);
    appendNodes(svg, layoutRoot);
    svg.append("</svg>\n");
    return svg.toString();
  }

  public void printResultTreeToFile(String path) throws IOException {
    Objects.requireNonNull(path, "path must not be null");
    printResultTreeToFile(Path.of(path));
  }

  public void printResultTreeToFile(Path path) throws IOException {
    Objects.requireNonNull(path, "path must not be null");
    Files.writeString(path, toSvg(), StandardCharsets.UTF_8);
  }

  private LayoutNode buildLayoutTree(ResultNode node, Map<ResultNode, Boolean> visited) {
    if (visited.put(node, Boolean.TRUE) != null) {
      throw new IllegalArgumentException("ResultTree contains a cycle or a shared ResultNode");
    }

    LayoutNode layoutNode = new LayoutNode(node);
    ResultNode child = node.getDownNode();
    while (child != null) {
      layoutNode.children.add(buildLayoutTree(child, visited));
      child = child.getRightNode();
    }
    return layoutNode;
  }

  private void position(LayoutNode node, int depth, LayoutState state) {
    node.y = MARGIN + (depth * (NODE_HEIGHT + VERTICAL_GAP));
    state.maxDepth = Math.max(state.maxDepth, depth);

    if (node.children.isEmpty()) {
      node.x = MARGIN + (state.leafCount * (NODE_WIDTH + HORIZONTAL_GAP));
      state.leafCount++;
      return;
    }

    for (LayoutNode child : node.children) {
      position(child, depth + 1, state);
    }

    LayoutNode firstChild = node.children.get(0);
    LayoutNode lastChild = node.children.get(node.children.size() - 1);
    node.x = ((firstChild.x + (NODE_WIDTH / 2.0))
        + (lastChild.x + (NODE_WIDTH / 2.0))) / 2.0
        - (NODE_WIDTH / 2.0);
  }

  private void appendHeader(StringBuilder svg, double width, double height) {
    svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        .append("<svg xmlns=\"http://www.w3.org/2000/svg\"")
        .append(" width=\"").append(format(width)).append("\"")
        .append(" height=\"").append(format(height)).append("\"")
        .append(" viewBox=\"0 0 ").append(format(width)).append(' ')
        .append(format(height)).append("\">\n");
  }

  private void appendStyles(StringBuilder svg) {
    svg.append("""
        <style>
          .edge { fill: none; stroke: #64748b; stroke-width: 1.5; }
          .node { stroke-width: 1.5; }
          .branch { fill: #eff6ff; stroke: #2563eb; }
          .leaf { fill: #f0fdf4; stroke: #16a34a; }
          .trivia { fill: #fffbeb; stroke: #d97706; stroke-dasharray: 5 3; }
          .name { fill: #0f172a; font: 600 14px sans-serif; }
          .value { fill: #334155; font: 12px monospace; }
          .span { fill: #64748b; font: 11px sans-serif; }
        </style>
        """);
  }

  private void appendEdges(StringBuilder svg, LayoutNode node) {
    double parentX = node.x + (NODE_WIDTH / 2.0);
    double parentY = node.y + NODE_HEIGHT;
    for (LayoutNode child : node.children) {
      double childX = child.x + (NODE_WIDTH / 2.0);
      double childY = child.y;
      double middleY = parentY + (VERTICAL_GAP / 2.0);
      svg.append("  <path class=\"edge\" d=\"M ")
          .append(format(parentX)).append(' ').append(format(parentY))
          .append(" V ").append(format(middleY))
          .append(" H ").append(format(childX))
          .append(" V ").append(format(childY))
          .append("\"/>\n");
      appendEdges(svg, child);
    }
  }

  private void appendNodes(StringBuilder svg, LayoutNode node) {
    ResultNode resultNode = node.resultNode;
    ResultNodeType type = resultNode.getType();
    boolean terminal = type == ResultNodeType.TERMINAL;
    double centerX = node.x + (NODE_WIDTH / 2.0);

    svg.append("  <g class=\"result-node\">\n")
        .append("    <rect class=\"node ").append(svgClass(type))
        .append("\" x=\"").append(format(node.x))
        .append("\" y=\"").append(format(node.y))
        .append("\" width=\"").append(format(NODE_WIDTH))
        .append("\" height=\"").append(format(NODE_HEIGHT))
        .append("\" rx=\"8\"/>\n");

    appendText(svg, "name", centerX, node.y + 24,
        abbreviate(visibleText(displayName(resultNode)), MAX_NAME_LENGTH));

    String matchedText = extractText(resultNode);
    String value = matchedText.equals(resultNode.getName())
        ? (terminal ? "terminal" : "")
        : abbreviate(visibleText(matchedText), MAX_TEXT_LENGTH);
    if (!value.isEmpty()) {
      appendText(svg, "value", centerX, node.y + 48, value);
    }

    appendText(svg, "span", centerX, node.y + 70, formatSpan(resultNode));
    svg.append("  </g>\n");

    for (LayoutNode child : node.children) {
      appendNodes(svg, child);
    }
  }

  private String svgClass(ResultNodeType type) {
    return switch (type) {
      case NON_TERMINAL -> "branch";
      case TERMINAL -> "leaf";
      case TRIVIA -> "trivia";
    };
  }

  private String displayName(ResultNode node) {
    if (node instanceof TriviaResultNode trivia) {
      return "trivia (" + trivia.getCategory() + ")";
    }
    return node.getName();
  }

  private void appendText(StringBuilder svg, String cssClass, double x, double y, String text) {
    svg.append("    <text class=\"").append(cssClass)
        .append("\" x=\"").append(format(x))
        .append("\" y=\"").append(format(y))
        .append("\" text-anchor=\"middle\">")
        .append(escapeXml(text))
        .append("</text>\n");
  }

  private String extractText(ResultNode node) {
    TokenQueue tokenQueue = resultTree.getTokenQueue();
    if (tokenQueue != null && hasValidSpan(node, tokenQueue)) {
      return tokenQueue.getSubstring(node.getFromToken(), node.getToToken());
    }
    return "";
  }

  private boolean hasValidSpan(ResultNode node, TokenQueue tokenQueue) {
    return node.getFromToken() >= 0
        && node.getToToken() >= node.getFromToken()
        && node.getToToken() <= tokenQueue.rawSize();
  }

  private String formatSpan(ResultNode node) {
    TokenQueue tokenQueue = resultTree.getTokenQueue();
    if (tokenQueue != null && hasValidSpan(node, tokenQueue)) {
      return "tokens [" + node.getFromToken() + ", " + node.getToToken() + ")";
    }
    return "token range unavailable";
  }

  private String emptyResultSvg() {
    double width = 360.0;
    double height = 120.0;
    StringBuilder svg = new StringBuilder();
    appendHeader(svg, width, height);
    svg.append("  <rect x=\"1\" y=\"1\" width=\"358\" height=\"118\"")
        .append(" fill=\"#f8fafc\" stroke=\"#94a3b8\"/>\n")
        .append("  <text x=\"180\" y=\"64\" text-anchor=\"middle\"")
        .append(" fill=\"#475569\" font-family=\"sans-serif\" font-size=\"14\">")
        .append("No parse result</text>\n")
        .append("</svg>\n");
    return svg.toString();
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

  private String escapeXml(String value) {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;");
  }

  private String format(double value) {
    if (value == Math.rint(value)) {
      return Long.toString(Math.round(value));
    }
    return Double.toString(value);
  }

  private static final class LayoutNode {

    private final ResultNode resultNode;
    private final List<LayoutNode> children = new ArrayList<>();
    private double x;
    private double y;

    private LayoutNode(ResultNode resultNode) {
      this.resultNode = resultNode;
    }
  }

  private static final class LayoutState {

    private int leafCount;
    private int maxDepth;
  }
}
