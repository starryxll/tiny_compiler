package com.mercy.compiler.Entity;

import com.mercy.compiler.AST.Location;
import com.mercy.compiler.INS.Operand.Reference;
import com.mercy.compiler.Type.Type;

public class ParameterEntity extends Entity {
    private Reference source;

    public ParameterEntity(Location loc, Type type, String name) {
        super(loc, type, name);
    }

    public Reference source() {
        return source;
    }
    public void setSource(Reference source) {
        this.source = source;
    }
}
