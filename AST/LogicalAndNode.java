package com.mercy.compiler.AST;

import com.mercy.compiler.FrontEnd.ASTVisitor;

public class LogicalAndNode extends BinaryOpNode {
    public LogicalAndNode(ExprNode left, ExprNode right) {
        super(left, BinaryOp.LOGIC_AND, right);
    }

    @Override
    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
