package com.mercy.compiler.Type;

import com.mercy.compiler.Entity.ClassEntity;

public class ClassType extends Type{
    static final int DEFAULT_SIZE = 8;
    static public final String CONSTRUCTOR_NAME = "__constructor_";

    protected String name;
    protected ClassEntity entity;

    public void setEntity(ClassEntity entity) {
        this.entity = entity;
    }

    public String name() {
        return name;
    }

    public ClassType(String name) {
        this.name = name;
    }

    @Override
    public boolean isClass() {
        return true;
    }

    @Override
    public boolean isCompatible(Type other) {
        if (other.isNull())     return true;
        if (!other.isClass())   return false;
        return entity.equals(((ClassType)other).entity);
    }

    @Override
    public int size() {
        return DEFAULT_SIZE;
    }

    @Override
    public String toString() {
        return "class " + name;
    }
}
