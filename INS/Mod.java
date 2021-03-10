package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Operand;

public class Mod extends Bin {
    public Mod(Operand left, Operand right) {
        super(left, right);
    }

    @Override
    public String name() {
        return "mod";
    }


    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }
}
