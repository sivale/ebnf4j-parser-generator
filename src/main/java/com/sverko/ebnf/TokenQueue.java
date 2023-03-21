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

    public String getToken(int index){
        return tokens.get(index);
    }

    public String getSubstring (int from, int to){
        return String.join("", tokens.subList(from, to));
    }
}
