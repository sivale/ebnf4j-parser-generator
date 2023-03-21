package com.sverko.ebnf;


import java.util.ArrayList;
import java.util.List;


public abstract class ParseNode implements CalledNode{

	public static final int WRONG_RETURN_VALUE = -100;
	public static final int NOT_FOUND= -1;
	public static final int END_OF_QUEUE = -2;
	public String name;
	public ParseNode parent;
	public TokenQueue tokens;
	int frmPtr, toPtr;

	List<ParseNodeEventListener> listeners = new ArrayList<>();

	public ParseNode rightNode=null, downNode=null;
	
	protected boolean isAcceptsWhitespace() {
		return acceptsWhitespace;
	}
	protected ParseNode setAcceptsWhitespace(boolean acceptsWhitespace) {
		this.acceptsWhitespace = acceptsWhitespace;
		return this;
	}
	protected String getName() {
		return name;
	}
	protected boolean acceptsWhitespace=false;
	
	public ParseNode(String name){
		this.name = name;
	}
	public ParseNode(){
		this("name not set");
	}

	public ParseNode returnRightNode(ParseNode node){
		this.rightNode = node;
		this.rightNode.tokens = tokens;
		this.rightNode.parent = this;
		return node;
	}
	
	public ParseNode returnDownNode(ParseNode node){
		this.downNode = node;
		this.downNode.tokens = tokens;
		this.downNode.parent = this;
		return node;
	}
	
	public ParseNode setDownNode (ParseNode node){
		this.downNode = node;
		this.downNode.tokens = tokens;
		this.downNode.parent = this;
		return this;
	}
	
	public ParseNode setRightNode (ParseNode node){
		this.rightNode = node;
		this.rightNode.tokens = tokens;
		this.rightNode.parent = this;
		return this;
	}
	
	public ParseNode linkDownNode(ParseNode downNode) {
		this.downNode = downNode;
		return this;
	}
	
	public ParseNode linkRightNode(ParseNode rightNode) {
		this.rightNode = rightNode;
		return this;
	}
	
	public ParseNode swapNode(ParseNode node) {
		return node;
	}
	
	public boolean hasRightNode(){
		return (rightNode != null);
	}

	public boolean hasDownNode(){
		return (downNode != null);
	}
	
	public boolean hasParent(){
		return (parent != null);
	}

	public boolean isAncestor(ParseNode ancestorNode){
		if (this.parent == ancestorNode){
			return true;
		}
		if (this.parent == null){
			return false;
		}
		return this.parent.isAncestor(ancestorNode);
	}
	
	public ParseNode setName(String name){
		this.name = name;
		return this;
	}
	
	ParseNode getRightNode() {
		return rightNode;
	}
	
	ParseNode getDownNode() {
		return downNode;
	}
	
	
	@Override
	public String toString() {
		return (name != null)? name:"no name";
	}
	
	public void addEventListener(ParseNodeEventListener l){
		listeners.add(l);
	}
	
	void removeEventListener(ParseNodeEventListener l){
		listeners.remove(l);
	}

	@Override
	public boolean equals(Object o) {
		return o != null && getClass() == o.getClass();
	}

	public abstract int callReceived (int curPtr);
}