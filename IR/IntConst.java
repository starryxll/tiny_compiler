package com.mercy.compiler.IR;

import com.mercy.compiler.BackEnd.InstructionEmitter;
import com.mercy.compiler.INS.Operand.Operand;

public class IntConst extends Expr {
    private int value;

    public IntConst(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    @Override
    public Operand accept(InstructionEmitter emitter) {
        return emitter.visit(this);
    }
}
