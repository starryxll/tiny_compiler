package com.mercy.compiler.IR;

import com.mercy.compiler.BackEnd.InstructionEmitter;
import com.mercy.compiler.INS.Operand.Operand;

abstract public class Expr extends IR {
    @Override
    abstract public Operand accept(InstructionEmitter emitter);
}
