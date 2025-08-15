package com.sverko.ebnf;

import com.sverko.ebnf.tools.ParseNodeParserFactory;
import com.sverko.ebnf.tools.UTF8FileToStringArrayList;
import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class Parser {
	
	List<String> q = new ArrayList<>();
	Lexer lexer;
	int curPtr=-1; //before the first element in queue
	ParseNode startNode;
	Map<String,ParseNode> nodeMap;
	Map<String, Predicate<Integer>> specialSequences = new HashMap<>();

	public Parser(){

	}

	public Parser(ParseNode startNode, Map<String, ParseNode> namedNodes, Set<String> lexerTokens, boolean ignoreWhitespace) {
		this.startNode = startNode;
		this.nodeMap = namedNodes;
		this.lexer = new Lexer(lexerTokens,ignoreWhitespace);
	}

	public int parse(List<String> l, ParseNode startNode){
		q = l;
		this.startNode = startNode;
		return parse(startNode);
	}

	public int parse(String text) {
		return parse(lexer.lexText(text), startNode);
	}

	public int parse(Path textLocation) throws IOException {
		return parse(lexer.lexText(UTF8FileToStringArrayList.loadFileIntoStringList(textLocation)), startNode);
	}


	private int parse(ParseNode startNode){
		assignParserToEachNode(startNode);
		return startNode.callReceived(getNextToken(startNode.acceptsWhitespace));
	}

	public int getNextToken(boolean acceptsWhitespace){
		curPtr++;
		if(curPtr < q.size()){
			if(!acceptsWhitespace){
				int character = q.get(curPtr).codePointAt(0);
					if(Character.isWhitespace(character)) {
					
					curPtr = getNextToken(false);
				}
			}
		return curPtr;
		}
		curPtr =-1;
		return -1;
	}
	
	public void returnToken(String token){
		curPtr--;
	}
	public int returnTokens(int position, boolean acceptsWhitespace) {
		position--;
		if(!acceptsWhitespace){
			int character = q.get(position).codePointAt(0);
				if(Character.isWhitespace(character)) {
					returnTokens(position--,acceptsWhitespace);
				}
		}
		curPtr = position;
		return curPtr;
	}

	public TokenQueue getTokenQueue(){
		return new TokenQueue(q);
	}

	public void assignParserToEachNode(ParseNode startNode){
		ParseNodeParserFactory.assign(startNode, this);
	}

	public void assignNodeEventListener(String nodeName, ParseNodeEventListener listener){
		nodeMap.get(nodeName).addEventListener(listener);
	}

	public void assignNodeEventListeners(ParseNodeEventListener listener, String... nodeNames){
		for(String nodeName : nodeNames) {
			assignNodeEventListener(nodeName,listener);
		}
	}

	public void setAcceptsWhitespace(boolean acceptsWhitespace, ParseNode parentNode){
		
		parentNode.acceptsWhitespace = acceptsWhitespace;
		if(parentNode.hasDownNode()){
			setAcceptsWhitespace(acceptsWhitespace, parentNode.downNode);
		}
		if(parentNode.hasRightNode()){
			setAcceptsWhitespace(acceptsWhitespace, parentNode.rightNode);
		}
	}
	public void addSpecialSequence (String key, Predicate<Integer> value) {
		specialSequences.put(key, value);
	}

	public Predicate<Integer> getSpecialSequence (String key){
		return specialSequences.get(key);
	}

	protected void addDefaultSpecialSequences(){
		addSpecialSequence("?WHITESPACE?",Character::isWhitespace);
		addSpecialSequence("?BMP?",Character::isBmpCodePoint);
		addSpecialSequence("?DIGIT?",Character::isDigit);
		addSpecialSequence("?LETTER?",Character::isLetter);
		addSpecialSequence("?LOWERCASE?",Character::isLowerCase);
		addSpecialSequence("?UPPERCASE?",Character::isUpperCase);
		addSpecialSequence("?BASIC_LATIN?", (i) -> Character.UnicodeBlock.of(i) == UnicodeBlock.BASIC_LATIN); //aka ASCII
		addSpecialSequence("?BASIC_LATIN_LETTER?", getSpecialSequence("?BASIC_LATIN?").and(Character::isLetter));
		addSpecialSequence("?CYRILLIC?", (i) -> Character.UnicodeBlock.of(i) == UnicodeBlock.CYRILLIC);
		addSpecialSequence("?EMOTICONS?", (i) ->  Character.UnicodeBlock.of(i) == UnicodeBlock.EMOTICONS);
	}

}