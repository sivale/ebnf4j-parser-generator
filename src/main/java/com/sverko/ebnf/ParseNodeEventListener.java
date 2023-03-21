package com.sverko.ebnf;

import com.sverko.ebnf.ParseNodeEvent;

public interface ParseNodeEventListener {

	void parseNodeEventOccurred(ParseNodeEvent e);
	
}
