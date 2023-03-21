package com.sverko.ebnf;

public class OrNode extends ParseNode {

	OrNode () {super();}
  OrNode (String name) {super(name);}

	@Override
	public int callReceived(int token) {
		int downNodeResult = downNode.callReceived(token);
		if(downNodeResult <= token){
			if(hasRightNode()){
				return rightNode.callReceived(token);
			}
		}
		return downNodeResult;
	}
}
