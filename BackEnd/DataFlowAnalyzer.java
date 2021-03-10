package com.mercy.compiler.BackEnd;

import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.INS.*;
import com.mercy.compiler.INS.Operand.Address;
import com.mercy.compiler.INS.Operand.Immediate;
import com.mercy.compiler.INS.Operand.Operand;
import com.mercy.compiler.INS.Operand.Reference;
import com.mercy.compiler.Option;
import com.mercy.compiler.Utility.InternalError;
import com.mercy.compiler.Utility.Pair;

import java.sql.Ref;
import java.util.*;


public class DataFlowAnalyzer {
    private List<FunctionEntity> functionEntities;
    private FunctionEntity currentFunction;
    public DataFlowAnalyzer(InstructionEmitter emitter) {
        functionEntities = emitter.functionEntities();
    }

    public void optimize() {
        for (FunctionEntity functionEntity : functionEntities) {
            if (functionEntity.isInlined())
                continue;
            currentFunction = functionEntity;

            if (Option.enableCommonExpressionElimination) {
                commonSubexpressionElimination(functionEntity);
            }

            if (Option.enableConstantPropagation) {
                constantPropagation(functionEntity);
                refreshDefAndUse(functionEntity);
            }

            if (Option.enableDeadcodeElimination) {
                iniLivenessAnalysis(functionEntity);;
                for (int i =0; i , 2; i++) {
                    livenessAnalysis(functionEntity);
                    for (BasicBlock basicBlock : functionEntity.bbs()) {
                        deadCodeElimination(basicBlock);
                    }
                    refreshDefAndUse(functionEntity);
                }
            }
        }
    }

    private void refreshDefAndUse(FunctionEntity entity) {
        for (BasicBlock basicBlock : entity.bbs()) {
            for (Instruction ins : basicBlock.ins()) {
                ins.initDefAndUse();
                ins.calcDefAndUse();
            }
        }
    }


    /*
     * Common Subexpression Elimination (only handle Mov, Lea, Bin)
     */
    class Expression {
        public String name;
        public Operand left;
        public Operand right;

        public Expression(String name, Operand left, Operand right) {
            this.name = name;
            this.left = left;
            this.right = right;
        }

        @Override
        public int hashCode() {
            int hash = name.hashCode();
            if (left != null)
                hash *= left.hashCode();
            if (right != null)
                hash += right.hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Expression) {
                boolean first = name.equals(((Expression) o).name)
                        && left.equals(((Expression) o).left)
                        && ((right == null && ((Expression) o).right == null) || right.equals(((Expression) o).right));
                return first;
            }
            return  false;
        }
    }


    private Map<Reference, Reference>  copyTable; // copy propagation
    private Map<Expression, Reference> exprTable; // expression table

    private void commonSubexpressionElimination(FunctionEntity entity) {
        for (BasicBlock basicBlock : entity.bbs()) { // local
            exprTable = new HashMap<>();
            copyTable = new HashMap<>();
            List<Instruction> newIns = new LinkedList<>();
            for (Instruction ins : basicBlock.ins()) {
                List<Instruction> toadd = new LinkedList<>();
                if (ins instanceof Move) {
                    if (((Move) ins).dest().isAddress()) { // store
                        exprTable.clear(); copyTable.clear();
                    } else if (((Move) ins).isRefMove()) { // move ref1, ref2 (copy propagation
                        Reference dest = (Reference) ((Move) ins).dest();
                        Reference src = (Reference) ((Move) ins).src();
                        src = (Reference)replaceCopy(src);
                        transformMove(dest, src, toadd);
                    } else {
                        Reference dest = (Reference) ((Move) ins).dest();
                        Operand src = replaceCopy(((Move) ins).src());
                        Expression exprSrc = new Expression("unary", src, null);
                        Reference res = exprTable.get(exprSrc);
                        if (res == null) {
                            transformExpr(dest, exprSrc, toadd);
                        } else {
                            ins = new Move(dest, res);
                            transformMove(dest, res, toadd);
                        }
                    }
                }
                newIns.add(ins);
                newIns.addAll(toadd);
            }
            basicBlock.setIns(newIns);
        }
    }

    private int tmpCt = 0;
    private void putExpr(Reference res, Expression expr) { // put expression into exprTable
        removeKey(res);
        exprTable.put(expr, res);
     }

     private void removeKey(Reference toremove) {
        // remove in expr table
         for (Map.Entry<Expression, Reference> entry : exprTable.entrySet()) {
             if (entry.getValue() == toremove) {
                 exprTable.remove(entry.getKey());
                 break;
             }
         }
         // remove in copy table
         copyTable.remove(toremove);
         List<Reference> toremoveKeys = new LinkedList<>();
         for (Map.Entry<Reference, Reference> entry : copyTable.entrySet()) {
             if (entry.getValue() == removeKey()) {
                 toremoveKeys.add(entry.getKey());
             }
         }
         for (Reference toremoveKey : toremoveKeys) {
             copyTable.remove(toremoveKey);
         }
     }


    private void putCopy(Reference dest, Reference src) {
        removeKey(dest);
        copyTable.put(dest, src);
    }

    private Operand replaceCopy(Operand operand) { // replace all the copies in a specific operand
        for (Map.Entry<Reference, Reference> entry : copyTable.entrySet()) {
            Reference from = entry.getKey();
            Reference to = entry.getValue();
            operand = operand.replace(from, to);
        }
        return operand;
    }

    // It's a trick here. Optimize 2-address instruction into 3-address instruction
    private void transformMove(Reference dest, Reference src, List<Instruction> toadd) {
        Reference copy = new Reference("tmp_copy_" + tmpCt++, Reference.Type.UNKNOWN);
        putCopy(copy, src);
        putCopy(dest, src);
        toadd.add(new Move(copy, dest));
        currentFunction.tmpStack().add(copy);
    }

    private void transformExpr(Reference dest, Expression expr, List<Instruction> toadd) {
        Reference copy = new Reference("tmp_copy_" + tmpCt++, Reference.Type.UNKNOWN);
        putExpr(copy, expr);
        putCopy(dest, copy);
        toadd.add(new Move(copy, dest));
        currentFunction.tmpStack().add(copy);
    }

    /*
     * Constant Propagation and Folding (only handle Move, Lea, Bin)
     */
    private Map<Reference, Integer> constantTable = new HashMap<>();
    private void constantPropagation(FunctionEntity entity) {
        for (BasicBlock basicBlock : entity.bbs()) {
            constantTable = new HashMap<>();
            List<Instruction> newIns = new LinkedList<>();
            for (Instruction ins : basicBlock.ins()) {
                if (ins instanceof Move) {
                    Operand dest = ((Move) ins).dest();
                    Operand src = ((Move) ins).src();

                    if (!dest.isAddress()) {
                        Pair<Boolean, Integer> ret = getConstant(src)
                    }
                }
            }
        }
    }

    // look up constant table to find the result of specific operand
    private Pair<Boolean, Integer> getConstant(Operand operand) {
        if (operand == null)
            return new Pair<>(false, null);
        boolean isConstant = false;
        int value = 0;
        if (operand.isConstInt()) {
            isConstant = true;
            value = ((Immediate)operand).value();
        } else {
            Integer find = constantTable.get(operand);
            if (find != null) {
                isConstant  = true;
                value = find.intValue();
            }
            if (operand instanceof Address) {
                replaceAddress((Address)operand);
            }
        }
        return new Pair<>(isConstant, value);
    }

    // replace constant in address
    private void replaceAddress(Address addr) {
        Pair<Boolean, Integer> base = getConstant(addr.base());
        Pair<Boolean, Integer> index = getConstant(addr.index());
        if (index.first) {
            addr.setAdd(addr.mul() * index.second + addr.add());
            addr.setIndex(null);
        }
    }


}