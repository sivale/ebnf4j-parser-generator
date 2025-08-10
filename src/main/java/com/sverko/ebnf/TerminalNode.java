package com.sverko.ebnf;
import java.util.function.Function;

public class TerminalNode extends ParseNode {

	private final Function<String, Boolean> compareFunction;
	private final String tag;
	public TerminalNode(Function<String, Boolean> compareFunction, String tag) {
		this.compareFunction = compareFunction;
		this.tag = tag;
	}

	public TerminalNode(String name, Function<String, Boolean> compareFunction, String tag) {
		this.name = name;
		this.compareFunction = compareFunction;
		this.tag = tag;
	}

	public String getTag() {
		// Fallbacks, damit immer etwas Sinnvolles angezeigt wird
		if (tag != null && !tag.isEmpty()) return tag;
		if (name != null && !name.isEmpty()) return name;
		return "<terminal>";
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