package com.sverko.ebnf.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class UTF8FileToStringArrayList {

    public static List<String> loadFileIntoStringList(String filename) throws IOException {
        List <String> stringList = new ArrayList<>(100);
        FileInputStream fs = new FileInputStream(filename);
        InputStreamReader isr = new InputStreamReader(fs, StandardCharsets.UTF_8);
        // here happens the conversion from UTF8 to UTF16
        Reader bfs = new BufferedReader(isr);
        int c;
        String s="";
        while ((c = bfs.read()) > -1){
            if (Character.isHighSurrogate((char)c)) {
                s += Character.toString(c);
                continue;
            } else if (Character.isLowSurrogate((char)c)) {
                s += Character.toString(c);
            } else {
                s = Character.toString(c);
            }
          stringList.add(s);
        }
        return stringList;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(loadFileIntoStringList("/tmp/test.txt"));
    }
}
