package com.mercy.compiler.AST;

import com.mercy.compiler.FrontEnd.ASTVisitor;
import com.mercy.compiler.Type.Type;

import java.util.List;

public class CreatorNode extends ExprNode {
    private Location location;
    private Type type;
    private List<ExprNode> exprs;
    private int total;

    public CreatorNode(Location loc, Type type, List<ExprNode> exprs, int total) {
        this.location = loc;
        this.type = type;
        this.exprs = exprs;
        this.total = total;
    }

    @Override
    public Type type() {
        return type;
    }

    public List<ExprNode> exprs() {
        return exprs;
    }

    public void setExprs(List<ExprNode> exprs) {
        this.exprs = exprs;
    }

    public int total() {
        return total;
    }

    @Override
    public Location location() {
        return location;
    }

    @Override
    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
