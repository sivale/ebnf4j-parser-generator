package com.sverko.ebnf;

import static java.lang.Character.UnicodeBlock.BASIC_LATIN;

import java.lang.Character.UnicodeBlock;

public class SimpleTest {
	static String a, b, c;
	public static void main(String[] args) {
		System.out.println(Character.UnicodeBlock.of('a') == BASIC_LATIN);
	}
}