package com.sverko.ebnf.result;

import com.sverko.ebnf.TokenQueue;
import java.util.ArrayList;
import java.util.List;

public class ResultTree {

  private ResultNode root;
  private TokenQueue tokenQueue;

  public ResultTree() {
  }

  public ResultTree(ResultNode root) {
    this.root = root;
  }

  public ResultNode getRoot() {
    return root;
  }

  public void setRoot(ResultNode root) {
    this.root = root;
  }

  public TokenQueue getTokenQueue() {
    return tokenQueue;
  }

  public void setTokenQueue(TokenQueue tokenQueue) {
    this.tokenQueue = tokenQueue;
  }

  public List<ResultNode> readSequentially() {
    List<ResultNode> nodes = new ArrayList<>();
    collectSequentially(root, nodes);
    return nodes;
  }

  public List<String> readSequentiallyAsAssignments() {
    List<String> entries = new ArrayList<>();
    collectSequentialAssignments(root, entries);
    return entries;
  }

  public List<String> readSequentiallyAsCondensedAssignments() {
    List<String> entries = new ArrayList<>();
    collectCondensedAssignments(root, entries);
    return entries;
  }

  private void collectSequentially(ResultNode node, List<ResultNode> nodes) {
    if (node == null) {
      return;
    }

    nodes.add(node);
    collectSequentially(node.getDownNode(), nodes);
    collectSequentially(node.getRightNode(), nodes);
  }

  private void collectSequentialAssignments(ResultNode node, List<String> entries) {
    if (node == null) {
      return;
    }

    entries.add(asAssignment(node));
    collectSequentialAssignments(node.getDownNode(), entries);
    collectSequentialAssignments(node.getRightNode(), entries);
  }

  private void collectCondensedAssignments(ResultNode node, List<String> entries) {
    if (node == null) {
      return;
    }

    if (shouldIncludeInCondensedView(node)) {
      entries.add(asAssignment(node));
    }
    collectCondensedAssignments(node.getDownNode(), entries);
    collectCondensedAssignments(node.getRightNode(), entries);
  }

  private boolean shouldIncludeInCondensedView(ResultNode node) {
    return node.getType() == ResultNodeType.NON_TERMINAL;
  }

  private String asAssignment(ResultNode node) {
    return node.getName() + "=" + extractText(node);
  }

  private String extractText(ResultNode node) {
    if (tokenQueue == null) {
      return "";
    }
    int from = node.getFromToken();
    int to = node.getToToken();
    if (from < 0 || to < from || to > tokenQueue.rawSize()) {
      return "";
    }
    return tokenQueue.getSubstring(from, to);
  }

}
