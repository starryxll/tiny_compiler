package com.mercy.compiler.IR;

import com.mercy.compiler.BackEnd.InstructionEmitter;
import com.mercy.compiler.INS.Operand.Operand;

public class Assign extends IR {
    Expr left, right;

    public Assign(Expr left, Expr right) {
        this.left = left;
        this.right = right;
    }

    public Expr left() {
        return left;
    }
    public Expr right() {
        return right;
    }

    @Override
    public Operand accept(InstructionEmitter emitter) {
        return emitter.visit(this);
    }
}
