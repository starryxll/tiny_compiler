package com.mercy.compiler.AST;

abstract public class Node {
    public Node() {
    }

    protected boolean isOutputIrrelevant = false;

    public boolean isOutputIrrelevant() {
        return isOutputIrrelevant;
    }

    public void setOutputIrrelevant(boolean outputIrrelevant) {
        isOutputIrrelevant = outputIrrelevant;
    }

    abstract public Location location();
}


