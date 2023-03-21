package com.sverko.ebnf;

public class AntiNode extends ParseNode {

    @Override
    public int callReceived(int token) {
        int downNodeResult = downNode.callReceived(token);
        if (downNodeResult == ParseNode.NOT_FOUND) {
            if (hasRightNode()) {
               return rightNode.callReceived(token);
            } else {
                return ++token;
            }
        }
        return ParseNode.NOT_FOUND;
    }
}
