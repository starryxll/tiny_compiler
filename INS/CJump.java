package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Immediate;
import com.mercy.compiler.INS.Operand.Operand;
import com.mercy.compiler.INS.Operand.Reference;
import com.mercy.compiler.Utility.InternalError;

import java.util.HashSet;
import java.util.Set;

public class CJump extends Instruction {
    private Operand cond;
    private Label trueLabel, falseLabel;
    private Label fallThrough;
    private Operand left, right;
    private Set<Reference> bringOut;

    public enum Type {
        EQ, NE, GT, GE, LT, LE, BOOL
    }

    Type type;

    public CJump(Operand cond, Label trueLabel, Label falseLabel) {
        this.type = Type.BOOL;
        this.cond = cond;
        if (cond instanceof Immediate)
            throw new InternalError("niha");
        this.trueLabel = trueLabel;
        this.falseLabel = falseLabel;
    }

    public CJump(Operand left, Operand right, Type type, Label trueLabel, Label falseLabel) {
        this.left = left; this.right = right;
        this.type = type;
        this.trueLabel = trueLabel;
        this.falseLabel = falseLabel;
    }

    public void setTrueLabel(Label trueLabel) {
        this.trueLabel = trueLabel;
    }

    public void setFalseLabel(Label falseLabel) {
        this.falseLabel = falseLabel;
    }

    public Set<Reference> bringOut() {
        return bringOut;
    }

    public void setBringOut(Set<Reference> bringOut) {
        this.bringOut = bringOut;
    }

    @Override
    public void replaceUse(Reference from, Reference to) {
        if (bringOut != null && bringOut.contains(from)) {
            Set<Reference> newBringOut = new HashSet<>();
            for (Reference reference : bringOut) {
                newBringOut.add((Reference) reference.replace(from, to));
            }
            bringOut = newBringOut;
        }

        if (type == Type.BOOL) {
            cond = cond.replace(from, to);
        } else {
            left = left.replace(from, to);
            right = right.replace(from, to);
        }
    }

    @Override
    public void replaceDef(Reference from, Reference to) {
    }

    @Override
    public void calcDefAndUse() {
        if (type == Type.BOOL)
            use.addAll(cond.getAllRef());
        else {
            use.addAll(left.getAllRef());
            use.addAll(right.getAllRef());
        }
        if (bringOut != null)
            use.addAll(bringOut);
        allref.addAll(use);
    }

    // getter
    public Operand cond() {
        return cond;
    }

    public Label trueLabel() {
        return trueLabel;
    }
    public Label falseLabel() {
        return falseLabel;
    }

    public Label fallThrough() {
        return fallThrough;
    }
    public void setFallThrough(Label fallThrough) {
        this.fallThrough = fallThrough;
    }

    public Type type() {
        return type;
    }
    public Operand left() {
        return left;
    }
    public Operand right() {
        return right;
    }

    public void setLeft(Operand left) {
        this.left = left;
    }

    public String name() {
        switch (type) {
            case EQ: return "je";
            case NE: return "jne";
            case GT: return "jg";
            case GE: return "jge";
            case LT: return "jl";
            case LE: return "jle";
            default:
                throw new InternalError("invalid compare operator");
        }
    }

    // a op b -> !(a op b)
    static public String getNotName(String raw) {
        switch (raw) {
            case "je":  return "jne";
            case "jne": return "je";
            case "jg":  return "jle";
            case "jge": return "jl";
            case "jl":  return "jge";
            case "jle": return "jg";
            default:
                throw new InternalError("invalid compare operator");
        }
    }

    // reflect a op b -> b op a
    static public String getReflect(String raw) {
        switch (raw) {
            case "je" : return "je";
            case "jne": return "jne";
            case "jg":  return "jlt";
            case "jge": return "jle";
            case "jl":  return "jgt";
            case "jle": return "jge";
            default:
                throw new InternalError("invalid compare operator");
        }
    }

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }

    @Override
    public String toString() {
        if (type == Type.BOOL)
            return "Cjump " + cond + ", " + trueLabel + ", " + falseLabel;
        else {
            return name() + " " + left() + " " + right() + ", " + trueLabel + ", " + falseLabel;
        }
    }
}
