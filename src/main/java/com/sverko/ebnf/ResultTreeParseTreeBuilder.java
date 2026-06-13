package com.sverko.ebnf;

import com.sverko.ebnf.result.ResultNode;
import com.sverko.ebnf.result.ResultNodeType;
import com.sverko.ebnf.result.ResultTree;
import com.sverko.ebnf.tools.StringUtils;
import com.sverko.ebnf.tools.TerminalNodeFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ResultTreeParseTreeBuilder {

  private static final Set<String> BOUNDARY_ENTRY_NAMES =
      Set.of("meta identifier", "terminal string", "special sequence");
  private static final Set<String> TRIMMED_SPAN_NODES =
      Set.of("meta identifier", "terminal string", "special sequence", "integer",
          "trivia selector");

  private final Parser schemaParser;
  private final Map<String, ParseNode> definedNtnNodes = new HashMap<>();
  private final Set<String> terminalStrings = new HashSet<>();

  private ResultTree resultTree;
  private TokenQueue tokenQueue;
  private NonTerminalNode firstNonTerminalNode;

  public ResultTreeParseTreeBuilder(Parser schemaParser) {
    this.schemaParser = Objects.requireNonNull(schemaParser, "schemaParser must not be null");
    resetState();
  }

  public ParseNode build(ResultTree resultTree) {
    this.resultTree = Objects.requireNonNull(resultTree, "resultTree must not be null");
    this.tokenQueue = Objects.requireNonNull(resultTree.getTokenQueue(),
        "resultTree token queue must not be null");

    resetState();

    List<RuleSlice> ruleSlices = collectRuleSlices(resultTree);
    predeclareRules(ruleSlices);
    buildRuleBodies(ruleSlices);
    resolveReferences();
    return firstNonTerminalNode;
  }

  public ParseNode getStartNode() {
    return firstNonTerminalNode;
  }

  public Map<String, ParseNode> getNamedNodes() {
    return definedNtnNodes;
  }

  public Set<String> getLexerTokens() {
    return terminalStrings;
  }

  private void resetState() {
    definedNtnNodes.clear();
    terminalStrings.clear();
    firstNonTerminalNode = null;

    definedNtnNodes.put("\\n",
        new NonTerminalNode("line feed")
            .setDownNode(new PositionNode().setDownNode(TerminalNodeFactory.cstn("\n"))));
    definedNtnNodes.put("\\t",
        new NonTerminalNode("tab")
            .setDownNode(new PositionNode().setDownNode(TerminalNodeFactory.cstn("\u0009"))));
    definedNtnNodes.put("\\s",
        new NonTerminalNode("space")
            .setDownNode(new PositionNode().setDownNode(TerminalNodeFactory.cstn("\u0020"))));
  }

  private List<RuleSlice> collectRuleSlices(ResultTree tree) {
    ResultNode root = tree.getRoot();
    if (root == null) {
      return List.of();
    }

    List<RuleSlice> slices = new ArrayList<>();
    boolean triviaConfigured = false;
    String triviaSelector = null;
    if ("syntax".equals(root.getName())) {
      for (ResultNode child : directChildren(root)) {
        if ("trivia directive".equals(child.getName())) {
          triviaConfigured = true;
          ResultNode selectorNode = firstDirectChild(child, "trivia selector");
          String selector = selectorNode == null ? "" : extractText(selectorNode);
          triviaSelector =
              selector.isEmpty() || "none".equalsIgnoreCase(selector) ? null : selector;
        } else if ("syntax rule".equals(child.getName())) {
          addRuleSlices(child, triviaConfigured, triviaSelector, slices);
        }
      }
    } else if ("syntax rule".equals(root.getName())) {
      addRuleSlices(root, false, null, slices);
    }
    return slices;
  }

  private void addRuleSlices(ResultNode syntaxRule, boolean triviaConfigured,
      String triviaSelector, List<RuleSlice> slices) {
    List<ResultNode> currentRule = new ArrayList<>();
    for (ResultNode child : directChildren(syntaxRule)) {
      currentRule.add(child);
      if ("terminator symbol".equals(child.getName())) {
        RuleSlice slice = toRuleSlice(currentRule, triviaConfigured, triviaSelector);
        if (slice != null) {
          slices.add(slice);
        }
        currentRule = new ArrayList<>();
      }
    }
  }

  private RuleSlice toRuleSlice(List<ResultNode> directChildren, boolean triviaConfigured,
      String triviaSelector) {
    ResultNode nameNode = firstNamed(directChildren, "meta identifier");
    ResultNode definitionsListNode = firstNamed(directChildren, "definitions list");
    if (nameNode == null || definitionsListNode == null) {
      return null;
    }
    return new RuleSlice(
        extractText(nameNode), definitionsListNode, triviaConfigured, triviaSelector);
  }

  private void predeclareRules(List<RuleSlice> ruleSlices) {
    for (RuleSlice slice : ruleSlices) {
      NonTerminalNode definitionNode = new NonTerminalNode(slice.name);
      slice.definitionNode = definitionNode;
      if (firstNonTerminalNode == null) {
        firstNonTerminalNode = definitionNode;
      }
      definedNtnNodes.put(slice.name, definitionNode);
    }
  }

  private void buildRuleBodies(List<RuleSlice> ruleSlices) {
    for (RuleSlice slice : ruleSlices) {
      ParseNode body = buildDefinitionsList(slice.definitionsListNode);
      if (body != null) {
        if (slice.triviaConfigured && !(body instanceof PositionNode)) {
          body = new PositionNode().setDownNode(body);
        }
        applyTriviaPolicy(body, slice.triviaConfigured, slice.triviaSelector);
        slice.definitionNode.setDownNode(body);
      }
    }
  }

  private void applyTriviaPolicy(ParseNode body, boolean triviaConfigured,
      String triviaSelector) {
    if (!triviaConfigured) {
      return;
    }
    applyTriviaPolicy(
        body, triviaSelector, Collections.newSetFromMap(new IdentityHashMap<>()));
  }

  private void applyTriviaPolicy(ParseNode node, String triviaSelector,
      Set<ParseNode> visited) {
    if (node == null || !visited.add(node) || node instanceof NonTerminalNode) {
      return;
    }
    if (node instanceof PositionNode positionNode) {
      if (positionNode.parent instanceof OrNode || triviaSelector == null) {
        positionNode.disableTrivia();
      } else {
        positionNode.setTriviaSelector(triviaSelector);
      }
    }
    if (node instanceof LoopNode loopNode && !loopNode.isCollectorScanner()) {
      if (triviaSelector == null) {
        loopNode.disableTrivia();
      } else {
        loopNode.setTriviaSelector(triviaSelector);
      }
    }
    applyTriviaPolicy(node.getDownNode(), triviaSelector, visited);
    applyTriviaPolicy(node.getRightNode(), triviaSelector, visited);
  }

  private ParseNode buildDefinitionsList(ResultNode definitionsListNode) {
    List<ParseNode> alternatives = new ArrayList<>();
    for (ResultNode child : directChildren(definitionsListNode)) {
      if ("single definition".equals(child.getName())) {
        alternatives.add(buildSingleDefinition(child));
      }
    }
    return chainAlternatives(alternatives);
  }

  private ParseNode buildSingleDefinition(ResultNode singleDefinitionNode) {
    List<ParseNode> terms = new ArrayList<>();
    for (ResultNode child : directChildren(singleDefinitionNode)) {
      if ("syntactic term".equals(child.getName())) {
        terms.add(buildSyntacticTerm(child));
      }
    }
    return chainSequence(terms);
  }

  private ParseNode buildSyntacticTerm(ResultNode syntacticTermNode) {
    ResultNode factorNode = firstDirectChild(syntacticTermNode, "syntactic factor");
    if (factorNode == null) {
      return null;
    }

    ParseNode factor = buildSyntacticFactor(factorNode);
    ResultNode exceptionNode = firstDirectChild(syntacticTermNode, "syntactic exception");
    if (exceptionNode == null) {
      return factor;
    }

    AntiNode antiNode = new AntiNode();
    antiNode.setDownNode(buildSyntacticException(exceptionNode));
    antiNode.setRightNode(asPositionCarrier(factor));
    return antiNode;
  }

  private ParseNode buildSyntacticFactor(ResultNode syntacticFactorNode) {
    ResultNode primaryNode = firstDirectChild(syntacticFactorNode, "syntactic primary");
    if (primaryNode == null) {
      return null;
    }

    ParseNode primary = buildSyntacticPrimary(primaryNode);
    ResultNode integerNode = firstDirectChild(syntacticFactorNode, "integer");
    if (integerNode == null) {
      return primary;
    }

    int repetitions = Integer.parseInt(extractText(integerNode));
    return new LoopNode(repetitions, repetitions).setDownNode(primary);
  }

  private ParseNode buildSyntacticPrimary(ResultNode syntacticPrimaryNode) {
    ResultNode payload = firstChild(directChildren(syntacticPrimaryNode));
    if (payload == null) {
      return null;
    }

    return switch (payload.getName()) {
      case "meta identifier" -> buildMetaIdentifierReference(payload);
      case "terminal string" -> buildTerminalString(payload);
      case "special sequence" -> buildSpecialSequence(payload);
      case "optional sequence" -> buildOptionalSequence(payload);
      case "repeated sequence" -> buildRepeatedSequence(payload);
      case "collector sequence", "bounded repeated sequence" ->
          buildBoundedRepeatedSequence(payload);
      case "grouped sequence" -> buildGroupedSequence(payload);
      case "empty sequence" -> buildEmptySequence();
      default -> throw new IllegalArgumentException(
          "Unsupported syntactic primary payload: " + payload.getName());
    };
  }

  private ParseNode buildSyntacticException(ResultNode syntacticExceptionNode) {
    ResultNode primaryNode = firstDirectChild(syntacticExceptionNode, "syntactic primary");
    if (primaryNode == null) {
      return null;
    }

    ResultNode payload = firstChild(directChildren(primaryNode));
    if (payload == null) {
      return null;
    }

    ParseNode builtPrimary = buildSyntacticPrimary(primaryNode);
    if ("terminal string".equals(payload.getName())) {
      return unwrapTerminalCarrier(builtPrimary);
    }
    return builtPrimary;
  }

  private ParseNode buildMetaIdentifierReference(ResultNode metaIdentifierNode) {
    return new PendingReferenceNode(extractText(metaIdentifierNode));
  }

  private ParseNode buildTerminalString(ResultNode terminalStringNode) {
    String allowed = performWsMapping(StringUtils.stripQuotes(extractText(terminalStringNode)));
    terminalStrings.add(allowed);
    return new PositionNode().setDownNode(TerminalNodeFactory.cstn(allowed));
  }

  private ParseNode buildSpecialSequence(ResultNode specialSequenceNode) {
    String specialSequence = extractText(specialSequenceNode);
    return new PositionNode().setDownNode(
        TerminalNodeFactory.createCharacterRangeBasedTerminalNode(
            schemaParser.getSpecialSequence(specialSequence), specialSequence));
  }

  private ParseNode buildOptionalSequence(ResultNode optionalSequenceNode) {
    ResultNode definitionsListNode = firstDirectChild(optionalSequenceNode, "definitions list");
    ParseNode body = definitionsListNode == null ? null : buildDefinitionsList(definitionsListNode);
    return new LoopNode(1).setDownNode(body);
  }

  private ParseNode buildRepeatedSequence(ResultNode repeatedSequenceNode) {
    ResultNode definitionsListNode = firstDirectChild(repeatedSequenceNode, "definitions list");
    ParseNode body = definitionsListNode == null ? null : buildDefinitionsList(definitionsListNode);
    return new LoopNode().setDownNode(body);
  }

  private ParseNode buildBoundedRepeatedSequence(ResultNode boundedRepeatedSequenceNode) {
    LoopNode collector = new LoopNode()
        .setCollectorScanner(true)
        .setCollectEdgeTrivia(true);

    ResultNode definitionsListNode = firstDirectChild(boundedRepeatedSequenceNode, "definitions list");
    if (definitionsListNode != null) {
      collector.setDownNode(buildDefinitionsList(definitionsListNode));
    }

    ResultNode bouncerNode = firstDirectChild(boundedRepeatedSequenceNode, "bouncer");
    if (bouncerNode != null) {
      ParseNode bouncer = buildBoundaryFromBouncer(bouncerNode);
      if (bouncer != null) {
        collector.setBouncerParseNode(bouncer);
      }
    }

    ResultNode kickoutNode = firstDirectChild(boundedRepeatedSequenceNode, "kickout");
    if (kickoutNode != null) {
      ParseNode kickout = buildBoundaryFromKickout(kickoutNode);
      if (kickout != null) {
        collector.setKickoutParseNode(kickout);
      }
    }

    return collector;
  }

  private ParseNode buildGroupedSequence(ResultNode groupedSequenceNode) {
    ResultNode definitionsListNode = firstDirectChild(groupedSequenceNode, "definitions list");
    return definitionsListNode == null ? null : buildDefinitionsList(definitionsListNode);
  }

  private ParseNode buildEmptySequence() {
    return new PositionNode().setDownNode(TerminalNodeFactory.cstn(""));
  }

  private ParseNode buildBoundaryFromKickout(ResultNode kickoutNode) {
    ResultNode nestedBouncer = firstDirectChild(kickoutNode, "bouncer");
    return nestedBouncer == null ? null : buildBoundaryFromBouncer(nestedBouncer);
  }

  private ParseNode buildBoundaryFromBouncer(ResultNode bouncerNode) {
    for (ResultNode child : directChildren(bouncerNode)) {
      if (BOUNDARY_ENTRY_NAMES.contains(child.getName())) {
        return asPositionCarrier(buildBoundaryEntry(child));
      }
    }
    return null;
  }

  private ParseNode buildBoundaryEntry(ResultNode boundaryEntryNode) {
    return switch (boundaryEntryNode.getName()) {
      case "meta identifier" -> buildMetaIdentifierReference(boundaryEntryNode);
      case "terminal string" -> buildTerminalString(boundaryEntryNode);
      case "special sequence" -> buildSpecialSequence(boundaryEntryNode);
      default -> throw new IllegalArgumentException(
          "Unsupported boundary entry: " + boundaryEntryNode.getName());
    };
  }

  private ParseNode chainAlternatives(List<ParseNode> alternatives) {
    if (alternatives.isEmpty()) {
      return null;
    }
    if (alternatives.size() == 1) {
      return alternatives.get(0);
    }

    OrNode head = new OrNode();
    OrNode current = head;
    for (int i = 0; i < alternatives.size(); i++) {
      current.setDownNode(asPositionCarrier(alternatives.get(i)));
      if (i + 1 < alternatives.size()) {
        current = (OrNode) current.returnRightNode(new OrNode());
      }
    }
    return head;
  }

  private ParseNode chainSequence(List<ParseNode> sequence) {
    if (sequence.isEmpty()) {
      return null;
    }
    if (sequence.size() == 1) {
      return sequence.get(0);
    }

    PositionNode head = null;
    PositionNode tail = null;
    for (ParseNode term : sequence) {
      PositionNode termHead = toSequencePositions(term);
      if (head == null) {
        head = termHead;
      } else {
        tail.returnRightNode(termHead);
      }
      tail = lastPosition(termHead);
    }
    return head;
  }

  private ParseNode asPositionCarrier(ParseNode node) {
    if (node instanceof PositionNode) {
      return node;
    }
    return new PositionNode().setDownNode(node);
  }

  private ParseNode unwrapTerminalCarrier(ParseNode node) {
    if (node instanceof PositionNode positionNode
        && positionNode.downNode instanceof TerminalNode terminalNode
        && !positionNode.hasRightNode()) {
      return terminalNode;
    }
    return node;
  }

  private PositionNode toSequencePositions(ParseNode node) {
    if (node instanceof PositionNode positionNode) {
      return positionNode;
    }
    return (PositionNode) new PositionNode().setDownNode(node);
  }

  private PositionNode lastPosition(PositionNode node) {
    PositionNode current = node;
    while (current.rightNode instanceof PositionNode next) {
      current = next;
    }
    return current;
  }

  private void resolveReferences() {
    Set<ParseNode> visited = Collections.newSetFromMap(new IdentityHashMap<>());
    resolveSubtree(firstNonTerminalNode, visited);
  }

  private void resolveSubtree(ParseNode node, Set<ParseNode> visited) {
    if (node == null || !visited.add(node)) {
      return;
    }

    if (node.hasDownNode()) {
      ParseNode resolvedDown = resolveReference(node.getDownNode());
      if (resolvedDown != node.getDownNode()) {
        attachChild(node, resolvedDown, true);
      }
      resolveSubtree(node.getDownNode(), visited);
    }

    if (node.hasRightNode()) {
      ParseNode resolvedRight = resolveReference(node.getRightNode());
      if (resolvedRight != node.getRightNode()) {
        attachChild(node, resolvedRight, false);
      }
      resolveSubtree(node.getRightNode(), visited);
    }

    if (node instanceof LoopNode loopNode) {
      ParseNode resolvedBouncer = resolveReference(loopNode.getBouncerParseNode());
      if (resolvedBouncer != loopNode.getBouncerParseNode()) {
        loopNode.setBouncerParseNode(resolvedBouncer);
      }
      resolveSubtree(loopNode.getBouncerParseNode(), visited);

      ParseNode resolvedKickout = resolveReference(loopNode.getKickoutParseNode());
      if (resolvedKickout != loopNode.getKickoutParseNode()) {
        loopNode.setKickoutParseNode(resolvedKickout);
      }
      resolveSubtree(loopNode.getKickoutParseNode(), visited);
    }
  }

  private ParseNode resolveReference(ParseNode node) {
    if (!(node instanceof PendingReferenceNode pendingReferenceNode)) {
      return node;
    }

    ParseNode target = definedNtnNodes.get(pendingReferenceNode.getReferenceName());
    return target != null ? target : new NonTerminalNode(pendingReferenceNode.getReferenceName());
  }

  private void attachChild(ParseNode parent, ParseNode child, boolean down) {
    if (shouldLink(child)) {
      if (down) {
        parent.linkDownNode(child);
      } else {
        parent.linkRightNode(child);
      }
      return;
    }

    if (down) {
      parent.setDownNode(child);
    } else {
      parent.setRightNode(child);
    }
  }

  private boolean shouldLink(ParseNode target) {
    return target == firstNonTerminalNode || target.hasParent();
  }

  private List<ResultNode> directChildren(ResultNode node) {
    List<ResultNode> children = new ArrayList<>();
    ResultNode child = node.getDownNode();
    while (child != null) {
      if (child.getType() != ResultNodeType.TRIVIA) {
        children.add(child);
      }
      child = child.getRightNode();
    }
    return children;
  }

  private ResultNode firstDirectChild(ResultNode node, String name) {
    for (ResultNode child : directChildren(node)) {
      if (name.equals(child.getName())) {
        return child;
      }
    }
    return null;
  }

  private ResultNode firstNamed(List<ResultNode> nodes, String name) {
    for (ResultNode node : nodes) {
      if (name.equals(node.getName())) {
        return node;
      }
    }
    return null;
  }

  private ResultNode firstChild(List<ResultNode> nodes) {
    return nodes.isEmpty() ? null : nodes.get(0);
  }

  private String extractText(ResultNode node) {
    int from = node.getFromToken();
    int to = node.getToToken();
    if (TRIMMED_SPAN_NODES.contains(node.getName())) {
      int[] childSpan = childSpan(node);
      if (childSpan != null) {
        from = childSpan[0];
        to = childSpan[1];
      }
    }
    if (from < 0 || to < from || to > tokenQueue.rawSize()) {
      return "";
    }
    String text = tokenQueue.getSubstring(from, to);
    return TRIMMED_SPAN_NODES.contains(node.getName()) ? text.strip() : text;
  }

  private int[] childSpan(ResultNode node) {
    List<ResultNode> children = directChildren(node);
    if (children.isEmpty()) {
      return null;
    }

    ResultNode first = children.get(0);
    ResultNode last = children.get(children.size() - 1);
    return new int[]{first.getFromToken(), last.getToToken()};
  }

  private String performWsMapping(String allowed) {
    if (allowed == null || allowed.isEmpty()) {
      return allowed;
    }

    StringBuilder out = new StringBuilder(allowed.length());
    for (int i = 0; i < allowed.length(); i++) {
      char ch = allowed.charAt(i);
      if (ch == '\\' && i + 1 < allowed.length()) {
        char next = allowed.charAt(i + 1);
        switch (next) {
          case 'n':
            out.append('\n');
            i++;
            continue;
          case 't':
            out.append('\t');
            i++;
            continue;
          case 's':
            out.append(' ');
            i++;
            continue;
          default:
            out.append('\\');
            continue;
        }
      }

      out.append(ch);
    }
    return out.toString();
  }

  private static final class RuleSlice {
    final String name;
    final ResultNode definitionsListNode;
    final boolean triviaConfigured;
    final String triviaSelector;
    NonTerminalNode definitionNode;

    private RuleSlice(String name, ResultNode definitionsListNode,
        boolean triviaConfigured, String triviaSelector) {
      this.name = name;
      this.definitionsListNode = definitionsListNode;
      this.triviaConfigured = triviaConfigured;
      this.triviaSelector = triviaSelector;
    }
  }

  private static final class PendingReferenceNode extends NonTerminalNode {
    private final String referenceName;

    private PendingReferenceNode(String referenceName) {
      super(referenceName);
      this.referenceName = referenceName;
    }

    private String getReferenceName() {
      return referenceName;
    }
  }
}
