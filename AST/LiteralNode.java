package com.mercy.compiler.AST;

import com.mercy.compiler.Type.Type;

abstract public class LiteralNode extends ExprNode {
    protected Location location;
    protected Type type;

    public LiteralNode(Location loc, Type type) {
        super();
        this.location = loc;
        this.type = type;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public Type type() {
        return type;
    }


    @Override
    public Location location() {
        return location;
    }
}
