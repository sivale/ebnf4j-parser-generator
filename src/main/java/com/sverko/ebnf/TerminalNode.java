package com.sverko.ebnf;
import java.util.function.Function;

public class TerminalNode extends ParseNode {

	private final Function<String, Boolean> compareFunction;

	public TerminalNode(Function<String, Boolean> compareFunction) {
		this.compareFunction = compareFunction;
	}

	public TerminalNode(String name, Function<String, Boolean> compareFunction) {
		this.name = name;
		this.compareFunction = compareFunction;
	}

	@Override
	public int callReceived(int curPtr) {
		if (tokens.checkIndex(curPtr)) {
			if (compareFunction.apply(tokens.getToken(curPtr))) {
				return curPtr + 1;
			}
			return NOT_FOUND;
		}
		return END_OF_QUEUE;
	}
}