package com.sverko.ebnf;

import com.sverko.ebnf.tools.TerminalNodeFactory;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestParseBranches {
    Parser parser = new Parser();

    @Test
    public void testNtnPnTnSingleCharacter(){
        /* simple test to check whether a linear combination of a NonTerminalNode -> PositionNode -> TerminalNode
           yields the expected result i.e. to return the index of the token in the TokenQueue which is specified
           in the TerminalNode
         */
        NonTerminalNode startNode = new NonTerminalNode("A");
        startNode.tokens = new TokenQueue(Arrays.asList("a"));
        startNode.returnDownNode(new PositionNode())
                 .setDownNode(TerminalNodeFactory.createSimpleTerminalNode("a"));
        assertEquals(startNode.callReceived(startNode.tokens.getFirstToken()), 1);
    }

    @Test
    public void testNtnPnTnMultipleCharacters(){
        /*
         same as in the testNtnPnTnSingleCharacter now with a multiple character String
         and the TokenQueue having multiple tokens of which the token searched for
         is in the first position
        */
        NonTerminalNode startNode = new NonTerminalNode("Start");
        startNode.tokens = new TokenQueue(Arrays.asList("abcd","efgh","ijkl"));
        startNode.returnDownNode(new PositionNode())
                .setDownNode(TerminalNodeFactory.createSimpleTerminalNode("abcd"));
        assertEquals(startNode.callReceived(startNode.tokens.getFirstToken()), 1);
    }
}
