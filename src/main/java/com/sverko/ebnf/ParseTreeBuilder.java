package com.sverko.ebnf;

import com.sverko.ebnf.tools.StringUtils;
import com.sverko.ebnf.tools.TerminalNodeFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ParseTreeBuilder implements ParseNodeEventListener {

  private ParseNode nonTerminalNode = null;
  private ParseNode firstNonTerminalNode = null;
  private ParseNode tail = null;
  private final Map<String, ParseNode> definedNtnNodes = new HashMap<>();
  public final Set<String> terminalStrings = new HashSet<>();
  private final Parser generator;

  public ParseTreeBuilder(Parser ebnfParserGenerator) {
    generator = ebnfParserGenerator;
    definedNtnNodes.put("LF", new NonTerminalNode("LF").setDownNode(new PositionNode().setDownNode(TerminalNodeFactory.cstn("\n"))));
    definedNtnNodes.put("HT", new NonTerminalNode("HT").setDownNode(new PositionNode().setDownNode(TerminalNodeFactory.cstn("\u0009"))));
  }

  /**
   * Node build conventions:
   * 1. each TerminalNode has a PositionNode vertical parent
   * 2. each OrNode has a PositionNode as its vertical child
   * 3. a NonTerminalNode may not have another NonTerminalNode
   * as its direct child
   */
  @Override
  public void parseNodeEventOccurred(ParseNodeEvent e) {
    switch (e.parseNode.name) {

      case "meta identifier":
        nonTerminalNode = new NonTerminalNode(e.resultString);
        if (firstNonTerminalNode == null) {
          firstNonTerminalNode = nonTerminalNode;
        }
        if (tail == null) {
          tail = nonTerminalNode;
        } else if (tail instanceof NonTerminalNode) {
          tail = tail.returnDownNode(new PositionNode()).returnDownNode(nonTerminalNode);
        } else if (tail instanceof PositionNode) {
          tail.setDownNode(nonTerminalNode);
        } else if (tail instanceof OrNode || tail instanceof LoopNode) {
          tail = tail.returnDownNode(nonTerminalNode);
        } else if (tail instanceof AntiNode) {
          tail.setDownNode(new PositionNode().setDownNode(nonTerminalNode));
          tail = tail.getRightNode();  // right node comes from 'case: "except symbol"'
        }
        break;

      case "defining symbol":
        definedNtnNodes.put(nonTerminalNode.name, nonTerminalNode);
        break;

      case "terminal string":
        String allowed = StringUtils.stripQuotes(e.resultString);
        terminalStrings.add(allowed);
        if (tail instanceof NonTerminalNode) {
          tail = tail.returnDownNode(new PositionNode().setDownNode(TerminalNodeFactory.cstn(allowed)));
        } else if (tail instanceof PositionNode) {
          tail.setDownNode(TerminalNodeFactory.cstn(allowed));
        } else if (tail instanceof OrNode || tail instanceof LoopNode) {
          tail = tail.returnDownNode(new PositionNode().setDownNode(TerminalNodeFactory.cstn(allowed)));
        } else if (tail instanceof AntiNode) {
          tail.setDownNode(TerminalNodeFactory.cstn(allowed));
          tail = tail.getRightNode(); // right node comes from 'case: "except symbol"'
        }
        break;

      case "concatenate symbol":
        if (tail instanceof NonTerminalNode) {
          if (tail.hasParent()) {
            if (tail.parent instanceof PositionNode) {
              tail = tail.parent.returnRightNode(new PositionNode());
            } else if (tail.parent instanceof LoopNode) {
              tail = tail.parent.returnDownNode(new PositionNode().setDownNode(tail))
                  .returnRightNode(new PositionNode());
            }
          }
        } else if (tail instanceof PositionNode) {
          tail = tail.returnRightNode(new PositionNode());
        } else if (tail instanceof OrNode) {
          // this happens when grouping braces are used
          if (getUpperLevelParentNode(tail) instanceof PositionNode) {
            tail = getUpperLevelParentNode(tail);
            tail = tail.returnRightNode(new PositionNode());
          } else if (getUpperLevelParentNode(tail) instanceof NonTerminalNode) {
            ParseNode head = getEldestSibling(tail);
            ParseNode levelParent = getUpperLevelParentNode(tail);
            tail = levelParent.returnDownNode(new PositionNode().setDownNode(head)).returnRightNode(new PositionNode());
          }
        } else if (tail instanceof LoopNode) {
          tail = tail.parent.returnDownNode(new PositionNode().setDownNode(tail)).returnRightNode(new PositionNode());
        } else if (tail instanceof TerminalNode) {
          if (tail.hasParent() && tail.parent instanceof NonTerminalNode) {
            tail = tail.parent.returnDownNode(new PositionNode().setDownNode(tail)).returnRightNode(new PositionNode());
          }
        }
        break;

      case "definition separator symbol":
        if (tail instanceof TerminalNode) {
          if (tail.parent instanceof NonTerminalNode) {
            tail = tail.parent.returnDownNode(new OrNode().setDownNode(new PositionNode().setDownNode(tail))).returnRightNode(new OrNode()).returnDownNode(new PositionNode());
          } else {
            tail = tail.parent.returnDownNode(new OrNode().setDownNode(new PositionNode().setDownNode(tail))).returnRightNode(new OrNode());
          }
        } else if (tail instanceof NonTerminalNode) {
          if (tail.hasParent() && tail.parent instanceof PositionNode) {
            tail = tail.parent.parent.returnDownNode(new OrNode().setDownNode(tail)).returnRightNode(new OrNode());
          } else {
            tail = tail.parent.returnDownNode(new OrNode().setDownNode(tail)).returnRightNode(new OrNode());
          }
        } else if (tail instanceof PositionNode) {
          ParseNode levelParent = getUpperLevelParentNode(tail);
          if (levelParent instanceof OrNode) {
            tail = levelParent.returnRightNode(new OrNode()).returnDownNode(new PositionNode());
          } else if (levelParent instanceof NonTerminalNode) {
            ParseNode head = getEldestSibling(tail);
            tail = levelParent.returnDownNode(new OrNode().setDownNode(head)).returnRightNode(new OrNode()).returnDownNode(new PositionNode());
          } else {
            ParseNode head = getEldestSibling(tail);
            tail = head.parent.returnDownNode(new OrNode().setDownNode(head)).returnRightNode(new OrNode()).returnDownNode(new PositionNode());
          }
        } else if (tail instanceof OrNode) {
          tail = tail.returnRightNode(new OrNode());
        } else if (tail instanceof LoopNode) {
          tail = tail.parent.returnDownNode(new OrNode().setDownNode(tail)).returnRightNode(new OrNode());
        }
        break;

      case "except symbol":
        if (tail instanceof NonTerminalNode) {
          if (tail.parent instanceof PositionNode) {
            ParseNode an = new AntiNode();
            tail = tail.parent.parent.returnDownNode(an.setRightNode(tail.parent));
          } else if (tail.parent instanceof LoopNode){
            tail = tail.parent.returnDownNode(new AntiNode().setRightNode(new PositionNode().setDownNode(tail)));
          }
        } else if (tail instanceof PositionNode) {
          tail = tail.parent.returnDownNode(
              new AntiNode().setRightNode(tail));
        }
        break;

      case "start group symbol":
          tail = tail.returnDownNode(new PositionNode());
        break;

      case "end group symbol":
        ParseNode levelParent = getUpperLevelParentNode(tail);
        ParseNode head = getEldestSibling(tail);
        if (levelParent instanceof PositionNode) {
          if (head instanceof PositionNode) {
            if (levelParent.parent.rightNode == levelParent) {
              levelParent.parent.setRightNode(head);
            } else if (levelParent.parent.downNode == levelParent) {
              levelParent.parent.setDownNode(head);
            }
          }
        } else if (levelParent instanceof OrNode) {
          tail = levelParent;
        }
        break;

      case "start option symbol":
        if (tail instanceof LoopNode) {
          // prevents two LoopNodes in 3*["a"]
          if (((LoopNode) tail).max > 0) {
            ((LoopNode) tail).min = 0;
            break;
          }
        }
        tail = tail.returnDownNode(new LoopNode(1));
        break;

      case "integer":
        tail = tail.returnDownNode(new LoopNode(Integer.parseInt(e.resultString), Integer.parseInt(e.resultString)));
        break;

      case "start repeat symbol":
        tail = tail.returnDownNode(new LoopNode());
        break;

      case "end option symbol":
      case "end repeat symbol":
        if (!(tail instanceof LoopNode)) {
          tail = getNextLoopNodeParent(tail);
        }
        break;

      case "special sequence":
        if (tail instanceof PositionNode) {
          tail.setDownNode(TerminalNodeFactory.createCharacterRangeBasedTerminalNode(
              generator.getSpecialSequence(e.resultString)));
        } else {
          tail = tail.returnDownNode(new PositionNode().setDownNode(TerminalNodeFactory.createCharacterRangeBasedTerminalNode(
                  generator.getSpecialSequence(e.resultString))));
        }
        break;

      case "terminator symbol":
        tail = null;
        break;

      case "end of parsing":
        stitchNonTerminalNodes(firstNonTerminalNode);
        break;
    }
  }

  private ParseNode getNextLoopNodeParent(ParseNode tail) {
    if (tail instanceof LoopNode) {
      return tail;
    } else {
      if (!tail.hasParent()) {
        return null;
      }
      return getNextLoopNodeParent(tail.parent);
    }
  }


  public ParseNode getStartNode() {
    return firstNonTerminalNode;
  }

  Map<String, ParseNode> getNamedNodes() { return definedNtnNodes; }
  Set<String> getLexerTokens() { return terminalStrings; }
  // a parent on the level above might not exist
  private ParseNode getUpperLevelParentNode(ParseNode curNode) {
    if (curNode.hasParent()) {
      if (curNode.parent.getDownNode() == curNode) {
        return curNode.parent;
      } else {
        return getUpperLevelParentNode(curNode.parent);
      }
    }
    return null;
  }

  // the node itself might be the eldest sibling
  private ParseNode getEldestSibling(ParseNode curNode) {
    if (curNode.hasParent() && curNode.parent.getRightNode() == curNode) {
      return getEldestSibling(curNode.parent);
    }
    return curNode;
  }

  // the node itself might be the top node
  private ParseNode getTopNode(ParseNode curNode) {
    if (curNode.hasParent()) {
      return getTopNode(curNode.parent);
    }
    return curNode;
  }

  private void stitchNonTerminalNodes(ParseNode curNode) {
    if (curNode instanceof NonTerminalNode && !curNode.hasDownNode()) {
      if (definedNtnNodes.containsKey(curNode.name)) {
        ParseNode nodeToInsert = definedNtnNodes.get(curNode.name);
        if (nodeToInsert.hasParent() || nodeToInsert == firstNonTerminalNode) {
          curNode.parent.linkDownNode(nodeToInsert);
        } else {
          curNode.parent.setDownNode(nodeToInsert);
        }
        curNode = curNode.parent.downNode;
      }
    }
    if (curNode.hasDownNode() && !curNode.isAncestor(curNode.downNode)) {
      stitchNonTerminalNodes(curNode.downNode);
    }
    if (curNode.hasRightNode() && !curNode.isAncestor(curNode.rightNode)) {
      stitchNonTerminalNodes(curNode.rightNode);
    }
  }
}
