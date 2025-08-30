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
    // remember start token index for this node
    this.frmPtr = token;
    int receivedResult = this.downNode.callReceived(token);
    if (receivedResult > 0) {
      this.toPtr = receivedResult;
      this.resultString = this.tokens.getSubstring(token, receivedResult);
      this.fireParseNodeEvent();
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
