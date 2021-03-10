package com.mercy.compiler.Entity;

import com.mercy.compiler.AST.ExprNode;
import com.mercy.compiler.AST.Location;
import com.mercy.compiler.Type.StringType;
import com.mercy.compiler.Type.Type;

public class StringConstantEntity extends Entity {
    private ExprNode expr;
    private String value;
    private String asmName;
    public static final String STRING_CONSTANT_ASM_LABEL_PREFIX =  "__STR_CONST_";

    public StringConstantEntity(Location loc, Type type, String name, ExprNode expr) {
        super(loc, type, StringType.STRING_CONSTANT_PREFIX + name);
        this.expr = expr;
        StringBuffer sb = new StringBuffer();
        name = name.replaceAll("\\\\" + "\"" , "\"");
        name = name.replaceAll("\\\\" + "n" , "\n");
        name = name.replaceAll("\\\\" + "\\\\" , "\\\\");
        this.value = name;
    }

    public String strValue() {
        return value;
    }

    public String asmName() {
        return asmName;
    }
    public void setAsmName(String asmName) {
        this.asmName = asmName;
    }

    @Override
    public String toString() {
        return "constant entity : " + name;
    }
}
