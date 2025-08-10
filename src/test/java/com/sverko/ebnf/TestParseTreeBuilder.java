package com.sverko.ebnf;

import com.sverko.ebnf.tools.NodeTreeComparisons;
import static com.sverko.ebnf.tools.TerminalNodeFactory.*;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestParseTreeBuilder {
  public static String QT = "\"";

  @Test
  public void testTerminalNode(){
    // A = "a";
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A","=",QT,"a",QT,";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode();
        ntn.returnDownNode(new PositionNode()).setDownNode(cstn("a")); //cstn() = TerminalNodeFactory.createSimpleTerminalNode()
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));

  }

  @Test
  public void testNonTerminalNode_1(){
    // A = B , "a";
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A","=","B",",",QT,"b",QT,";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new PositionNode()
        .setDownNode(new NonTerminalNode("B"))
        .setRightNode(new PositionNode().setDownNode(cstn("b")))
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testNonTerminalNode_2(){
    // A = "a", B;
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A","=",QT,"a",QT,",","B",";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new PositionNode()
            .setDownNode(cstn("a"))
            .setRightNode(new PositionNode()
                .setDownNode(new NonTerminalNode("B"))
            )
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testNonTerminalNode_3(){
    // A = "a" | B;
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A","=",QT,"a",QT,"|","B",";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new OrNode().setDownNode(new PositionNode().setDownNode(cstn("a")))
        .setRightNode(new OrNode().setDownNode(new PositionNode().setDownNode(new NonTerminalNode("B"))))
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testNonTerminalNode_4() {
    // A = { B } ;
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "{", "B", "}",";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new LoopNode().setDownNode(new NonTerminalNode("B")));
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testNonTerminalNode_5() {
    // A = [ B ] ;
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "[", "B", "]", ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new LoopNode(1).setDownNode(new NonTerminalNode("B")));
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testNonTerminalNode_6() {
    // A = 3* B ;
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "3", "*", "B", ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new LoopNode(3,3).setDownNode(new NonTerminalNode("B")));
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testOneConcatenationSymbol(){
    // A = "a","b";
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A","=",QT,"a",QT,",",QT,"b",QT,";"));
    generator.processEbnfSchema();
    ParseNode pn = new NonTerminalNode("top node");
    pn.returnDownNode(new PositionNode()).setDownNode(cstn("a")).setRightNode(new PositionNode().setDownNode(cstn("b")));
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(pn, generator.getFirstNode()));
  }

  @Test
  public void testThreeConcatenationSymbols(){
    // A = "a","b","c";
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A","=",QT,"a",QT,",",QT,"b",QT,",",QT,"c",QT,";"));
    generator.processEbnfSchema();
    ParseNode prn = new NonTerminalNode("top node");
    PositionNode pn = new PositionNode();
    pn.setDownNode(cstn("a")).returnRightNode(new PositionNode().setDownNode(cstn("b")).setRightNode(new PositionNode().setDownNode(cstn("c"))));
    prn.setDownNode(pn);
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(prn, generator.getFirstNode()));
  }

  @Test
  public void testOneSeparatorSymbol(){
    // A = "a"|"b";
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A","=",QT,"a",QT,"|",QT,"b",QT,";"));
    generator.processEbnfSchema();
    ParseNode pn = new NonTerminalNode("top node");
    ParseNode or1 = new OrNode().setDownNode(new PositionNode().setDownNode(cstn("a")));
    ParseNode or2 = new OrNode().setDownNode(new PositionNode().setDownNode(cstn("b")));
    or1.setRightNode(or2);
    pn.setDownNode(or1);
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(pn, generator.getFirstNode()));
  }

  @Test
  public void testTwoSeparatorSymbols() {
    // A = "a"|"b"|"c";
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", QT, "a", QT, "|", QT, "b", QT,"|", QT, "c", QT, ";"));
    generator.processEbnfSchema();
    ParseNode pn = new NonTerminalNode("top node");
    ParseNode or1 = new OrNode().setDownNode(new PositionNode().setDownNode(cstn("a")));
    ParseNode or2 = new OrNode().setDownNode(new PositionNode().setDownNode(cstn("b")));
    ParseNode or3 = new OrNode().setDownNode(new PositionNode().setDownNode(cstn("c")));
    or1.returnRightNode(or2).setRightNode(or3);
    pn.setDownNode(or1);
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(pn, generator.getFirstNode()));
  }

  @Test
  public void testMixedConcatenateAndSeparatorSymbols_1(){
    // A = "a","b"|"c";
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A","=",QT,"a",QT,",",QT,"b",QT,"|",QT,"c",QT,";"));
    generator.processEbnfSchema();
    ParseNode pn = new NonTerminalNode("top node");
    OrNode or = new OrNode();
    PositionNode pos = new PositionNode();
        pos
        .setDownNode(cstn("a"))
        .returnRightNode(new PositionNode())
        .setDownNode(cstn("b"));
    or.setDownNode(pos);
    or.returnRightNode(new OrNode()).returnDownNode(new PositionNode()).setDownNode(cstn("c"));
    pn.setDownNode(or);
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(pn, generator.getFirstNode()));
  }

  @Test
  public void testMixedConcatenateAndSeparatorSymbols_2(){
    // A = "a"|"b","c";
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A","=",QT,"a",QT,"|",QT,"b",QT,",",QT,"c",QT,";"));
    generator.processEbnfSchema();
    ParseNode pn = new NonTerminalNode("top node");
    OrNode leftOr = new OrNode();
    OrNode rightOr = new OrNode();
    leftOr.returnDownNode(new PositionNode().setDownNode(cstn("a")));
    leftOr.setRightNode(rightOr);
    rightOr.returnDownNode(
            new PositionNode().setDownNode(cstn("b"))
        ).returnRightNode(
            new PositionNode().setDownNode(cstn("c")));
    pn.setDownNode(leftOr);
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(pn, generator.getFirstNode()));
  }
  @Test
  public void testMixedConcatenateAndSeparatorSymbols_3(){
    // A = "a","b"|"c","d";
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A","=",QT,"a",QT,",",QT,"b",QT,"|",QT,"c",QT,",",QT,"d",QT,";"));
    generator.processEbnfSchema();
    ParseNode pn = new NonTerminalNode("top node");
    OrNode leftOr = new OrNode();
    OrNode rightOr = new OrNode();
    leftOr.returnDownNode(
        new PositionNode().setDownNode(cstn("a")))
        .returnRightNode(new PositionNode().setDownNode(cstn("b")));
    rightOr.returnDownNode(
        new PositionNode().setDownNode(cstn("a")))
        .returnRightNode(new PositionNode().setDownNode(cstn("b")));
    leftOr.setRightNode(rightOr);
    pn.setDownNode(leftOr);
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(pn, generator.getFirstNode()));
  }

  @Test
  public void testMixedConcatenateAndSeparatorSymbols_4() {
    // A = "a"|"b","c"|"d";
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A","=",QT,"a",QT,"|",QT,"b",QT,",",QT,"c",QT,"|",QT,"d",QT,";"));
    generator.processEbnfSchema();
    ParseNode pn = new NonTerminalNode("top node");
    ParseNode or1 = new OrNode();
    ParseNode or2 = new OrNode();
    ParseNode or3 = new OrNode();
    or1.setDownNode(new PositionNode().setDownNode(cstn("a")));
    or2.returnDownNode(new PositionNode().setDownNode(cstn("b")))
        .returnRightNode(new PositionNode().setDownNode(cstn("c")));
    or3.setDownNode(new PositionNode().setDownNode(cstn("d")));
    or1.returnRightNode(or2).setRightNode(or3);
    pn.setDownNode(or1);
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(pn, generator.getFirstNode()));
  }

  @Test
  public void testMixedConcatenateAndSeparatorSymbols_5() {
    // A = "a","b"|"c"|"d"|"e"|"f","g";
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A","=",QT,"a",QT,",",QT,"b",QT,"|",QT,"c",QT,"|",QT,"d",QT,"|",QT,"e",QT,"|",QT,"f",QT,",",QT,"g",QT,";"));
    generator.processEbnfSchema();
    ParseNode pn = new NonTerminalNode("top node");
    ParseNode or1 = new OrNode();
    ParseNode or2 = new OrNode();
    ParseNode or3 = new OrNode();
    ParseNode or4 = new OrNode();
    ParseNode or5 = new OrNode();

    or1.returnDownNode(
        new PositionNode().setDownNode(cstn("a")).setRightNode(
        new PositionNode().setDownNode(cstn("b"))
        )
    );
    or2.setDownNode(new PositionNode().setDownNode(cstn("c")));
    or3.setDownNode(new PositionNode().setDownNode(cstn("d")));
    or4.setDownNode(new PositionNode().setDownNode(cstn("e")));
    or5.returnDownNode(
        new PositionNode().setDownNode(cstn("f")).setRightNode(
        new PositionNode().setDownNode(cstn("g"))
        )
    );
    or1.setRightNode(or2.setRightNode(or3.setRightNode(or4.setRightNode(or5))));
    pn.setDownNode(or1);
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(pn, generator.getFirstNode()));
  }

  @Test
  public void testGroupedSequence_1() {
    // A = "a",("b"|"c"),"d";
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A","=",QT,"a",QT,",","(",QT,"b",QT,"|",QT,"c",QT,")",",",QT,"d",QT,";"));
    generator.processEbnfSchema();
    ParseNode pn = new NonTerminalNode("top node");
    ParseNode pn1 = new PositionNode();
    ParseNode pn2 = new PositionNode();
    ParseNode pn3 = new PositionNode();
    pn1.setDownNode(cstn("a"));
    pn2.returnDownNode(new OrNode().setDownNode(new PositionNode().setDownNode(cstn("b"))))
            .returnRightNode(new OrNode().setDownNode(new PositionNode().setDownNode(cstn("c"))));
    pn3.setDownNode(cstn("d"));
    pn1.returnRightNode(pn2).setRightNode(pn3);
    pn.setDownNode(pn1);
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(pn, generator.getFirstNode()));
  }

  @Test
  public void testGroupedSequence_2() {
    // A = "a",("b","c"),"d";
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A","=",QT,"a",QT,",","(",QT,"b",QT,",",QT,"c",QT,")",",",QT,"d",QT,";"));
    generator.processEbnfSchema();
    ParseNode pn = new NonTerminalNode("top node");
    ParseNode pn1 = new PositionNode();
    pn1.setDownNode(cstn("a"));
    pn1.returnRightNode(new PositionNode().setDownNode(cstn("b")))
        .returnRightNode(new PositionNode().setDownNode(cstn("c")))
        .returnRightNode(new PositionNode().setDownNode(cstn("d")));
    pn.setDownNode(pn1);
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(pn, generator.getFirstNode()));
  }

  @Test
  public void testGroupedSequence_3() {
    // A = ("B","c");
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A","=", "(" ,"B" ,",", QT,"c", QT, ")", ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new PositionNode().setDownNode(new NonTerminalNode("B"))
        .setRightNode(new PositionNode().setDownNode(cstn("c")))
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testGroupedSequence_4() {
    // A = ("a","b")|"c";
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A","=", "(", QT, "a", QT, ",", QT, "b", QT, ")", "|", QT, "c", QT, ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new OrNode().setDownNode(new PositionNode().setDownNode(cstn("a"))
            .setRightNode(new PositionNode().setDownNode(cstn("b")))
        )
        .setRightNode(new OrNode().setDownNode(new PositionNode().setDownNode(cstn("c"))))
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testGroupedSequence_5() {
    // A = ({ "a" , "b" } , "c" );
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "(", "{", QT, "a", QT, ",", QT, "b", QT, "}", ",", QT, "c", QT, ")", ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new PositionNode()
        .setRightNode(new PositionNode().setDownNode(cstn("c")))
        .setDownNode(new LoopNode()
            .setDownNode(new PositionNode()
                .setDownNode(cstn("a"))
                .setRightNode(new PositionNode().setDownNode(cstn("b")))
            )
        )
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testNonTerminalNodeStitching_1() {
    // A = B,"c";
    // B = "d","e";

    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "B", ",", QT, "c", QT, ";", "B", "=", QT, "d", QT, ",", QT, "e", QT, ";"));
    generator.processEbnfSchema();
    ParseNode pn = new NonTerminalNode("top node");
    ParseNode stn = new NonTerminalNode("second ntn node");
    pn.returnDownNode(new PositionNode().setDownNode(stn))
        .setRightNode(new PositionNode().setDownNode(cstn("c")));
    stn.returnDownNode(new PositionNode().setDownNode(cstn("d")))
        .returnRightNode(new PositionNode()).setDownNode(cstn("e"));
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(pn, generator.getFirstNode()));
  }

  @Test
  public void testNonTerminalNodeStitching_2() {
    // A = B | "c";
    // B = "d" | "e";
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "B", "|", QT, "c", QT, ";", "B", "=", QT, "d", QT, "|", QT, "e", QT, ";"));
    generator.processEbnfSchema();
    ParseNode pn = new NonTerminalNode("top node");
    ParseNode or = new OrNode();
    or.setRightNode(new OrNode().setDownNode(new PositionNode().setDownNode(cstn("c"))))
         .returnDownNode(new NonTerminalNode("B"))
         .returnDownNode(new OrNode()).setDownNode(new PositionNode().setDownNode(cstn("d")))
            .returnRightNode(new OrNode().setDownNode(new PositionNode().setDownNode(cstn("e"))));
    pn.setDownNode(or);
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(pn, generator.getFirstNode()));
  }

  @Test
  public void testNonTerminalNodeStitching_3() {
    // A = B , "c";
    // B = "d" | "e";
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "B", ",", QT, "c", QT, ";", "B", "=", QT, "d", QT, "|", QT, "e", QT, ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.returnDownNode(new PositionNode().setRightNode(new PositionNode().setDownNode(cstn("c"))))
        .returnDownNode(new NonTerminalNode("B"))
              .returnDownNode(new OrNode())
                .setRightNode(new OrNode().setDownNode(new PositionNode().setDownNode(cstn("e"))))
                    .setDownNode(new PositionNode().setDownNode(cstn("d")));

    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testNonTerminalNodeStitching_4() {
    // A = B | "c";
    // B = "d" , "e";
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "B", "|", QT, "c", QT, ";", "B", "=", QT, "d", QT, ",", QT, "e", QT, ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.returnDownNode(new OrNode().setRightNode(new OrNode().setDownNode(new PositionNode().setDownNode(cstn("c")))))
        .returnDownNode(new NonTerminalNode("B"))
        .returnDownNode(new PositionNode().setDownNode(cstn("d")))
        .setRightNode(new PositionNode().setDownNode(cstn("e")));
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testLoopNode_1() {
    // A = { "b" };
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "{", QT, "b", QT, "}", ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.returnDownNode(new LoopNode().setDownNode( new PositionNode().setDownNode(cstn("b"))));
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testLoopNode_2() {
    // A = { "a" } , "b";
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "{", QT, "a", QT, "}", "," , QT, "b", QT, ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.returnDownNode(new PositionNode().setRightNode(new PositionNode().setDownNode(cstn("c")))
        .setDownNode(new LoopNode().setDownNode(new PositionNode()
            .setDownNode(cstn("b")))));
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testLoopNode_2b() {
    // A = [ "a" ] , "b";
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "[", QT, "a", QT, "]", "," , QT, "b", QT, ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.returnDownNode(new PositionNode().setRightNode(new PositionNode().setDownNode(cstn("c")))
        .setDownNode(new LoopNode(1).setDownNode(new PositionNode()
            .setDownNode(cstn("b")))));
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testLoopNode_3() {
    // A = "a" , { "b" } ;
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", QT, "a", QT, ",", "{", QT, "b", QT, "}", ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new PositionNode().setDownNode(cstn("a"))
                          .setRightNode(new PositionNode()
                                              .setDownNode(new LoopNode()
                                                                  .setDownNode(new PositionNode().setDownNode(cstn("b")))
                                              )
                          )
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }
  @Test
  public void testLoopNode_4() {
    // A = "a" | { "b" } ;
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", QT, "a", QT, "|", "{", QT, "b", QT, "}", ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode( new OrNode().setDownNode(new PositionNode().setDownNode(cstn("a")))
                          .setRightNode(new OrNode()
                                              .setDownNode(new PositionNode().setDownNode(
                                                  new LoopNode().setDownNode(
                                                      new PositionNode().setDownNode(
                                                          cstn("b")
                                                      )
                                                  )
                                              ))
                          )
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testLoopNode_5() {
    // A = { "a" } | "b" ;
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "{", QT, "a", QT, "}", "|" , QT, "b", QT, ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode( new OrNode().setDownNode(new LoopNode().setDownNode(new PositionNode().setDownNode(cstn("a"))))
                          .setRightNode(new OrNode().setDownNode(new PositionNode().setDownNode(cstn("b")))));
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testLoopNode_6() {
    // A = { "a" , "b" } ;
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "{", QT, "a", QT, ",", QT, "b", QT, "}", ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(
        new LoopNode()
            .setDownNode(new PositionNode()
                .setDownNode(cstn("a"))
                .setRightNode(new PositionNode().setDownNode(cstn("b"))
                )
            )
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testLoopNode_7() {
    // A = { "B" , "a" } ;
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "{", "B", ",", QT, "b", QT, "}", ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(
        new LoopNode()
            .setDownNode(new PositionNode()
                .setDownNode(new NonTerminalNode("B"))
                .setRightNode(new PositionNode().setDownNode(cstn("a"))
                )
            )
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testLoopNode_7b() {
    // A = { "a" , "B" } ;
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "{", QT, "b", QT, ",", "B", "}", ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new LoopNode()
        .setDownNode(new PositionNode()
            .setDownNode(cstn("b"))
            .setRightNode(new PositionNode()
                .setDownNode(new NonTerminalNode("B"))
            )
        )
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testLoopNode_8() {
    // A = { "a" , { "b" , "c" }} ;
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "{", QT, "a", QT, ",", "{", QT, "b", QT, ",", QT, "c", QT, "}", "}", ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(
        new LoopNode().setDownNode(new PositionNode()
            .setDownNode(cstn("a"))
            .setRightNode(new PositionNode()
                .setDownNode(new LoopNode()
                    .setDownNode(new PositionNode()
                            .setDownNode(cstn("b"))
                            .setRightNode(new PositionNode()
                                .setDownNode(cstn("c"))
                            )
                        )
                    )
                )
            )
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testLoopNode_9(){
    // A = {{ "a", "b" }, "c" };
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "{", "{", QT, "a", QT, ",", QT, "b", QT, "}", ",", QT, "c", QT, "}", ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new LoopNode()
        .setDownNode(new PositionNode()
            .setRightNode(new PositionNode().setDownNode(cstn("c")))
            .setDownNode(new LoopNode()
                .setDownNode(new PositionNode()
                    .setDownNode(cstn("a"))
                    .setRightNode(new PositionNode().setDownNode(cstn("b")))
                )
            )
        )
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testLoopNode_9b(){
    // A = [{ "a", "b" }, "c" ];
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "[", "{", QT, "a", QT, ",", QT, "b", QT, "}", ",", QT, "c", QT, "]", ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new LoopNode(1)
        .setDownNode(new PositionNode()
            .setRightNode(new PositionNode().setDownNode(cstn("c")))
            .setDownNode(new LoopNode()
                .setDownNode(new PositionNode()
                    .setDownNode(cstn("a"))
                    .setRightNode(new PositionNode().setDownNode(cstn("b")))
                )
            )
        )
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testLoopNode_9c(){
    // A = {[ "a", "b" ], c };
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "{", "[", QT, "a", QT, ",", QT, "b", QT, "]", ",", QT, "c", QT, "}", ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new LoopNode()
        .setDownNode(new PositionNode()
            .setRightNode(new PositionNode().setDownNode(cstn("c")))
            .setDownNode(new LoopNode(1)
                .setDownNode(new PositionNode()
                    .setDownNode(cstn("a"))
                    .setRightNode(new PositionNode().setDownNode(cstn("b")))
                )
            )
        )
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testLoopNode_10(){
    // A = { "a" | B };
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "{", QT, "a", QT, "|", "B", "}", ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new LoopNode()
        .setDownNode(new OrNode()
            .setDownNode(new PositionNode().setDownNode(cstn("a")))
            .setRightNode(new OrNode()
                .setDownNode(new PositionNode().setDownNode(new NonTerminalNode("B")))
            )
        )
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testMinMaxLoopNode_1() {
    // A = [ "a" ],"b" ;
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "[", QT, "a", QT, "]",",", QT, "b", QT,";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new PositionNode()
        .setDownNode(new LoopNode(1).setDownNode(new PositionNode().setDownNode(cstn("a"))))
        .setRightNode(new PositionNode().setDownNode(cstn("b")))
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testMinMaxLoopNode_2() {
    // A = 3 * "a" ;
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(
        List.of("A", "=", "3", "*", QT, "a", QT, ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new LoopNode(3,3)
        .setDownNode(new PositionNode()
            .setDownNode(cstn("a"))
        )
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testAntiNode_1() {
    // A = B - "a" ;
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(
        List.of("A", "=", "B", "-", QT, "a", QT, ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new AntiNode()
        .setDownNode(cstn("a"))
        .setRightNode(new PositionNode()
                 .setDownNode(new NonTerminalNode("B"))
             )
        );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

    @Test
  public void testSpecialSequence_1(){
    // A = ?WHITESPACE? ;
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(
        List.of("A", "=", "?", "WHITESPACE", "?", ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new PositionNode()
        .setDownNode(createCharacterRangeBasedTerminalNode(Character::isWhitespace,"?WHITESPACE?"))
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testSpecialSequence_2(){
    // A = "a" , ?WHITESPACE?, "b"
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(
        List.of("A", "=", QT, "a", QT, ",", "?", "WHITESPACE", "?", QT, "b", QT, ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new PositionNode().setDownNode(cstn("a"))
            .setRightNode(new PositionNode()
                .setDownNode(createCharacterRangeBasedTerminalNode(Character::isWhitespace,"?WHITESPACE")))
                .setRightNode(new PositionNode()
                    .setDownNode(cstn("b"))
                )
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testSpecialSequence_3(){
    // A = { ?WHITESPACE? };
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(
        List.of("A", "=", "{", "?", "WHITESPACE", "?", "}", ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new LoopNode().setDownNode(new PositionNode().setDownNode(createCharacterRangeBasedTerminalNode(Character::isWhitespace,"WHITESPACE?"))));
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testSpecialSequence_4(){
    // A = 😀 | ?BMP? ;
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", QT, "\uD83D\uDE00", QT, "|", "?", "BMP", "?", ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new OrNode().setDownNode(new PositionNode().setDownNode(cstn("\uD83D\uDE00")))
        .setRightNode(new OrNode().setDownNode(new PositionNode().setDownNode(createCharacterRangeBasedTerminalNode(Character::isBmpCodePoint,"?ANY?"))))
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }

  @Test
  public void testSpecialSequence_5(){
    // A = ( 😀 | ?BMP? ) , "b" ;
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "(", QT, "\uD83D\uDE00", QT, "|", "?", "BMP", "?", ")", ",", QT, "a", QT, ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new PositionNode()
        .setRightNode(new PositionNode().setDownNode(cstn("a")))
        .setDownNode(new OrNode()
                .setDownNode(new PositionNode().setDownNode(cstn("\uD83D\uDE00")))
                .setRightNode(new OrNode()
                    .setDownNode(new PositionNode().setDownNode(createCharacterRangeBasedTerminalNode(Character::isBmpCodePoint,"?ANY?")))
                )
            )
        );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }


  @Test
  public void testSpecialSequence_6(){
    // A = 3* ?BMP? ;
    EbnfParserGenerator generator = new EbnfParserGenerator();
    generator.startNode = EbnfParseTree.getStartNode();
    generator.loadEbnfSchema(List.of("A", "=", "3", "*", "?", "BMP", "?", ";"));
    generator.processEbnfSchema();
    ParseNode ntn = new NonTerminalNode("top node");
    ntn.setDownNode(new LoopNode(3,3)
        .setDownNode(new PositionNode()
            .setDownNode(createCharacterRangeBasedTerminalNode(Character::isBmpCodePoint,"?ANY?"))
        )
    );
    Assertions.assertTrue(NodeTreeComparisons.isSameStructure(ntn, generator.getFirstNode()));
  }
}
