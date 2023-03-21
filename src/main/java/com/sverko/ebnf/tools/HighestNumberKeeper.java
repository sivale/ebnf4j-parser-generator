package com.sverko.ebnf.tools;

public class HighestNumberKeeper <T extends Number> {
    T val;

    public static HighestNumberKeeper<Byte> getByteInstance(){
        return new HighestNumberKeeper<>(Byte.MIN_VALUE);
    }
    public static HighestNumberKeeper<Short> getShortInstance(){
        return new HighestNumberKeeper<>(Short.MIN_VALUE);
    }
    public static HighestNumberKeeper<Integer> getIntegerInstance(){
        return new HighestNumberKeeper<>(Integer.MIN_VALUE);
    }
    public static HighestNumberKeeper<Long> getLongInstance(){
        return new HighestNumberKeeper<>(Long.MIN_VALUE);
    }
    public static HighestNumberKeeper<Float> getFloatInstance(){
        return new HighestNumberKeeper<>(Float.NEGATIVE_INFINITY);
    }
    public static HighestNumberKeeper<Double> getDoubleInstance(){
        return new HighestNumberKeeper<>(Double.NEGATIVE_INFINITY);
    }

    private HighestNumberKeeper(){}

    public HighestNumberKeeper( T val){
        this.val = val;
    }

    public T getVal(){
        return val;
    }

    public void setVal(T val){
        if(this.val.doubleValue() < val.doubleValue()){
            this.val = val;
        }
    }

    public static void main(String[] args) {
        HighestNumberKeeper<Double> hnk = HighestNumberKeeper.getDoubleInstance();
        hnk.setVal(-200_000D);
        System.out.println(hnk.getVal());

    }


}
