package com.mercy.compiler.Utility;

import com.mercy.compiler.AST.Location;

public class InternalError extends Error{
    public InternalError(String message) {
        super(message);
    }

    public InternalError(Location loc, String message) {
        super(loc.toString() + messge);
    }
}


