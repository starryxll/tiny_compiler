package com.mercy.compiler.Entity;


public class MemberEntity extends VariableEntity {
    public MemberEntity(VariableEntity entity) {
        super(entity.location(), entity.type(), entity.name(), entity.initializer());
    }
}
