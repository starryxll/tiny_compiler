package com.mercy.compiler.BackEnd;

import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.Entity.ParameterEntity;
import com.mercy.compiler.Entity.VariableEntity;
import com.mercy.compiler.INS.Instruction;
import com.mercy.compiler.INS.Operand.Reference;
import com.mercy.compiler.INS.Operand.Register;
import com.mercy.compiler.Option;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.mercy.compiler.Option.REG_SIZE;
import static java.lang.System.err;

public class NaiveAllocator {
    private List<FunctionEntity> functionEntities;

    private Register rax, rcx, rdx, rbx, rsp, rbp, rsi, rdi;

}
