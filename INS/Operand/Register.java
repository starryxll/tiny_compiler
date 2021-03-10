package com.mercy.compiler.INS.Operand;

import java.util.HashSet;
import java.util.Set;

public class Register extends Operand {
    private String lowName;
    private boolean isCalleeSave;
    private String name;

    public Register(String name, String lowName) {
        this.name = name;
        this.lowName = lowName;
    }

    public String name() {
        return name;
    }
    public String lowName() {
        return lowName;
    }

    public boolean isCalleeSave() {
        return isCalleeSave;
    }
    public void setCalleeSave(boolean calleeSave) {
        isCalleeSave = calleeSave;
    }

    public boolean callerSave() {
        return !isCalleeSave;
    }
    public void setCallerSave(boolean callerSave) {
        isCalleeSave = !callerSave;
    }

    @Override
    public Set<Reference> getAllRef() {
        return new HashSet<>();
    }

    @Override
    public Operand replace(Operand from, Operand to) {
        return this;
    }

    @Override
    public boolean isRegister() {
        return true;
    }

    @Override
    public boolean isDirect() {
        return true;
    }

    @Override
    public String toNASM() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}