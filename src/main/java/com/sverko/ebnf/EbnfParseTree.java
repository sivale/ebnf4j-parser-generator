package com.sverko.ebnf;


import com.sverko.ebnf.tools.TerminalNodeFactory;
import java.util.HashMap;
import java.util.Map;

public class EbnfParseTree {
  static Map<String, ParseNode> nodeMap = new HashMap<>();

  public static ParseNode getStartNode() {

    ParseNode startNode = new PositionNode();

    startNode.returnDownNode(createNonTerminalNode("syntax"))
        .returnDownNode(new LoopNode())
        .returnDownNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("syntax rule"))
        .returnDownNode(new LoopNode().setName("loopy"))
        .returnDownNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("meta identifier"))
        .returnDownNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("letter"))
        .returnDownNode(new PositionNode())
        .returnDownNode(TerminalNodeFactory.createArrayBasedTerminalNode(new String[]{
            " ", "_",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
            "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
        }));
        nodeMap.get("meta identifier").getDownNode()
        .returnRightNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("meta identifying character"))
        .returnDownNode(new LoopNode())
        .returnDownNode(new OrNode())
        .returnDownNode(new PositionNode())
        .linkDownNode(nodeMap.get("letter"));
        nodeMap.get("meta identifying character").getDownNode().getDownNode()
        .returnRightNode(new OrNode())
        .returnDownNode(createNonTerminalNode("decimal digit"))
        .returnDownNode(new PositionNode())
        .returnDownNode(TerminalNodeFactory.createArrayBasedTerminalNode(new String[]{
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
        }));
        nodeMap.get("meta identifier").parent
        .returnRightNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("defining symbol"))
        .returnDownNode(new PositionNode())
        .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode("="));
        nodeMap.get("defining symbol").parent
        .returnRightNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("definitions list"))
        .returnDownNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("single definition"))
        .returnDownNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("syntactic term"))
        .returnDownNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("syntactic factor"))
        .returnDownNode(new PositionNode())
        .returnDownNode(new LoopNode())
        .returnDownNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("integer"))
        .returnDownNode(new PositionNode())
        .linkDownNode(nodeMap.get("decimal digit"));
        nodeMap.get("integer").getDownNode()
        .returnRightNode(new PositionNode())
        .returnDownNode(new LoopNode())
        .linkDownNode(nodeMap.get("decimal digit"));
        nodeMap.get("integer").parent
        .returnRightNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("repetition symbol"))
        .returnDownNode(new PositionNode())
        .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode("*"));
        nodeMap.get("syntactic factor").downNode
        .returnRightNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("syntactic primary"))
        .returnDownNode(new OrNode().linkDownNode(nodeMap.get("meta identifier")))
        .returnRightNode(new OrNode())
        .returnDownNode(createNonTerminalNode("terminal string"))
        .returnDownNode(new OrNode())
        .returnDownNode(new PositionNode().setAcceptsWhitespace(true))
        .returnDownNode(createNonTerminalNode("first quote symbol"))
        .returnDownNode(new PositionNode())
        .returnDownNode(TerminalNodeFactory.cstn("\""));
        nodeMap.get("first quote symbol").parent
        .returnRightNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("first terminal character"))
        .returnDownNode(new AntiNode().linkDownNode(nodeMap.get("first quote symbol")))
        .returnRightNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("terminal character"))
        .returnDownNode(new PositionNode())
        .returnDownNode(
            TerminalNodeFactory.createCharacterRangeBasedTerminalNode(Character::isDefined,"?ANY?"));
        nodeMap.get("first terminal character").parent
        .returnRightNode(new PositionNode())
        .returnDownNode(new LoopNode())
        .linkDownNode(nodeMap.get("first terminal character"));
        nodeMap.get("first terminal character").parent.getRightNode()
        .returnRightNode(new PositionNode())
        .linkDownNode(nodeMap.get("first quote symbol"));
        nodeMap.get("terminal string").getDownNode()
        .returnRightNode((new OrNode()))
        .returnDownNode(new PositionNode().setAcceptsWhitespace(true))
        .returnDownNode(createNonTerminalNode("second quote symbol"))
        .returnDownNode(new PositionNode())
        .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode("'"));
        nodeMap.get("second quote symbol").parent
        .returnRightNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("second terminal character"))
        .returnDownNode(new AntiNode().linkDownNode(nodeMap.get("second quote symbol")))
        .returnRightNode(new PositionNode())
        .linkDownNode(nodeMap.get("terminal character"));
        nodeMap.get("second terminal character").parent
        .returnRightNode(new PositionNode())
        .returnDownNode(new LoopNode())
        .linkDownNode(nodeMap.get("second terminal character"));
        nodeMap.get("second terminal character").parent.getRightNode()
        .returnRightNode(new PositionNode())
        .linkDownNode(nodeMap.get("second quote symbol"));
        nodeMap.get("terminal string").parent
        .returnRightNode(new OrNode())
        .returnDownNode(createNonTerminalNode("special sequence"))
        .returnDownNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("special symbol"))
        .returnDownNode(new PositionNode())
        .returnDownNode(TerminalNodeFactory.cstn("?"));
        nodeMap.get("special sequence").getDownNode()
        .returnRightNode(new PositionNode())
        .returnDownNode(new LoopNode())
        .returnDownNode(createNonTerminalNode("special sequence character"))
        .returnDownNode(new AntiNode())
        .linkDownNode(nodeMap.get("special symbol"))
        .returnRightNode(new PositionNode().linkDownNode(nodeMap.get("terminal character")));
        nodeMap.get("special sequence").getDownNode().getRightNode()
        .returnRightNode(new PositionNode().linkDownNode(nodeMap.get("special symbol")));
        nodeMap.get("special sequence").parent
        .returnRightNode(new OrNode())
        .returnDownNode(createNonTerminalNode("optional sequence"))
        .returnDownNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("start option symbol"))
        .returnDownNode(new PositionNode())
        .returnDownNode(TerminalNodeFactory.cstn("["));
        nodeMap.get("optional sequence").getDownNode()
        .returnRightNode(new PositionNode().linkDownNode(nodeMap.get("definitions list")))
        .returnRightNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("end option symbol"))
        .returnDownNode(TerminalNodeFactory.cstn("]"));
        nodeMap.get("optional sequence").parent
        .returnRightNode(new OrNode())
        .returnDownNode(createNonTerminalNode("repeated sequence"))
        .returnDownNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("start repeat symbol"))
        .returnDownNode(TerminalNodeFactory.cstn("{"));
        nodeMap.get("repeated sequence").getDownNode()
        .returnRightNode(new PositionNode().linkDownNode(nodeMap.get("definitions list")))
        .returnRightNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("end repeat symbol"))
        .returnDownNode(TerminalNodeFactory.cstn("}"));
        nodeMap.get("repeated sequence").parent
        .returnRightNode(new OrNode())
        .returnDownNode(createNonTerminalNode("grouped sequence"))
        .returnDownNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("start group symbol"))
        .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode("("));
         nodeMap.get("grouped sequence").getDownNode()
        .returnRightNode(new PositionNode().linkDownNode(nodeMap.get("definitions list")))
        .returnRightNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("end group symbol"))
        .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode(")"));
        nodeMap.get("grouped sequence").parent
        .returnRightNode(new OrNode())
        .returnDownNode(createNonTerminalNode("empty sequence"))
        .returnDownNode(new PositionNode())
        .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode(""));
        nodeMap.get("syntactic factor").parent
        .returnRightNode(new PositionNode())
        .returnDownNode(new LoopNode(1))
        .returnDownNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("except symbol"))
        .returnDownNode(new PositionNode())
        .returnDownNode(TerminalNodeFactory.cstn("-"));
        nodeMap.get("except symbol").parent
        .returnRightNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("syntactic exception"))
        .returnDownNode(new PositionNode())
        /*
         *  In this place the spec defines, that a syntactic exception must not create
         *  self references like xx = "A" - xx; that could lead to paradoxes.
         *  be aware that this restriction is not enforced by this API
         */
        .linkDownNode(nodeMap.get("syntactic primary"));
        nodeMap.get("syntactic term").parent
        .returnRightNode(new PositionNode())
        .returnDownNode(new LoopNode())
        .returnDownNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("concatenate symbol"))
        .returnDownNode(new PositionNode())
        .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode(","));
         nodeMap.get("concatenate symbol").parent
        .returnRightNode(new PositionNode().linkDownNode(nodeMap.get("syntactic term")));
         nodeMap.get("single definition").parent
        .returnRightNode(new PositionNode())
        .returnDownNode(new LoopNode())
        .returnDownNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("definition separator symbol"))
        .returnDownNode(new PositionNode())
        .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode("|"));
        nodeMap.get("definition separator symbol").parent
        .returnRightNode(new PositionNode().linkDownNode(nodeMap.get("single definition")));
        nodeMap.get("definitions list").parent
        .returnRightNode(new PositionNode())
        .returnDownNode(createNonTerminalNode("terminator symbol"))
        .returnDownNode(new PositionNode())
        .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode(";"));
    return startNode;
  }

  public static Map<String, ParseNode> getNodeMap() {
    return nodeMap;
  }

  static ParseNode createNonTerminalNode(String nodeName) {
    NonTerminalNode node = new NonTerminalNode();
    node.setName(nodeName);
    nodeMap.put(nodeName, node);
    return node;
  }
}
