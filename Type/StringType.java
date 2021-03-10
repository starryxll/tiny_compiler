package com.mercy.compiler.Type;

import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.Entity.Scope;
import com.mercy.compiler.Utility.LibFunction;

import static com.mercy.compiler.Utility.LibFunction.LIB_PREFIX;

public class StringType extends Type {
    static final int DEFAULT_SIZE = 8;
    static final public String STRING_CONSTANT_PREFIX = "__str_constant_";
    static public FunctionEntity operatorADD, operatorEQ,  operatorNE, operatorLT, operatorGT, operatorGE, operatorLE;
    static private Scope scope;

    static public void initializeBuiltinFunction() {
        // operators
        operatorADD = new LibFunction(stringType, LIB_PREFIX + "str_operator_ADD", new Type[]{stringType, stringType}).getEntity();
        operatorEQ  = new LibFunction(stringType, LIB_PREFIX + "str_operator_EQ", new Type[]{stringType, stringType}).getEntity();
        operatorNE  = new LibFunction(stringType, LIB_PREFIX + "str_operator_NE", new Type[]{stringType, stringType}).getEntity();
        operatorLT  = new LibFunction(stringType, LIB_PREFIX + "str_operator_LT", new Type[]{stringType, stringType}).getEntity();
        operatorGT  = new LibFunction(stringType, LIB_PREFIX + "str_operator_GT", new Type[]{stringType, stringType}).getEntity();
        operatorLE  = new LibFunction(stringType, LIB_PREFIX + "str_operator_LE", new Type[]{stringType, stringType}).getEntity();
        operatorGE  = new LibFunction(stringType, LIB_PREFIX + "str_operator_GE", new Type[]{stringType, stringType}).getEntity();

        scope = new Scope(true);
        scope.insert(new LibFunction(integerType, "length", LIB_PREFIX + "str_length", new Type[]{stringType}).getEntity());
        scope.insert(new LibFunction(stringType, "substring", LIB_PREFIX + "str_substring",new Type[]{stringType, integerType, integerType}).getEntity());
        scope.insert(new LibFunction(integerType, "parseInt", LIB_PREFIX + "str_parseInt",new Type[]{stringType}).getEntity());
        scope.insert(new LibFunction(integerType, "ord", LIB_PREFIX + "str_ord",new Type[]{stringType, integerType}).getEntity());
        scope.insert(operatorADD);
        scope.insert(operatorLT);
        scope.insert(operatorEQ);
    }

    static public Scope scope() {
        return scope;
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public boolean isCompatible(Type other) {
        return other.isString();
    }

    @Override
    public boolean isFullComparable() {
        return true;
    }

    @Override
    public boolean isHalfComparable() {
        return true;
    }

    @Override
    public int size() {
        return DEFAULT_SIZE;
    }

    @Override
    public String toString() {
        return "string";
    }

}
