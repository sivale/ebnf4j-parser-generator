package com.sverko.ebnf;

public class ParseNodeEvent {
	public ParseNode parseNode;
	public String resultString;
  public int from;
  public int to;
	
	public ParseNodeEvent(ParseNode parseNode, String token){
		this.parseNode = parseNode;
		this.resultString = token;
    this.from = parseNode.frmPtr;
    this.to = parseNode.toPtr;
	}
	
	@Override
	public String toString() {
		return this.resultString;
	}
	
}
