package com.mercy.compiler.IR;

import com.mercy.compiler.INS.Operand.Operand;
import com.mercy.compiler.BackEnd.InstructionEmitter;

abstract public class IR {
    abstract public Operand accept(InstructionEmitter emitter);
}
