package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Address;
import com.mercy.compiler.INS.Operand.Reference;


public class Lea extends Instruction{
    private Reference dest;
    private Address addr;

    public Lea (Reference dest, Address addr) {
        this.dest = dest;
        this.addr = addr;
    }

    public Reference dest() {
        return dest;
    }

    public Address addr() {
        return addr;
    }


    @Override
    public void replaceUse(Reference from, Reference to) {
        addr = addr.replace(from, to);
        if (dest != from)
            dest = (Reference)dest.replace(from, to);
    }

    @Override
    public void replaceDef(Reference from, Reference to) {
        dest = (Reference) dest.replace(from, to);
    }

    @Override
    public void calcDefAndUse() {
        if (dest instanceof Reference)
            def.addAll(dest.getAllRef());
        use.addAll(addr.getAllRef());
        allref.addAll(use);
        allref.addAll(def);
    }

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }
}
