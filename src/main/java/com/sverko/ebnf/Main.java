package com.sverko.ebnf;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.StringTokenizer;

public class Main {

	public static void main(String[] args) {
		ParseNode p1 = new PositionNode();
		System.out.println(p1.getClass().getName());
		/*
		ParseNode curNode;
				
		EbnfParserGenerator parserGenerator = new EbnfParserGenerator("/tmp/ebnf.ebnf");
		Parser parser = parserGenerator.generate();
  		*/

   		//new SvgPrinter(parser.startNode);
		//parser.parse("/tmp/text.txt");
		
		
		
		//parser.setAcceptsWhitespace(false, parser.startNode);
		
		
		//ParseNodeEventListener textListener = ( e ->  System.out.println("Text gefunden: "+e.token));
		//parser.nodeMap.get("CONTENT").addEventListener(textListener);
		
		
		
		//ParseNodeEventListener integerListener = ( e -> System.out.println("Integer gefunden: "+e.token));
		//ParseNodeEventListener doubleListener = ( e -> System.out.println("Double gefunden: "+e.token));
		
		//parser.nodeMap.get("INTEGER").addEventListener(integerListener);
		//parser.nodeMap.get("DOUBLE").addEventListener(doubleListener);
		
		//parser.parse("/tmp/difficultForRegex.txt");
		/*
		parser.parse("/tmp/text.txt");
		System.out.println("Parsing done: "+Instant.now());

		 */
	}
}