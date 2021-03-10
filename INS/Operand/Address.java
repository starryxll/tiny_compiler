package com.mercy.compiler.INS.Operand;

import com.mercy.compiler.Utility.InternalError;

import java.util.HashSet;
import java.util.Set;


public class Address extends Operand {
    private Operand base = null, index = null;  // however, these two member can only be register/reference, cannot be address!
    private int mul = 1, add = 0;

    private boolean showSize = true;

    private Operand baseNasm, indexNasm;

    public Address(Operand base) {
        this.base = base;

        if (base instanceof Address)
            throw new InternalError("invalid address : nested address");
    }

    public Address(Operand base, Operand index, int mul, int add) {
        this.base = base;
        this.index = index;
        this.mul = mul;
        this.add = add;

        if (base instanceof Address || index instanceof  Address)
            throw new InternalError("invalid address : nested address");
    }

    @Override
    public Set<Reference> getAllRef() {
        Set<Reference> ret = new HashSet<>();
        if (base != null)
            ret.addAll(base.getAllRef());
        if (index != null) {
            ret.addAll(index.getAllRef());
        }
        return ret;
    }

    @Override
    public Address replace(Operand from, Operand to) {
        if (base != null)
            base = base.replace(from, to);
        if (index != null)
            index = index.replace(from, to);
        return this;
    }

    /*
     * getter and setter
     */
    public Operand base() {
        return base;
    }
    public Operand index() {
        return index;
    }
    public int mul() {
        return mul;
    }
    public int add() {
        return add;
    }

    public void setBase(Operand base) {
        this.base = base;
    }
    public void setIndex(Operand index) {
        this.index = index;
    }
    public void setMul(int mul) {
        this.mul = mul;
    }
    public void setAdd(int add) {
        this.add = add;
    }

    public Operand baseNasm() {
        return baseNasm != null ? baseNasm : base;
    }
    public void setBaseNasm(Operand baseNasm) {
        this.baseNasm = baseNasm;
    }

    public Operand indexNasm() {
        return indexNasm != null ? indexNasm : index;
    }
    public void setIndexNasm(Operand indexNasm) {
        this.indexNasm = indexNasm;
    }

    public void setShowSize(boolean showSize) {
        this.showSize = showSize;
    }

    public boolean baseOnly() {
        return base != null && index == null && mul == 1 && add == 0;
    }

    @Override
    public boolean isDirect() {
        if (base == null) {
            return index().isRegister();
        } if (index == null) {
            return base.isRegister();
        } else
            return index.isRegister() && base.isRegister();
    }

    @Override
    public boolean isAddress() {
        return true;
    }

    @Override
    public String toNASM() {
        String ret = showSize ? "qword" + " [" : "[";
        String gap = "";
        if (base != null) {
            ret += gap + baseNasm().toNASM();
            gap = " + ";
        }
        if (index != null) {
            ret += gap + indexNasm().toNASM();
            gap = " + ";
            if (mul != 1) {
                ret += " * " + mul;
            }
        }
        if (add != 0) {
            ret += gap + add;
        }

        return ret + "]";
    }

    @Override
    public int hashCode() {
        int hash = 0x93;
        if (base != null)
            hash *= base.hashCode();
        if (index!= null)
            hash += index.hashCode();
        hash = hash * mul + add;
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Address) {
            return base == ((Address) o).base() && index == ((Address) o).index()
                    && mul == ((Address) o).mul() && add == ((Address) o).add();
        }
        return false;
    }

    @Override
    public String toString() {
        String str = "";

        String gap = "";
        if (base != null) {
            str += gap + base;
            gap = " + ";
        }
        if (index != null) {
            str += gap + index;
            gap = " + ";
            if (mul != 1) {
                str += " * " + mul;
            }
        }
        if (add != 0) {
            str += gap + add;
        }
        return "[" + str + "]";
    }
}