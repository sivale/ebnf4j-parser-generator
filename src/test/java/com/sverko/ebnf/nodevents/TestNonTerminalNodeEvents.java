package com.sverko.ebnf.nodevents;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sverko.ebnf.EbnfParseTree;
import com.sverko.ebnf.ParseNode;
import com.sverko.ebnf.Parser;
import com.sverko.ebnf.tools.NodeListeners;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TestNonTerminalNodeEvents{
  Parser parser = new Parser();
  String nodeName=null;
  @Test
  public void testReceiveMetaIdentifierEvent(){
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("meta identifier", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N","T","N","=","\"", "a","\"", ",", "\"","b","\"",";"), startNode);
    assertEquals("meta identifier",nodeName);
  }

  @Test
  public void testReceiveLetterEvent(){
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("letter", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N","T","N","=","\"", "a","\"", ",", "\"","b","\"",";"), startNode);
    assertEquals("letter",nodeName);
  }

  @Test
  public void testReceiveMetaIdentifyingCharacterEvent(){
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("meta identifying character", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N","T","N","=","\"", "a","\"", ",", "\"","b","\"",";"), startNode);
    assertEquals("meta identifying character",nodeName);
  }

  @Test
  public void testReceiveDefiningSymbolEvent(){
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("defining symbol", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N","T","N","=","\"", "a","\"", ",", "\"","b","\"",";"), startNode);
    assertEquals("defining symbol",nodeName);
  }

  @Test
  public void testReceiveDefinitionsListEvent(){
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("definitions list", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N","T","N","=","\"", "a","\"", ",", "\"","b","\"",";"), startNode);
    assertEquals("definitions list",nodeName);
  }

  @Test
  public void testReceiveSingleDefinitionEvent(){
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("single definition", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N","T","N","=","\"", "a","\"", ",", "\"","b","\"",";"), startNode);
    assertEquals("single definition",nodeName);
  }

  @Test
  public void testReceiveSyntacticTermEvent(){
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("syntactic term", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N","T","N","=","\"","a","\"", ",", "\"","b","\"",";"), startNode);
    assertEquals("syntactic term",nodeName);
  }

  @Test
  public void testReceiveSyntacticFactorEvent(){
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("syntactic factor", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N","T","N","=","\"", "a","\"", ",", "\"","b","\"",";"), startNode);
    assertEquals("syntactic factor",nodeName);
  }

  @Test
  public void testReceiveIntegerEvent(){
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("integer", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N","T","N","=","2","5","*","\"", "a","\"", ",", "\"","b","\"",";"), startNode);
    assertEquals("integer",nodeName);
  }

  @Test
  public void testReceiveSyntacticPrimaryEvent(){
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("syntactic primary", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N","T","N","=","2","5","*","\"", "a","\"", ",", "\"","b","\"",";"), startNode);
    assertEquals("syntactic primary",nodeName);
  }

  @Test
  public void testReceiveTerminalStringEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("terminal string", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "2", "5", "*", "\"", "a","\"", ",", "\"","b","\"", ";"), startNode);
    assertEquals("terminal string", nodeName);
  }
  @Test
  public void testReceiveFirstQuoteSymbolEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("first quote symbol", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "2", "5", "*", "\"", "a","\"", ",", "\"","b","\"", ";"), startNode);
    assertEquals("first quote symbol", nodeName);
  }

  @Test
  public void testReceiveFirstTerminalCharacterEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("first terminal character", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "2", "5", "*", "\"", "a","\"", ",", "\"","b","\"", ";"), startNode);
    assertEquals("first terminal character", nodeName);
  }

  @Test
  public void testReceiveSecondQuoteSymbolEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("second quote symbol", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "2", "5", "*", "'", "a","'", ",", "\"","b","\"", ";"), startNode);
    assertEquals("second quote symbol", nodeName);
  }

  @Test
  public void testReceiveSecondTerminalCharacterEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("second terminal character", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "2", "5", "*", "'", "a","'", ",", "\"","b","\"", ";"), startNode);
    assertEquals("second terminal character", nodeName);
  }

  @Test
  public void testReceiveSpecialSequenceEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("special sequence", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "?","B","M","P","?",",","'","a","'", ",", "\"","b","\"", ";"), startNode);
    assertEquals("special sequence", nodeName);
  }

  @Test
  public void testReceiveSpecialSequenceCharacterEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("special sequence character", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "?","B","M","P","?",",","'","a","'", ",", "\"","b","\"", ";"), startNode);
    assertEquals("special sequence character", nodeName);
  }

  @Test
  public void testReceiveSpecialSymbolEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("special symbol", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "?","B","M","P","?",",","'","a","'", ",", "\"","b","\"", ";"), startNode);
    assertEquals("special symbol", nodeName);
  }

  @Test
  public void testReceiveOptionalSequenceEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("optional sequence", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "[","A",",","B","]",",","'","a","'",",","\"","b","\"", ";"), startNode);
    assertEquals("optional sequence", nodeName);
  }

  @Test
  public void testReceiveStartOptionSymbolEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("start option symbol", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "[","A",",","B","]",",","'","a","'",",","\"","b","\"", ";"), startNode);
    assertEquals("start option symbol", nodeName);
  }

  @Test
  public void testReceiveEndOptionSymbolEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("end option symbol", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "[","A",",","B","]",",","'","a","'",",","\"","b","\"", ";"), startNode);
    assertEquals("end option symbol", nodeName);
  }

  @Test
  public void testReceiveRepeatedSequenceEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("repeated sequence", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "{","A",",","B","}",",","'","a","'",",","\"","b","\"", ";"), startNode);
    assertEquals("repeated sequence", nodeName);
  }

  @Test
  public void testReceiveStartRepeatSymbolEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("start repeat symbol", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "{","A",",","B","}",",","'","a","'",",","\"","b","\"", ";"), startNode);
    assertEquals("start repeat symbol", nodeName);
  }

  @Test
  public void testReceiveEndRepeatSymbolEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("end repeat symbol", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "{","A",",","B","}",",","'","a","'",",","\"","b","\"", ";"), startNode);
    assertEquals("end repeat symbol", nodeName);
  }

  @Test
  public void testReceiveGroupedSequenceEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("grouped sequence", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "(","A",",","B",")",",","'","a","'",",","\"","b","\"", ";"), startNode);
    assertEquals("grouped sequence", nodeName);
  }

  @Test
  public void testReceiveStartGroupSymbolEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("start group symbol", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "(","A",",","B",")",",","'","a","'",",","\"","b","\"", ";"), startNode);
    assertEquals("start group symbol", nodeName);
  }

  @Test
  public void testReceiveEndGroupSymbolEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("end group symbol", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "(","A",",","B",")",",","'","a","'",",","\"","b","\"", ";"), startNode);
    assertEquals("end group symbol", nodeName);
  }

  @Test
  public void testReceiveEmptySequenceEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("empty sequence", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "",",","'","a","'",",","\"","b","\"", ";"), startNode);
    assertEquals("empty sequence", nodeName);
  }

  @Test
  public void testReceiveDefinitionSeparatorSymbolEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("definition separator symbol", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "\"","a","\"","|","\"","b","\"", ";"), startNode);
    assertEquals("definition separator symbol", nodeName);
  }

  @Test
  public void testReceiveConcatenateSymbolEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("concatenate symbol", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "\"","a","\"",",","\"","b","\"", ";"), startNode);
    assertEquals("concatenate symbol", nodeName);
  }

  @Test
  public void testReceiveExceptSymbolEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("except symbol", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "A","B","C","-","\"","b","\"", ";"), startNode);
    assertEquals("except symbol", nodeName);
  }

  @Test
  public void testReceiveTerminatorSymbolEvent() {
    ParseNode startNode = EbnfParseTree.getStartNode();
    NodeListeners.assign("terminator symbol", startNode, e -> nodeName = e.parseNode.name);
    parser.parse(List.of("N", "T", "N", "=", "A","B","C","-","\"","b","\"", ";"), startNode);
    assertEquals("terminator symbol", nodeName);
  }
}
