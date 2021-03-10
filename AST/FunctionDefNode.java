package com.mercy.compiler.AST;

import com.mercy.compiler.FrontEnd.ASTVisitor;
import com.mercy.compiler.Entity.FunctionEntity;


public class FunctionDefNode extends DefinitionNode {

    private FunctionEntity entity;

    public FunctionDefNode(FunctionEntity entity) {
        super(entity.location(), entity.name());
        this.entity = entity;
    }

    public FunctionEntity entity() {
        return entity;
    }


    @Override
    public <S, E> S accept(ASTVisitor<S, E> visitor) {
        return (S) visitor.visit(this);
    }
}
