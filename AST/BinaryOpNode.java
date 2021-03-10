package com.mercy.compiler.AST;

import com.mercy.compiler.FrontEnd.ASTVisitor;
import com.mercy.compiler.Type.Type;
import com.mercy.compiler.Utility.InternalError;

public class BinaryOpNode extends ExprNode {
    public enum BinaryOp {
        ADD, SUB, MUL, DIV, MOD,
        LSHIFT, RSHIFT, LT, GT, LE, GE, EQ, NE,
        BIT_AND, BIT_XOR, BIT_OR,
        LOGIC_AND, LOGIC_OR
    }

    private BinaryOp operator;
    private ExprNode left, right;
    private Type type;

    public BinaryOpNode(ExprNode left, BinaryOp op, ExprNode right) {
        super();
        this.operator = op;
        this.left = left;
        this.right = right;
    }

    public BinaryOpNode(Type t, ExprNode left, BinaryOp op, ExprNode right) {
        super();
        this.type = t;
        this.operator = op;
        this.left = left;
        this.right = right;
    }

    public BinaryOp operator() {
        return operator;
    }

    public void setOperator(BinaryOp operator) {
        this.operator = operator;
    }

    public ExprNode left() {
        return left;
    }

    public void setLeft(ExprNode left) {
        this.left = left;
    }

    public ExprNode right() {
        return right;
    }

    public void setRight(ExprNode right) {
        this.right = right;
    }

    public void setType(Type type) {
        if (this.type != null)
            throw new InternalError("BinaryOp#setType called twice");
        this.type = type;
    }

    @Override
    public Type type() {
        return (type != null) ? type : left.type();
    }

    @Override
    public Location location() {
        return left.location();
    }

    @Override
    public <S, E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
