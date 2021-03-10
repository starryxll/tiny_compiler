package com.mercy.compiler.FrontEnd;

import com.mercy.compiler.AST.*;
import com.mercy.compiler.Entity.*;
import com.mercy.compiler.Type.*;
import com.mercy.compiler.Utility.InternalError;
import com.mercy.compiler.Utility.SemanticError;

import java.util.List;

public class TypeChecker extends Visitor {
    static final Type boolType = new BoolType();
    static final Type integerType = new Integer();
    static final Type stringType = new StringType();


}
