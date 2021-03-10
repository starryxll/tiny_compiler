package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Operand;

public class Div extends Bin{
    public Div(Operand left, Operand right) {
        super(left, right);
    }

    @Override
    public String name() {
        return "div";
    }

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }
}
