package com.mercy.compiler.Utility;

import com.mercy.compiler.AST.Location;

public class SemanticError extends Error {
    public SemanticError(Location loc, String message) {
        //super(message);
        super(loc.toString() + message);
    }
}

