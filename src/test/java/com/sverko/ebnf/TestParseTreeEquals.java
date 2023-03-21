package com.sverko.ebnf;

import com.sverko.ebnf.tools.NodeTreeComparisons;
import com.sverko.ebnf.tools.TerminalNodeFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestParseTreeEquals {
    /*
    This Class tests whether two ParseNodes have the same subnodes
    which means that:
    1.) the types and the connections of all the subnodes are equal
    2.) the state of the subnodes in the same position is equal ( for instance a LoopNode has same max and min fields )
     */

    @Test
    public void testForEquality_01(){
            /*
    +-----+     +-----+
    | NTN |     | NTN |
    +-----+     +-----+
       |           |
       v           v
    +-----+     +-----+
    | PN  |     | PN  |
    +-----+     +-----+
       |           |
       v           v
    +-----+     +-----+
    | TN  |     | TN  |
    +-----+     +-----+
        */
        ParseNode p1 = new NonTerminalNode("StartNode");
        p1.returnDownNode(new PositionNode()).setDownNode(TerminalNodeFactory.createSimpleTerminalNode("END"));
        ParseNode p2 = new NonTerminalNode("StartNode");
        p2.returnDownNode(new PositionNode()).setDownNode(TerminalNodeFactory.createSimpleTerminalNode("END"));
        assertTrue(NodeTreeComparisons.isSameStructure(p1,p2));
    }

    @Test
    public void testForInequality_01(){
        /*
    +-----+     +-----+
    | NTN |     | NTN |
    +-----+     +-----+
       |           |
       v           v
    +-----+     +-----+
    | PN  |     | LN  |
    +-----+     +-----+
       |           |
       v           v
    +-----+     +-----+
    | TN  |     | TN  |
    +-----+     +-----+
        */
        ParseNode p1 = new NonTerminalNode("StartNode");
        p1.returnDownNode(new PositionNode()).setDownNode(TerminalNodeFactory.createSimpleTerminalNode("END"));
        ParseNode p2 = new NonTerminalNode("StartNode");
        p2.returnDownNode(new LoopNode()).setDownNode(TerminalNodeFactory.createSimpleTerminalNode("END"));
        assertFalse(NodeTreeComparisons.isSameStructure(p1,p2));
    }
}
