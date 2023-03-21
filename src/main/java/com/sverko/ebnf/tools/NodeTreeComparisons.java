package com.sverko.ebnf.tools;

import com.sverko.ebnf.ParseNode;

public class NodeTreeComparisons {
    public static boolean isSameStructure(ParseNode p1, ParseNode p2) {

        if (p1 == p2) {
            return true;
        }
        if (p1.equals(p2)) {
            if (p1.hasRightNode() == !p2.hasRightNode()) {
                return false;
            }
            if (p1.hasDownNode() == !p2.hasDownNode()) {
                return false;
            }
            if (p1.hasRightNode() && p2.hasRightNode()) {
                if (!isSameStructure(p1.rightNode, p2.rightNode)) return false;
            }
            if (p1.hasDownNode() && p2.hasDownNode()) {
                return isSameStructure(p1.downNode, p2.downNode);
            }
        return true;
        }
    return false;
    }
}
