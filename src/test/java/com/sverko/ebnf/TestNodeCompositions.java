package com.sverko.ebnf;

import com.sverko.ebnf.tools.TerminalNodeFactory;
import java.util.List;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestNodeCompositions {

    @Test
    public void testThreeNTNsJoinedByPn (){
        /*
            Search Pattern:
            A = B , C;
            B = "bb";
            C = "cc";
            Search String: "bbcc"
         */
        NonTerminalNode ntn = new NonTerminalNode("A");
        ntn.tokens = new TokenQueue(Arrays.asList("bb","cc"));
        ParseNode pn1 = ntn.returnDownNode(new PositionNode());
        pn1.returnDownNode(new NonTerminalNode("B"))
                .setDownNode(TerminalNodeFactory.createSimpleTerminalNode("bb"));
        pn1.returnRightNode(new PositionNode())
                .returnDownNode(new NonTerminalNode("C"))
                .setDownNode(TerminalNodeFactory.createSimpleTerminalNode("cc"));
        assertEquals(ntn.callReceived(0),2);
    }

    @Test
    public void testThreeNTNsJoinedByOr (){
        /*
           Search Pattern:
           A = B | C;
           B = "bb";
           C = "cc";
           Search String: "cc"

         */
        NonTerminalNode ntn = new NonTerminalNode("A");
        ntn.tokens = new TokenQueue(List.of("cc"));
        ParseNode or1 = ntn.returnDownNode(new OrNode());
        or1.returnDownNode(new NonTerminalNode("B"))
                .setDownNode(TerminalNodeFactory.createSimpleTerminalNode("bb"));
        or1.returnRightNode(new OrNode())
                .returnDownNode(new NonTerminalNode("C"))
                .setDownNode(TerminalNodeFactory.createSimpleTerminalNode("cc"));
        assertEquals(ntn.callReceived(0),1);
    }
}

