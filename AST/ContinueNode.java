package com.mercy.compiler.AST;

import com.mercy.compiler.FrontEnd.ASTVisitor;

public class ContinueNode extends StmtNode {
    public ContinueNode(Location loc) {
        super(loc);
    }

    @Override
    public <S, E> S accept(ASTVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
