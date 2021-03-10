package com.mercy.compiler.Type;

public class IntegerType extends Type{
    static final int DEFAULT_SIZE = 8;

    @Override
    public boolean isInteger() {
        return true;
    }

    @Override
    public boolean isCompatible(Type other) {
        return other.isInteger();
    }

    @Override
    public int size() {
        return DEFAULT_SIZE ;
    }

    @Override
    public String toString() {
        return "int";
    }

    @Override
    public boolean isHalfComparable() {
        return true;
    }

    @Override
    public boolean isFullComparable() {
        return true;
    }
}
