package com.mercy.compiler.Entity;

import com.mercy.compiler.AST.ExprNode;
import com.mercy.compiler.AST.Location;
import com.mercy.compiler.Type.Type;

public class VariableEntity extends Entity {
    private ExprNode initializer;

    public VariableEntity(Location loc, Type type, String name, ExprNode init) {
        super(loc, type, name);
        initializer = init;
    }

    public VariableEntity copy() {
        VariableEntity a = new VariableEntity(location(), type(), name(), initializer());
        a.setOutputIrrelevant(outputIrrelevant());
        return a;
    }

    public ExprNode initializer() {
        return initializer;
    }

    @Override
    public String toString() {
        return "variable entity : " + name;
    }
}


