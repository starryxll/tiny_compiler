package com.mercy.compiler.AST;

import com.mercy.compiler.FrontEnd.ASTVisitor;

public class WhileNode extends StmtNode{
    private StmtNode body;
    private ExprNode cond;

    public WhileNode(Location loc, ExprNode cond, StmtNode body) {
        super(loc);
        this.cond = cond;
        this.body = BlockNode.wrapBlock(body);
    }

    public StmtNode body() {
        return body;
    }

    public ExprNode cond() {
        return cond;
    }

    @Override
    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }

}
