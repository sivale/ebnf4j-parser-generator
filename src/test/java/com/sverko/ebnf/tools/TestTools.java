package com.sverko.ebnf.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTools {
    @Test
    public void testIntegerKeeper (){
        HighestNumberKeeper<Integer> keeper = HighestNumberKeeper.getIntegerInstance();
        keeper.setVal(22);
        keeper.setVal(11);
        assertEquals(keeper.getVal(), 22);
    }
    @Test
    public void testDoubleKeeper (){
        HighestNumberKeeper<Double> keeper = HighestNumberKeeper.getDoubleInstance();
        keeper.setVal(22.0);
        keeper.setVal(11.0);
        assertEquals(keeper.getVal(), 22);
    }


}
