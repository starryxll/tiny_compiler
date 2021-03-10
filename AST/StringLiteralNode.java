package com.mercy.compiler.AST;

import com.mercy.compiler.Entity.StringConstantEntity;
import com.mercy.compiler.FrontEnd.ASTVisitor;
import com.mercy.compiler.Type.StringType;
import com.mercy.compiler.Type.Type;


public class StringLiteralNode extends LiteralNode {
    private String value;
    private StringConstantEntity entity;

    public StringLiteralNode(Location loc, String type) {
        super(loc, new StringType());
        this.value = value;
    }

    public void setEntity(StringConstantEntity entity) {
        this.entity = entity;
    }

    public String value() {
        return value;
    }

    public StringConstantEntity entity() {
        return entity;
    }

    @Override
    public <S, E> E accept(ASTVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}