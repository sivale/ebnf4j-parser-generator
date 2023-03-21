package com.sverko.ebnf;

import com.sverko.ebnf.tools.TerminalNodeFactory;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSingleNodesFunctionality {
    @Test
    public void testTerminalNodeStringAcceptance (){
        TerminalNode tn = TerminalNodeFactory.createSimpleTerminalNode("ghi");
        tn.tokens = new TokenQueue(Arrays.asList("abc","def","ghi","klm"));
        assertEquals (tn.callReceived(2),3);
    }
    @Test
    public void testTerminalNodeStringRejection (){
        TerminalNode tn = TerminalNodeFactory.createSimpleTerminalNode("ghi");
        tn.tokens = new TokenQueue(Arrays.asList("abc","def","ghi","klm"));
        assertEquals (tn.callReceived(1),ParseNode.NOT_FOUND);
    }
    @Test
    public void testTerminalNodeStringRangeAcceptance (){
        TerminalNode tn = TerminalNodeFactory.createArrayBasedTerminalNode( new String[]{"a","b","c"});
        tn.tokens = new TokenQueue(Arrays.asList("x","y","a","z"));
        assertEquals(tn.callReceived(2), 3);
    }
    @Test
    public void testTerminalNodeStringRangeRejection (){
        TerminalNode tn = TerminalNodeFactory.createArrayBasedTerminalNode( new String[]{"a","b","c"});
        tn.tokens = new TokenQueue(Arrays.asList("x","y","a","z"));
        assertEquals(tn.callReceived(1), ParseNode.NOT_FOUND);
    }
    @Test
    public void testTerminalNodeStringRangeStringRejection (){
        TerminalNode tn = TerminalNodeFactory.createArrayBasedTerminalNode( new String[]{"a","b","c"});
        tn.tokens = new TokenQueue(Arrays.asList("xy","ab","zz"));
        assertEquals(tn.callReceived(1), ParseNode.NOT_FOUND);
    }
    @Test
    public void testPositionNodeTokenFound (){
        PositionNode pn = new PositionNode();
        pn.tokens = new TokenQueue(Arrays.asList("ab","bc"));
        pn.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("bc"));
        assertEquals(pn.callReceived(1),2);
    }
    @Test
    public void testPositionNodeTokenNotFound (){
        PositionNode pn = new PositionNode();
        pn.tokens = new TokenQueue(Arrays.asList("ab","bc"));
        pn.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("de"));
        assertEquals(pn.callReceived(1),ParseNode.NOT_FOUND);
    }
    @Test
    public void testChainedPositionNodesAllTokensFound (){
        // A = "ab","cd";  QUEUE {"abcd"}
        PositionNode pn = new PositionNode();
        pn.tokens = new TokenQueue(Arrays.asList("ab","cd"));
        pn.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("ab"))
                .returnRightNode(new PositionNode())
                .setDownNode(TerminalNodeFactory.createSimpleTerminalNode("cd"));
        assertEquals(pn.callReceived(0),2);
    }
    @Test
    public void testChainedPositionNodesNotEnoughTokens (){
        // A = "ab","cd"; QUEUE {"ab"}
        PositionNode pn = new PositionNode();
        pn.tokens = new TokenQueue(Arrays.asList("ab"));
        pn.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("ab"))
                .returnRightNode(new PositionNode())
                .setDownNode(TerminalNodeFactory.createSimpleTerminalNode("cd"));
        assertEquals(pn.callReceived(0),PositionNode.END_OF_QUEUE);
    }
    @Test
    public void testEndlessLoopNodeAllTokensFound (){
        // find {"ab"} in "abababc"
        LoopNode ln = new LoopNode();
        ln.tokens = new TokenQueue(Arrays.asList("ab","ab","ab","c"));
        ln.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("ab"));
        assertEquals(ln.callReceived(0),3);
    }
    @Test
    public void testEndlessLoopNodeNoTokensFound (){
        // find {"c"} in "abababc"
        LoopNode ln = new LoopNode();
        ln.tokens = new TokenQueue(Arrays.asList("ab","ab","ab","c"));
        ln.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("c"));
        assertEquals(ln.callReceived(0),0);
    }
    @Test
    public void testEndlessLoopNodeEndOfQueue (){
        // find {"ab"} in "ababab"
        LoopNode ln = new LoopNode();
        ln.tokens = new TokenQueue(Arrays.asList("ab","ab","ab"));
        ln.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("ab"));
        assertEquals(ln.callReceived(0),3);
    }
    @Test
    public void testEndlessLoopNodeNotStartingFromZero (){
        // find {"ab"} in bbababcc;
        ParseNode pn1 = new PositionNode("first PN");
        ParseNode pn2 = new PositionNode("second PN");
        ParseNode pn3 = new PositionNode("third PN");
        ParseNode ln = new LoopNode();
        TokenQueue tokens = new TokenQueue(Arrays.asList("bb","ab","ab","cc"));
        pn1.tokens = tokens;
        pn2.tokens = tokens;
        pn3.tokens = tokens;
        ln.tokens = tokens;
        pn1.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("bb"));
        pn1.setRightNode(pn2
            .setDownNode(ln
                .setDownNode(pn3
                    .setDownNode(TerminalNodeFactory.createSimpleTerminalNode("ab"))
                )
            )
        );
        assertEquals(pn1.callReceived(0),3);

    }
    @Test
    public void testOptionalLoopNodeTokenFound (){
        // find "cd",["ab"] in "cdab"
        PositionNode pn1 = new PositionNode();
        pn1.tokens = new TokenQueue(Arrays.asList("cd","ab"));
        pn1.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("cd")).
                returnRightNode(new PositionNode()).
                returnDownNode( new LoopNode(1)).
                setDownNode(TerminalNodeFactory.createSimpleTerminalNode("ab"));

       assertEquals(pn1.callReceived(0), 2);
    }
    @Test
    public void testOptionalLoopNodeTokenNotFound (){
        // find "cd",["ab"] in "cdxy"
        PositionNode pn1 = new PositionNode();
        pn1.tokens = new TokenQueue(Arrays.asList("cd","xy"));
        pn1.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("cd")).
                returnRightNode(new PositionNode()).
                returnDownNode( new LoopNode(1)).
                setDownNode(TerminalNodeFactory.createSimpleTerminalNode("ab"));

        assertEquals(pn1.callReceived(0), 1);
    }

    @Test
    public void testOptionalLoopNodeStopWhenFound (){
        LoopNode ln = new LoopNode(1);
        ln.tokens = new TokenQueue(Arrays.asList("aa","aa"));
        ln.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("aa"));
        assertEquals(ln.callReceived(0), 1);
    }

    @Test
    public void testOptionalLoopNodeEndOfQueue (){
        LoopNode ln = new LoopNode(1);
        ln.tokens = new TokenQueue(Arrays.asList("bb"));
        ln.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("aa"));
        assertEquals(ln.callReceived(1), 1);
    }
    @Test
    public void testOptionalLoopNodeFoundLessThanMax (){
        LoopNode ln = new LoopNode(3);
        ln.tokens = new TokenQueue(Arrays.asList("aa","aa","bb"));
        ln.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("aa"));
        assertEquals(ln.callReceived(0), 2);
    }

    @Test
    public void testIntegerLoopNodeEndOfQueue (){
        LoopNode ln = new LoopNode(2,2);
        ln.tokens = new TokenQueue(Arrays.asList("aa","aa"));
        ln.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("aa"));
        assertEquals(ln.callReceived(0), 2);
    }

    @Test
    public void testIntegerLoopNodeFoundMax (){
        LoopNode ln = new LoopNode(2,2);
        ln.tokens = new TokenQueue(Arrays.asList("aa","aa","bb"));
        ln.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("aa"));
        assertEquals(ln.callReceived(0), 2);
    }

    @Test
    public void testIntegerLoopNodeNotEnough (){
        LoopNode ln = new LoopNode(2,2);
        ln.tokens = new TokenQueue(Arrays.asList("aa","bb","bb"));
        ln.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("aa"));
        assertEquals(ln.callReceived(0), -1);
    }

    @Test void testLoopNodeBetweenMaxMin (){
        LoopNode ln = new LoopNode(1,3);
        ln.tokens = new TokenQueue(Arrays.asList("aa","aa","bb"));
        ln.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("aa"));
        assertEquals(ln.callReceived(0), 2);
    }

    @Test void testLoopNodeMaxReached (){
        LoopNode ln = new LoopNode(1,3);
        ln.tokens = new TokenQueue(Arrays.asList("aa","aa","aa","aa"));
        ln.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("aa"));
        assertEquals(ln.callReceived(0), 3);
    }

    @Test void testLoopNodeMaxReachedEndOfQueue (){
        LoopNode ln = new LoopNode(1,3);
        ln.tokens = new TokenQueue(Arrays.asList("aa","aa","aa"));
        ln.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("aa"));
        assertEquals(ln.callReceived(0), 3);
    }

    @Test void testSingleOrNode (){
        OrNode on = new OrNode();
        on.tokens = new TokenQueue(Arrays.asList("aa","bb"));
        on.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("aa"));
        assertEquals(on.callReceived(0),1);
    }

    @Test void testOrNodeSecondFound (){
        OrNode on = new OrNode();
        on.tokens = new TokenQueue(Arrays.asList("bb","cc"));
        on.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("aa"));
        on.returnRightNode(new OrNode()).setDownNode(TerminalNodeFactory.createSimpleTerminalNode("bb"));
        assertEquals(on.callReceived(0),1);
    }

    @Test void testOrNodeSecondNotFound (){
        OrNode on = new OrNode();
        on.tokens = new TokenQueue(Arrays.asList("cc"));
        on.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("aa"));
        on.returnRightNode(new OrNode()).setDownNode(TerminalNodeFactory.createSimpleTerminalNode("bb"));
        assertEquals(on.callReceived(0),-1);
    }

    @Test void testOrNodeSecondEndOfQueue (){
        OrNode on = new OrNode();
        on.tokens = new TokenQueue(Collections.emptyList());
        on.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("aa"));
        on.returnRightNode(new OrNode()).setDownNode(TerminalNodeFactory.createSimpleTerminalNode("bb"));
        assertEquals(on.callReceived(0),-2);
    }
    @Test void testNonTerminalNodeTokensFound (){
        NonTerminalNode ntn = new NonTerminalNode("AABB");
        ntn.tokens = new TokenQueue(Arrays.asList("aa","bb"));
        ntn.returnDownNode(new PositionNode()).
                setDownNode(TerminalNodeFactory.createSimpleTerminalNode("aa")).
            returnRightNode(new PositionNode()).
                setDownNode(TerminalNodeFactory.createSimpleTerminalNode("bb"));
        ntn.callReceived(0);
        assertEquals("aabb", ntn.getResultString());
    }

    @Test void testAntiNodeTokenFound (){
        AntiNode an = new AntiNode();
        an.tokens = new TokenQueue(Arrays.asList("aa"));
        an.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("aa"));
        assertEquals(an.callReceived(0),-1);
    }

    @Test void testAntiNodeTokenNotFound (){
        AntiNode an = new AntiNode();
        an.tokens = new TokenQueue(Arrays.asList("aa"));
        an.setDownNode(TerminalNodeFactory.createSimpleTerminalNode("bb"));
        assertEquals(an.callReceived(0),1);
    }

    @Test void testAntiNodeTokenEndOfQueue (){
        // there can't be an end of queue because the AntiNode does not move to the next position
        // it repeats the received token, much like the OrNode
    }
}
