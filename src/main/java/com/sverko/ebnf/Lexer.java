package com.sverko.ebnf;


import com.sverko.ebnf.tools.UTF8FileToStringArrayList;
import com.sverko.ebnf.tools.UnicodeString;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

public class Lexer {

  private static boolean IGNORE_WHITESPACE = true;
  private LexerNode rootNode = new LexerNode("");

  private enum MatchType {CASE_SENSITIVE, CASE_INSENSITIVE}


  BiFunction<LexerNode, String, Boolean> matchTester;

  private void setMatchTester(BiFunction<LexerNode, String, Boolean> matchTester) {
    this.matchTester = matchTester;
  }

  private void chooseMatchTester(MatchType type) {
    if (type == MatchType.CASE_SENSITIVE) {
      setMatchTester(
          (n, s) -> n.allowed.equals(s)
      );
    } else if (type == MatchType.CASE_INSENSITIVE) {
      setMatchTester(
          (n, s) -> n.allowed.equalsIgnoreCase(s)
      );
    }
  }

  public Lexer(){
    this (Collections.EMPTY_SET);
  }

  public Lexer(boolean ignoreWhitespace){
    this (Collections.EMPTY_SET, ignoreWhitespace);
  }

  public Lexer(Set<String> lexerTokens){
    this(lexerTokens, true);
  }

  public Lexer(Set<String> lexerTokens, Boolean ignoreWhitespace) {
    IGNORE_WHITESPACE = ignoreWhitespace;
    List<UnicodeString> unicodeStringList = new ArrayList<>();
    for (String s : lexerTokens) {
      unicodeStringList.add(new UnicodeString(s));
    }
    buildLexerTree(unicodeStringList);
    chooseMatchTester(MatchType.CASE_SENSITIVE);
  }




  public void buildLexerTree(List<UnicodeString> tokens) {
    LexerNode currentNode = null;

    for (int i = 0; i < tokens.size(); i++) {
      for (int k = 0; k < tokens.get(i).length(); k++) {

        if (k != 0) {
          if (currentNode.hasRightNode()) {
            currentNode = currentNode.getRightNode();
            if (tokens.get(i).getStringAt(k).equals(currentNode.allowed)) {
              //do nothing, moving the node to the right is sufficient
            } else {
              LexerNode matchingNode = findMatchingDownNode(currentNode, tokens.get(i).getStringAt(k));
              if (matchingNode == null){
                currentNode = getLastVerticalNode(currentNode);
                currentNode = currentNode.returnDownNode(new LexerNode(tokens.get(i).getStringAt(k)));
              } else {
                currentNode = matchingNode;
              }
            }
          } else {
            currentNode = currentNode.returnRightNode(new LexerNode(tokens.get(i).getStringAt(k)));
          }

          // stop mark is set on current node when the last sign in the token is reached
          if (k == tokens.get(i).length() - 1) {
            currentNode.stopMark = true;
          }
        } else if (i != 0) {
          currentNode = rootNode;
          if (!tokens.get(i).getStringAt(k).equals(currentNode.allowed)) {
            LexerNode matchingNode = findMatchingDownNode(currentNode,
                tokens.get(i).getStringAt(k));
            if (matchingNode == null) {
              currentNode = getLastVerticalNode(currentNode);
              currentNode = currentNode.returnDownNode(new LexerNode(tokens.get(i).getStringAt(k)));
            } else {
              currentNode = matchingNode;
            }
          }
          // allowed string matches, continue with k>0
        } else { // (i==0 && k==0)
          rootNode = new LexerNode(tokens.get(i).getStringAt(k));
          currentNode = rootNode;
        }
      }
    }
  }

  private LexerNode getLastVerticalNode(LexerNode currentNode) {
    if (!currentNode.hasDownNode()) {
      return currentNode;
    } else {
      return getLastVerticalNode(currentNode.getDownNode());
    }
  }

  private LexerNode findMatchingDownNode(LexerNode currentNode, String allowed) {
    if (currentNode.allowed.equals(allowed)) {
      return currentNode;
    } else if (currentNode.hasDownNode()) {
      return findMatchingDownNode(currentNode.getDownNode(), allowed);
    }
    return null;
  }

  public ArrayList<String> lexText(List<String> txt) throws IOException {

    ArrayList<String> tokens = new ArrayList<>();
    LexerNode currentNode = rootNode;
    StringBuilder token = new StringBuilder();

    int c;
    String s = "";

    for (int i=0; i < txt.size(); i++){

      s = txt.get(i);

      if (s.equals(" ") | s.equals("\n")){
        if (IGNORE_WHITESPACE){
          continue;
        }
      }

      if (s.equals(currentNode.allowed)){
        token.append(s);
      } else {
        LexerNode match = findMatchingDownNode(currentNode, s);
        if (match != null){
          currentNode = match;
          token.append(s);
        } else { //neither the right node nor any of the down nodes match
          if (currentNode.getSiblingParent() != null && currentNode.parentNode.hasStopMark()){
            tokens.add(token.toString());
            token.setLength(0);
            currentNode = rootNode;
            continue;
          } else {
            UnicodeString us = new UnicodeString(token.toString());
              for(int n=0; n < us.length(); n++ ){
                tokens.add(us.getStringAt(n));
              }
            tokens.add(s);
            token.setLength(0);
            currentNode = rootNode;
            continue;
          }
        }
      }
      if (!currentNode.hasRightNode()){
        tokens.add(token.toString());
        token.setLength(0);
        currentNode = rootNode;
      } else {
        currentNode = currentNode.rightNode;
      }
    }
    return tokens;
  }


  public static class LexerNode {

    private LexerNode rightNode, downNode, parentNode;
    String allowed;
    boolean stopMark;

    public LexerNode(String allowed, LexerNode rightNode, LexerNode downNode) {
      this.allowed = allowed;
      this.rightNode = rightNode;
      this.downNode = downNode;
    }

    public LexerNode(String allowed) {
      this(allowed, null, null);
    }

    public LexerNode() {
      this(null, null, null);
    }

    public LexerNode getRightNode() {
      return rightNode;
    }

    public LexerNode setRightNode(LexerNode rightNode) {
      this.rightNode = rightNode;
      rightNode.parentNode = this;
      return this;
    }

    public LexerNode returnRightNode(LexerNode rightNode) {
      this.rightNode = rightNode;
      rightNode.parentNode = this;
      return rightNode;
    }

    public LexerNode getDownNode() {
      return downNode;
    }

    public LexerNode setDownNode(LexerNode downNode) {
      this.downNode = downNode;
      downNode.parentNode = this;
      return this;
    }

    public LexerNode returnDownNode(LexerNode lexerNode) {
      downNode = lexerNode;
      downNode.parentNode = this;
      return downNode;
    }

    public boolean hasRightNode() {
      return rightNode != null;
    }

    public boolean hasDownNode() {
      return downNode != null;
    }

    public boolean hasParentNode() { return parentNode != null; }

    public LexerNode getParentNode(){
      return parentNode;
    }

    public LexerNode getSiblingParent(){
      if (hasParentNode() && parentNode.getRightNode() == this){
        return parentNode;
      }
      return null;
    }

    protected boolean hasStopMark() {
      return stopMark;
    }

    protected void setStopMark(boolean stopMark) {
      this.stopMark = stopMark;
    }


    @Override
    public String toString() {
      return allowed;
    }
  }

  public static void main(String[] args) throws IOException {

    Lexer lexer = new Lexer(Set.of("abc","def"));
    List<String> tokens = lexer.lexText(UTF8FileToStringArrayList.loadFileIntoStringList("/tmp/test.txt"));
    System.out.println(tokens);
  }
}
