package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Operand;

public class Sar extends Bin {
    public Sar(Operand left, Operand right) {
        super(left, right);
    }

    @Override
    public String name() {
        return "sar";
    }

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }
}
