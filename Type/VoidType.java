package com.mercy.compiler.Type;

public class VoidType extends Type{
    public VoidType() {
    }
    @Override
    public boolean isCompatible(Type other) {
        return other.isVoid();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public String toString() {
        return "void";
    }

    @Override
    public boolean isVoid() {
        return true;
    }
}
