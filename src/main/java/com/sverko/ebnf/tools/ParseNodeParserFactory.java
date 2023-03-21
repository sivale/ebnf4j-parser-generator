package com.sverko.ebnf.tools;

import com.sverko.ebnf.ParseNode;
import com.sverko.ebnf.Parser;


public class ParseNodeParserFactory {
    public static void assign(ParseNode currentNode, Parser parser){
        currentNode.tokens = parser.getTokenQueue();

        if (currentNode.hasDownNode() && !currentNode.isAncestor(currentNode.downNode)){
            assign(currentNode.downNode, parser);
        }
        if (currentNode.hasRightNode() && !currentNode.isAncestor(currentNode.rightNode)){
            assign(currentNode.rightNode,parser);
        }
    }
}
