package com.sverko.ebnf;

public class NonTerminalNode extends ParseNode {
	String resultString;
	
	public NonTerminalNode(String name){

		super(name);
	}
	public NonTerminalNode() {
		
	}
	
	@Override
	public int callReceived(int token) {

		int receivedResult = downNode.callReceived(token);
		if (receivedResult > 0){
			resultString = tokens.getSubstring(token, receivedResult);
			fireParseNodeEvent();
		}
		return receivedResult;
	}

	String getResultString (){
		return resultString;
	}

	void fireParseNodeEvent(){
 		for(ParseNodeEventListener l : listeners){
			l.parseNodeEventOccurred(new ParseNodeEvent(this, resultString));
		}
	}

	void fireParseNodeEvent(String resultString){
		for(ParseNodeEventListener l : listeners){
			l.parseNodeEventOccurred(new ParseNodeEvent(this, resultString));
		}
	}
}
