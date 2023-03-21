package com.sverko.ebnf;

public class ParseNodeEvent {
	public ParseNode parseNode;
	public String resultString;
	
	public ParseNodeEvent(ParseNode parseNode, String token){
		this.parseNode = parseNode;
		this.resultString = token;
	}
	
	@Override
	public String toString() {
		return this.resultString;
	}
	
}
