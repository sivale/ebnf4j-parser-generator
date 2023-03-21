package com.sverko.ebnf;


import com.sverko.ebnf.tools.TerminalNodeFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class EbnfParseTree {
	
	static Stack<ParseNode> crumbs = new Stack<>();
	static Map<String,ParseNode> nodeMap = new HashMap<>();
	
	public static ParseNode getStartNode() {
		
	  ParseNode startNode=new PositionNode();
		
	  startNode.returnDownNode(createNonTerminalNode("syntax"))
		       .returnDownNode(new LoopNode())
		       .returnDownNode(new PositionNode())
		       .returnDownNode(createNonTerminalNode("syntax rule"))
		       .returnDownNode(new LoopNode().setName("loopy"))
		       .returnDownNode(throwCrumb(new PositionNode()))
		       .returnDownNode(createNonTerminalNode("meta identifier"))
		       .returnDownNode(throwCrumb(new PositionNode()))
		       .returnDownNode(createNonTerminalNode("letter"))
		       .returnDownNode(new PositionNode())
		       .returnDownNode(TerminalNodeFactory.createArrayBasedTerminalNode(new String[] {
		    		" ", "_",
						"a","b","c","d","e","f","g","h","i","j","k","l","m",
						"n","o","p","q","r","s","t","u","v","w","x","y","z",
						"A","B","C","D","E","F","G","H","I","J","K","L","M",
						"N","O","P","Q","R","S","T","U","V","W","X","Y","Z"}))
		       .swapNode(pickCrumbUp())
		       .returnRightNode(new PositionNode())
		       .returnDownNode(createNonTerminalNode("meta identifying character"))
		       .returnDownNode(new LoopNode())
		       .returnDownNode(throwCrumb(new OrNode()))
		       .returnDownNode(new PositionNode())
		       .linkDownNode(nodeMap.get("letter"))
		       .swapNode(pickCrumbUp())
		       .returnRightNode(new OrNode())
		       .returnDownNode(createNonTerminalNode("decimal digit"))
		       .returnDownNode(new PositionNode())
		       .returnDownNode(TerminalNodeFactory.createArrayBasedTerminalNode(new String[] {
		    		   "0","1","2","3","4","5","6","7","8","9"
		                }))
		       .swapNode(pickCrumbUp())
		       
		       .returnRightNode(throwCrumb(new PositionNode()))
		       .returnDownNode(createNonTerminalNode("defining symbol"))
		       .returnDownNode(new PositionNode())
		       .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode("="))
		       .swapNode(pickCrumbUp())
		       .returnRightNode(throwCrumb(new PositionNode()))
		       .returnDownNode(createNonTerminalNode("definitions list"))
		       
		       .returnDownNode(throwCrumb(new PositionNode()))
		       .returnDownNode(createNonTerminalNode("single definition"))
		       .returnDownNode(throwCrumb(new PositionNode()))
		       .returnDownNode(createNonTerminalNode("syntactic term"))
		       .returnDownNode(throwCrumb(new PositionNode()))
		       .returnDownNode(createNonTerminalNode("syntactic factor"))
		       
		       //.returnDownNode(throwCrumb(new OrNode()))
		       
		       .returnDownNode(throwCrumb(new PositionNode()))
		       .returnDownNode(new LoopNode())
		       .returnDownNode(throwCrumb(new PositionNode()))
		       .returnDownNode(createNonTerminalNode("integer"))
		       .returnDownNode(throwCrumb(new PositionNode()))
		       .linkDownNode(nodeMap.get("decimal digit"))
		       .swapNode(pickCrumbUp())
		       
		       .returnRightNode(new PositionNode())
		       .returnDownNode(new LoopNode())
		       .linkDownNode(nodeMap.get("decimal digit"))
		       .swapNode(pickCrumbUp())
		       .returnRightNode(new PositionNode())
		       .returnDownNode(createNonTerminalNode("repetition symbol"))
		       .returnDownNode(new PositionNode())
		       .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode("*"))
		       
		       .swapNode(pickCrumbUp())
		       .returnRightNode(new PositionNode())
		       .returnDownNode(createNonTerminalNode("syntactic primary"))
		       .returnDownNode(new OrNode().linkDownNode(nodeMap.get("meta identifier")))
		       .returnRightNode(throwCrumb(new OrNode()))
		       .returnDownNode(createNonTerminalNode("terminal string"))
		       
		       .returnDownNode(throwCrumb(new OrNode()))
		       .returnDownNode(throwCrumb(new PositionNode().setAcceptsWhitespace(true)))
		       .returnDownNode(createNonTerminalNode("first quote symbol"))
		       .returnDownNode(new PositionNode())
		       .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode("\""))
		       .swapNode(pickCrumbUp())
		       .returnRightNode(throwCrumb(new PositionNode()))
		       .returnDownNode(createNonTerminalNode("first terminal character"))
		       .returnDownNode(new AntiNode().linkDownNode(nodeMap.get("first quote symbol")))
		       .returnRightNode(new PositionNode())
			   .returnDownNode(createNonTerminalNode("terminal character"))
			   .returnDownNode(new PositionNode())
			   .returnDownNode(TerminalNodeFactory.createCharacterRangeBasedTerminalNode(Character::isDefined))
			   .swapNode(pickCrumbUp())
		       .returnRightNode(throwCrumb(new PositionNode()))
		       .returnDownNode(new LoopNode())
		       .linkDownNode(nodeMap.get("first terminal character"))
		       .swapNode(pickCrumbUp())
		       .returnRightNode(new PositionNode())
		       .linkDownNode(nodeMap.get("first quote symbol"))
		       .swapNode(pickCrumbUp())
		       
		       .returnRightNode((new OrNode()))
		       .returnDownNode(throwCrumb(new PositionNode().setAcceptsWhitespace(true)))
		       .returnDownNode(createNonTerminalNode("second quote symbol"))
		       .returnDownNode(new PositionNode())
		       .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode("'"))
		       .swapNode(pickCrumbUp())
		       .returnRightNode(throwCrumb(new PositionNode()))
		       .returnDownNode(createNonTerminalNode("second terminal character"))
		       .returnDownNode(new AntiNode().linkDownNode(nodeMap.get("second quote symbol")))
		       .returnRightNode(new PositionNode())
		       .linkDownNode(nodeMap.get("terminal character"))
		       .swapNode(pickCrumbUp())
		       .returnRightNode(throwCrumb(new PositionNode()))
		       .returnDownNode(new LoopNode())
		       .linkDownNode(nodeMap.get("second terminal character"))
		       .swapNode(pickCrumbUp())
		       .returnRightNode(new PositionNode())
		       .linkDownNode(nodeMap.get("second quote symbol"))
		       .swapNode(pickCrumbUp())
		       
		       .returnRightNode(throwCrumb(new OrNode()))
		       .returnDownNode(createNonTerminalNode("special sequence"))
		       .returnDownNode(throwCrumb(new PositionNode()))
		       .returnDownNode(createNonTerminalNode("special symbol"))
		       .returnDownNode(new PositionNode())
		       .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode("?"))
		       .swapNode(pickCrumbUp())
		       .returnRightNode(throwCrumb(new PositionNode()))
		       .returnDownNode(new LoopNode())
		       .returnDownNode(createNonTerminalNode("special sequence character"))
		       .returnDownNode(new AntiNode())
		       .linkDownNode(nodeMap.get("special symbol"))
		       .returnRightNode(new PositionNode().linkDownNode(nodeMap.get("terminal character")))
		       .swapNode(pickCrumbUp())
		       .returnRightNode(new PositionNode().linkDownNode(nodeMap.get("special symbol")))
		       .swapNode(pickCrumbUp())
			   		       
		       .returnRightNode(throwCrumb(new OrNode()))
		       .returnDownNode(createNonTerminalNode("optional sequence"))
		       .returnDownNode(throwCrumb(new PositionNode()))
		       .returnDownNode(createNonTerminalNode("start option symbol"))
		       .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode("["))
		       .swapNode(pickCrumbUp())
		       .returnRightNode(new PositionNode().linkDownNode(nodeMap.get("definitions list")))
		       .returnRightNode(new PositionNode())
		       .returnDownNode(createNonTerminalNode("end option symbol"))
		       .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode("]"))
		       .swapNode(pickCrumbUp())
		       
		       .returnRightNode(throwCrumb(new OrNode()))
		       .returnDownNode(createNonTerminalNode("repeated sequence"))
		       .returnDownNode(throwCrumb(new PositionNode()))
		       .returnDownNode(createNonTerminalNode("start repeat symbol"))
		       .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode("{"))
		       .swapNode(pickCrumbUp())
		       .returnRightNode(new PositionNode().linkDownNode(nodeMap.get("definitions list")))
		       .returnRightNode(new PositionNode())
		       .returnDownNode(createNonTerminalNode("end repeat symbol"))
		       .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode("}"))
		       .swapNode(pickCrumbUp())
		       
		       .returnRightNode(throwCrumb(new OrNode()))
		       .returnDownNode(createNonTerminalNode("grouped sequence"))
		       .returnDownNode(throwCrumb(new PositionNode()))
		       .returnDownNode(createNonTerminalNode("start group symbol"))
		       .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode("("))
		       .swapNode(pickCrumbUp())
		       .returnRightNode(new PositionNode().linkDownNode(nodeMap.get("definitions list")))
		       .returnRightNode(new PositionNode())
		       .returnDownNode(createNonTerminalNode("end group symbol"))
		       .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode(")"))
		       .swapNode(pickCrumbUp())
		       
		       .returnRightNode(new OrNode())
		       .returnDownNode(createNonTerminalNode("empty sequence"))
		       .returnDownNode(new PositionNode())
		       .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode(""))
		       .swapNode(pickCrumbUp())
		       //.returnRightNode(new OrNode().linkDownNode(nodeMap.get("syntactic primary")))
		       //.swapNode(pickCrumbUp())
		       
		       .returnRightNode(new PositionNode())
		       .returnDownNode(new LoopNode(1))
		       .returnDownNode(throwCrumb(new PositionNode()))
		       .returnDownNode(createNonTerminalNode("except symbol"))
		       .returnDownNode(new PositionNode())
		       .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode("-"))
		       .swapNode(pickCrumbUp())
		       
		       .returnRightNode(new PositionNode())
		       .returnDownNode(createNonTerminalNode("syntactic exception"))
		       .returnDownNode(new PositionNode())
		       .returnDownNode(createNonTerminalNode("syntactic factor no meta"))
		       .linkDownNode(nodeMap.get("syntactic primary").getDownNode())
		       .swapNode(pickCrumbUp())
		       
		       .returnRightNode(new PositionNode())
		       .returnDownNode(new LoopNode())
		       .returnDownNode(throwCrumb(new PositionNode()))
		       .returnDownNode(createNonTerminalNode("concatenate symbol"))
		       .returnDownNode(new PositionNode())
		       .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode(","))
		       .swapNode(pickCrumbUp())
		       .returnRightNode(new PositionNode().linkDownNode(nodeMap.get("syntactic term")))
		       .swapNode(pickCrumbUp())
		       
		       .returnRightNode(new PositionNode())
		       .returnDownNode(new LoopNode())
		       .returnDownNode(throwCrumb(new PositionNode()))
		       .returnDownNode(createNonTerminalNode("definition separator symbol"))
		       .returnDownNode(new PositionNode())
		       .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode("|"))
		       .swapNode(pickCrumbUp())
		       .returnRightNode(new PositionNode().linkDownNode(nodeMap.get("single definition")))
		       .swapNode(pickCrumbUp())
		       
		       .returnRightNode(new PositionNode())
		       .returnDownNode(createNonTerminalNode("terminator symbol"))
		       .returnDownNode(new PositionNode())
		       .returnDownNode(TerminalNodeFactory.createSimpleTerminalNode(";"));
	  
	  			return startNode;
	}
	public static Map<String,ParseNode> getNodeMap () {
		return nodeMap;
	}
	
	static ParseNode createNonTerminalNode(String nodeName){
		NonTerminalNode node = new NonTerminalNode();
		node.setName(nodeName);
		nodeMap.put(nodeName, node);
		return node;
	}
	
	static ParseNode throwCrumb(ParseNode node){
		crumbs.push(node);
		return node;
	}
	
	static ParseNode pickCrumbUp(){
		return crumbs.pop();
	}
}
