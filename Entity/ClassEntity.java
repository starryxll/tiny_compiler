package com.mercy.compiler.Entity;

import com.mercy.compiler.AST.FunctionDefNode;
import com.mercy.compiler.AST.Location;
import com.mercy.compiler.AST.VariableDefNode;
import com.mercy.compiler.Type.ClassType;
import com.mercy.compiler.Type.Type;

import java.util.List;

public class ClassEntity extends Entity {
    private List<VariableDefNode> memberVars;
    private List<FunctionDefNode> memberFuncs;
    private Scope scope;
    private FunctionEntity constructor;
    private ClassType classType; // for add "this" pointer
    private int size;

    public ClassEntity (Location loc, String name, List<VariableDefNode> memberVars, List<FunctionDefNode> memberFuncs) {
        super(loc, new ClassType(name), name);
        this.memberVars = memberVars;
        this.memberFuncs = memberFuncs;
        this.scope = null;
        this.constructor = null;
        ((ClassType)this.type).setEntity(this);
    }

    public List<VariableDefNode> memberVars() {
        return memberVars;
    }

    public List<FunctionDefNode> memberFuncs() {
        return memberFuncs;
    }

    public Scope scope() {
        return scope;
    }
    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public FunctionEntity constructor() {
        return constructor;
    }

    public void setConstructor(FunctionEntity constructor) {
        this.constructor = constructor;
    }

    public void initOffset(int alignment) {
        this.size = scope.locateMember(alignment);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public String toString() {
        return "class entity :" + name;
    }
}
