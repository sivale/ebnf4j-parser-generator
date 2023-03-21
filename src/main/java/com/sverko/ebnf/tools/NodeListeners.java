package com.sverko.ebnf.tools;

import com.sverko.ebnf.EbnfParseTree;
import com.sverko.ebnf.NonTerminalNode;
import com.sverko.ebnf.ParseNode;
import com.sverko.ebnf.ParseNodeEventListener;
import java.util.ArrayList;
import java.util.List;

public class NodeListeners {
  public static void assign(String nodeName, ParseNode currentNode, ParseNodeEventListener listener){
    if (nodeName.equals(currentNode.name)){
      currentNode.addEventListener(listener);
    }
    if (currentNode.hasDownNode() && !currentNode.isAncestor(currentNode.downNode)){
      assign(nodeName, currentNode.downNode, listener);
    }
    if (currentNode.hasRightNode() && !currentNode.isAncestor(currentNode.rightNode)){
      assign(nodeName, currentNode.rightNode,listener);
    }
  }
}
