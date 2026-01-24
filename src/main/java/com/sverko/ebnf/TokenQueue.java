package com.sverko.ebnf;

import java.util.List;
import java.util.stream.Collectors;

public class TokenQueue {
    private List<String> tokens;

    public TokenQueue(List<String> tokens){
        this.tokens = tokens;
    }

    public int getFirstToken (){
        return 0;
    }

    public int getNextToken(int index){
        if(tokens.size()-1 > index && index >= 0){
            return ++index;
        } else {
            return ParseNode.END_OF_QUEUE;
        }
    }

    public boolean checkIndex(int index){
        return index > -1 && index < tokens.size();
    }

    public List<String> getTokens(){
      return tokens;
    }

    public String getToken(int index){
        return tokens.get(index);
    }

    public String getSubstring (int from, int to){
        return String.join("", tokens.subList(from, to));
    }

    public int size(){
      return tokens.size();
    }

    public String get(int index) {
      return tokens.get(index);
    }

    public boolean contains(String token){
        return tokens.contains(token);
    }

    public static TokenQueue ofList(List<String> tokens){
        return new TokenQueue(tokens);
    }

    public static TokenQueue ofList(String... tokens){
        return new TokenQueue(List.of(tokens));
    }

    @Override
    public boolean equals(Object obj) {
      return tokens.equals(((TokenQueue)obj).tokens);
    }
}
