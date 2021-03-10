package com.mercy.compiler.IR;

import com.mercy.compiler.AST.ExprNode;
import com.mercy.compiler.BackEnd.InstructionEmitter;
import com.mercy.compiler.Entity.Entity;
import com.mercy.compiler.INS.Operand.Operand;


public class Addr extends Expr {
    private Entity entity;

    public Addr(Entity entity) {
        super();
        this.entity = entity;
    }

    public Entity entity() {
        return entity;
    }
    @Override
    public Operand accept(InstructionEmitter emitter) {
        return emitter.visit(this);
    }

}

