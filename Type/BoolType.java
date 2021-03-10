package com.mercy.compiler.Type;


public class BoolType extends Type {
    static final int DEFAULT_SIZE = 8;

    @Override
    public boolean isBool() {
        return true;
    }

    @Override
    public boolean isCompatible(Type other) {
        return other.isBool();
    }

    @Override
    public boolean isHalfComparable() {
        return true;
    }

    @Override
    public int size() {
        return DEFAULT_SIZE;
    }

    @Override
    public String toString() {
        return "bool";
    }
}