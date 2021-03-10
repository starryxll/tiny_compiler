package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Operand;
import com.mercy.compiler.INS.Operand.Reference;

public class Neg extends Instruction {
    private Operand operand;

    public Neg(Operand operand) {
        this.operand = operand;
    }

    public Operand operand() {
        return operand;
    }

    @Override
    public void replaceUse(Reference from, Reference to) {
        if (operand instanceof Reference) {
            ;
        } else {
            operand = operand.replace(from, to);
        }
    }

    @Override
    public void replaceDef(Reference from, Reference to) {
        if (operand instanceof Reference)
            operand = operand.replace(from, to);
    }

    @Override
    public void replaceAll(Reference from, Reference to) {
        operand = operand.replace(from, to);
    }


    @Override
    public void calcDefAndUse() {
        if (operand instanceof Reference) {
            def.addAll(operand().getAllRef());
        }
        use.addAll(operand().getAllRef());
        allref.addAll(use);
        allref.addAll(def);
    }

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }

    @Override
    public String toString() {
        return "neg " + operand;
    }
}
