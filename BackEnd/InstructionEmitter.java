package com.mercy.compiler.BackEnd;

import com.mercy.compiler.AST.FunctionDefNode;
import com.mercy.compiler.Entity.*;
import com.mercy.compiler.INS.*;
import com.mercy.compiler.INS.CJump;
import com.mercy.compiler.INS.Call;
import com.mercy.compiler.INS.Label;
import com.mercy.compiler.INS.Operand.*;
import com.mercy.compiler.INS.Return;
import com.mercy.compiler.IR.*;
import com.mercy.compiler.Option;
import com.mercy.compiler.Utility.InternalError;
import com.mercy.compiler.Utility.Triple;

import javax.swing.*;
import java.util.*;

import static com.mercy.compiler.INS.Operand.Reference.Type.*;
import static com.mercy.compiler.IR.Binary.BinaryOp.*;
import static java.lang.System.err;

public class InstructionEmitter {
    private List<FunctionEntity> functionEntities;
    private Scope globalScope;
    private List<IR> globalInitializer;

    private List<Instruction> ins;
    private FunctionEntity currentFunction;
    private boolean isInLeaf;

    // temp virtual register
    private List<Reference> tmpStack;
    private int tmpTop = 0;
    private int tmpCounter = 0;

    public InstructionEmitter(IRBuilder irBuilder) {
        this.globalScope = irBuilder.globalScope();
        this.functionEntities = new LinkedList<>(irBuilder.functionEntities());
        for (ClassEntity entity : irBuilder.ast().classEntitsies()) {
            for (FunctionDefNode functionDefNode : entity.memberFuncs()) {
                this.functionEntities.add(functionDefNode.entity());
            }
        }
        this.globalInitializer = irBuilder.globalInitializer();
    }

    public void emit() {
        int stringCounter = 1;

        // set reference for gloobal variables
        for (Entity entity : globalScope.entities().values()) {
            // System.out.println(entity.name())
            if (entity instanceof VariableEntity) {
                entity.setReference(new Reference(entity.name(), GLOBAL));
            } else if (entity instanceof StringConstantEntity) {
                ((StringConstantEntity) entity).setAsmName(StringConstantEntity.STRING_CONSTANT_ASM_LABEL_PREFIX + stringCounter++);
            }
        }

        // emit functions
        for (FunctionEntity functionEntity : functionEntities) {
            currentFunction = functionEntity;
            tmpStack = new ArrayList<>();
            functionEntity.setINS(emitFuntion(functionEntity));
            functionEntity.setTmpStack(tmpStack);
        }
    }

    private Map<Entity, Entity> globalLcalMap = new HashSet<>();
    private Set<Entity> usedGlobal;

    private List<Instruction> emitFunction(FunctionEntity entity) {
        if (entity.isInlined())
            return null;

        // leaf function optimization
        int callSize = entity.calls().size();
        for (FunctionEntity called : entity.calls()) {
            if (called.isInlined() || called.isLibFunction()) {
                callSize--;
            }

            if (Option.enableLeafFunctionOptimization && callSize == 0) {
                isInLeaf = true;
                err.println(entity.name() + "is leaf");
                usedGlobal = new HashSet<>();
                // make copy to local
                for (Entity global : globalScope.entities().values()) {
                    if (global instanceof VariableEntity) {
                        VariableEntity local  = new VariableEntity(global.location(), global.type(), "g_" + global.name(), null);
                        globalLcalMap.put(global, local);
                        currentFunction.scope().insert(local);
                    }
                }
            } else
                isInLeaf = false;

            // set reference for params and local variable
            for (ParameterEntity parameterEntity : (entity)) {
                parameterEntity.setReference(new Reference(parameterEntity));
                parameterEntity.setSource(new Reference(parameterEntity.name() + "_src", CANNOT_COLOR));
            }
            for (VariableEntity variableEntity : entity.allLocalVariables()) {
                variableEntity.setReference(new Reference(new ));
            }

            // emit instructions
            entity.setLabelINS(getLabel(entity.beginLabelIR().name()), getLabel(entity.endLabelIR().name()));
            ins = new LinkedList<>();
            for (IR ir : entity.IR()) {
                tmpTop = exprDepth = 0;
                ir.accept(this);
            }

            // if is in leaf, add move instruction for copy global to local
            if (isInLeaf) {
                for (Entity global : usedGlobal) {
                    ins.add(1, new Move(transEntity(global).reference(), global.reference()));
                    ins.add(ins.size(), new Move(global.reference(), transEntity(global).reference()));
                }
            }
            return ins;
        }
    }

    /*
     * Instruction Selection (match address)
     */
    private boolean isPowerOf2(Expr ir) {
        if (ir instanceof IntConst) {
            int x = ((IntConst) ir).value();
            return x == 1 || x == 2 || x == 4 || x == 8;
        }
        return false;
    }
    private class AddressTuple {
        Expr base, index;
        int mul, add;
        AddressTuple(Expr base, Expr index, int mul, int add) {
            this.base = base;
            this.index = index;
            this.mul = mul;
            this.add = add;
        }
    }



    /*
     * IR Visitor
     */
    private int exprDepth = 0;
    public Operand visitExpr(com.mercy.compiler.IR.Expr ir) {
        boolean matched = false;
        Operand ret = null;

        exprDepth++;
        AddressTuple addr;

        // instruction selection for "lea"
        if ((addr = matchAddress(ir)) != null) {
            Operand base = visitExpr(addr.base);
            Operand index = null;
            if (addr.index != null) {
                index = visitExpr(addr.index);
                index = eliminateAddress(index);
            }
            base = eliminateAddress(base);

            ret = getTmp();
            ins.add(new Lea((Reference) ret, new Address(base, index, addr.add)));
            matched = true;
        }

        if (!matched) {
            ret = ir.accept(this);
        }
        exprDepth--;
        return ret;
    }

    public Operand visit(com.mercy.compiler.IR.Addr ir) {
        throw new InternalError("cannot happen in instruction emitter Addr ir");
    }

    public Operand visit(com.mercy.compiler.IR.Assign ir) {
        Operand dest = null;

        if (ir.left() instanceof Addr) {
            dest = transEntity(((Addr) ir.left()).entity()).reference();
        } else {
            Operand lhs = visitExpr(ir.left());
            lhs = eliminateAddress(LHS);
            dest = new Address(lhs);
        }

        exprDepth++;
        Operand rhs = visitExpr(ir.right());
        exprDepth--;

        if (dest.isAddress() && rhs.isAddress()) {
            Reference tmp = getTmp();
            ins.add(new Move(tmp, rhs));
            ins.add(new Move(dest, tmp));
        } else
            ins.add(new Move(dest, rhs));

        return null;
    }



    // only match [base + index * mul]
    private boolean matchSimpleAdd = false;  // whether to match [base + index]
    private Triple<Expr, Expr, Integer>  matchBaseIndexMul(Expr expr) {
        if (!(expr instanceof Binary))
            return null;
        Binary bin = (Binary)expr;

        Expr base = null, index = null;
        int mul = 0;
        boolean matched = false;
        if (bin.operator() == ADD) {
            if (bin.right() instanceof Binary && ((Binary) bin.right()).operator() == MUL) {
                base = bin.left();
                Binary right = (Binary) bin.right();
                if (isPowerOf2(right.right())) {
                    index = right.left();
                    mul = ((IntConst) right.right()).value();
                    matched = true;
                } else if (isPowerOf2(right.left())) {
                    index = right.right();
                    mul = ((IntConst) right.left()).value();
                    matched = true;
                }
            } else if (bin.left() instanceof Binary && ((Binary) bin.left()).operator() == MUL) {
                base = bin.right();
                Binary left = (Binary) bin.left();
                if (isPowerOf2(left.right())) {
                    index = left.left();
                    mul = ((IntConst) left.right()).value();
                    matched = true;
                } else if (isPowerOf2(left.left())) {
                    index = left.right();
                    mul = ((IntConst) left.left()).value();
                    matched = true;
                }
            } else if (matchSimpleAdd) {
                base = bin.left();
                index = bin.right();
                mul = 1;
                matched = true;
            }
        }
        if (matched) {
            return new Triple<>(base, index, mul);
        } else {
            return null;
        }
    }

    // march all types of address [base + index * mul + offset]
    // two steps : 1.match offset 2. match [base + index * mul]
    private AddressTuple matchAddress(Expr expr) {
        if (!Option.enableInstructionSelection)
            return null;

        if (!(expr instanceof Binary))
            return null;

        Binary bin =  (Binary)expr;

        Expr base = null, index = null;
        int mul = 1, add = 0;
        boolean matched = false;
        Triple<Expr, Expr, Integer> baseIndexMul = null;
        if (bin.operator() == ADD) {
            if (bin.right() instanceof IntConst) {
                add = ((IntConst) bin.right()).value();

                if ((baseIndexMul = matchBaseIndexMul))
            }
        }
    }

    /*
     * utility for the emitter
     */
    // make the label unique, i.e. point to the same object
    private Map<String, Label> labelMap = new HashMap<>();
    private Label getLabel(String name) {
        Label ret = labelMap.get(name);
        if (ret == null) {
            ret = new Label(name);
            labelMap.put(name, ret)
        }
        return ret;
    }

    // translate entity for global in leaf function
    private Entity transEntity(Entity entity) {
        if (isInLeaf) {
            Entity ret = globalLcalMap.get(entity);
            if (ret != null) {
                usedGlobal.add(entity);
                return ret;
            }
        }
        return entity;
    }

    // to get rid of nested address
    private Operand eliminateAddress(Operand operand) {
        if (operand instanceof Address || (operand instanceof Reference && ((Reference) operand).type() == GLOBAL)) {
            Reference tmp = getTmp();
            ins.add(new Move(tmp, operand));
            return tmp;
        } else {
            return operand;
        }
    }

    // get lvalue for immediate or reference of entity;
    private Reference getLvalue(Operand operand) {
        operand = eliminateAddress(operand);
        if (operand instanceof Immediate ||
                (operand instanceof Reference && !((Reference) operand).canBeAccumulator())) {
            Reference ret = getTmp();
            ins.add(new Move(ret, operand));
            return ret;
        }
        return (Reference) operand;
    }

    // temp virtual register utility function
    public Reference getTmp() {
        if (Option.enableGlobalRegisterAllocation) {
            return new Reference("ref_" + tmpCounter++, UNKNOWN);
        } else {
            // reuse temp register
            if (tmpTop >= tmpStack.size()) {
                tmpStack.add(new Reference("ref_" + tmpCounter, UNKNOWN));
            }
            return tmpStack.get(tmpTop++);
        }
    }

    private boolean isCommutative(com.mercy.compiler.IR.Binary.BinaryOp op) {
        switch(op) {
            case ADD: case MUL:
            case BIT_AND: case BIT_OR: case BIT_XOR:
            case EQ: case NE:
                return true;
            default:
                return false;
        }
    }

    private boolean isCompareOP(Binary.BinaryOp op) {
        switch (op) {
            case EQ: case NE:
            case GT: case GE:
            case LT: case LE:
                return true;
            default:
                return false;
        }
    }
}