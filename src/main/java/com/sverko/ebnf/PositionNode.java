package com.sverko.ebnf;

public class PositionNode extends ParseNode{

	PositionNode(){super();}
	PositionNode(String name){ super(name);}

	@Override
	public int callReceived(int token) {
		if (token == END_OF_QUEUE){
			return token;
		}
		int downNodeResult = downNode.callReceived(token);
		if (downNodeResult < 0) {
			return downNodeResult;
		}
		if(hasRightNode()){
			if (tokens.checkIndex(downNodeResult)) {
				int rightNodeResult = rightNode.callReceived(downNodeResult);
				if (rightNodeResult < 0 || rightNodeResult >= downNodeResult) {
					return rightNodeResult;
				} else {
					return ParseNode.WRONG_RETURN_VALUE;
				}
			}else{
				return ParseNode.END_OF_QUEUE; // this is an exception, but it is not thrown, as the parser definition process might not be finished yet.
			}
		}
		return downNodeResult;
	}
}