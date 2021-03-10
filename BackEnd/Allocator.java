package com.mercy.compiler.BackEnd;

import com.mercy.compiler.AST.LiteralNode;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.Entity.ParameterEntity;
import com.mercy.compiler.Entity.Scope;
import com.mercy.compiler.INS.*;
import com.mercy.compiler.INS.Operand.*;
import com.mercy.compiler.Option;
import com.mercy.compiler.Utility.InternalError;

import javax.swing.*;
import javax.swing.text.EditorKit;
import java.util.*;

import static java.lang.System.err;

public class Allocator {
    private List<FunctionEntity> functionEntities;
    private List<Register> registers;
    private List<Reference> paraRegisterRef;
    private Set<Reference> callerSaveRegRef;
    private RegisterConfig regConfig;

    private Register rax, rbx, rcx, rdx, rsi, rdi, rsp, rbp;
    private Reference rrax, rrbx, rrcx, rrdx, rrsi, rrdi, rrsp, rrbp;
    private Reference rr10, rr11;
    private List<Register> colors = new LinkedList<>();
    private Scope globalScope;

    public Allocator (InstructionEmitter emitter, RegisterConfig regConfig) {
        functionEntities = emitter.functionEntities();
        this.regConfig = regConfig;
        globalScope = emitter.globalScope();

        // load registers
        rbp = regConfig.rbp();
        registers = regConfig.registers();
        rax = registers.get(0); rbx = registers.get(1);
        rcx = registers.get(2); rdx = registers.get(3);
        rsi = registers.get(4); rdi = registers.get(5);
        rbp = registers.get(6); rsp = registers.get(7);


        rrax = new Reference(rax);
        rr10 = new Reference(registers.get(10));
        rr11 = new Reference(registers.get(11));

        paraRegisterRef = new LinkedList<>();
        for (Register register : regConfig.paraRegister()) {
            paraRegisterRef.add(new Reference(register));
        }
        rrdi = paraRegisterRef.get(0); rrsi = paraRegisterRef.get(1);
        rrdx = paraRegisterRef.get(2); rrcx = paraRegisterRef.get(3);

        // set precolored
        precolored = new LinkedHashSet<>();
        precolored.addAll(paraRegisterRef);
        precolored.add(rrax);
        precolored.add(rr10);
        precolored.add(rr11);
        for (Reference ref : precolored) {
            ref.isPrecolored = true;
            ref.color = ref.reg();
        }




        // global
        private Set<Edge> edgeSet;
        private Set<Edge> simplifiedEdge;
        private int K;
        private int localOffset;

        // move set (disjoint)
        private Set<Move> coalescedMoves;
        private Set<Move> constrainedMoves;
        private Set<Move> frozenMoves;
        private Set<Move> worklistMoves;
        private Set<Move> activeMoves;


    }

    // rewrite instructions to satisfy the machine-relevant requirement
    private void loadPrecolord(FunctionEntity entity) {
        for (BasicBlock basicBlock : entity.bbs()) {
            List<Instruction> newIns = new LinkedList<>();
            for (Instruction raw : basicBlock.ins()) {
                if (raw instanceof Call) {
                    Set<Reference> paraRegUsed = new HashSet<>();
                    Call ins = (Call) raw;
                    int i = 0, pushCt = 0;
                    for (Operand operand : ins.operands()) {
                        if (i < paraRegisterRef.size()) {
                            paraRegUsed.add(paraRegisterRef.get(i));
                            newIns.add(new Move(paraRegisterRef.get(i), operand));
                        } else {
                            if (operand instanceof Immediate) {
                                Reference tmp = new Reference("tmp_push", Reference.Type.UNKNOWN);
                                newIns.add(new Move(tmp, operand));
                                operand = tmp;
                            }
                            newIns.add(new Push(operand));
                            pushCt++;
                        }
                        i++;
                    }
                    Call newCall = new Call(ins.entity(), new LinkedList<>());
                    newCall.setCallorsave(callerSaveRegRef);
                    newCall.setUsedParameterRegister(paraRegUsed);
                    newIns.add(newCall);
                    if (pushCt > 0) {
                        newIns.add(new Add(rsp, new Immediate(pushCt * Option.REG_SIZE)));
                    }
                    if (ins.ret()!=null) {
                        newIns.add(new Move(ins.ret(), rrax));
                    }
                } else if (raw instanceof Div || raw instanceof Mod) {
                    newIns.add(new Move(rrax, ((Bin)raw).left()));
                    newIns.add(new Move(rrdx, rdx)); // cqo

                    Operand right = ((Bin)raw).right();
                    // Right operand cannot be rdx (because of cqo), but our pre-color method does not
                    // sopport such constraint, so we should restrict the right operand to be a fix register 'rcs'
                    newIns.add(new Move(rrcx, right));
                    right = rrcx;

                    if (raw instanceof Div) {
                        newIns.add(new Div(rrax, right));
                        newIns.add(new Move(rrax, rax));  // refresh
                        newIns.add(new Move(rrdx, rdx));
                        newIns.add(new Move(((Div) raw).left(), rrax));
                    } else {
                        newIns.add(new Mod(rrax, right));
                        newIns.add(new Move(rrax, rax)); // refresh
                        newIns.add(new Move(rrdx, rdx));
                        newIns.add(new Move(((Mod) raw).left(), rrdx));
                    }
                } else if (raw instanceof Return) {
                    if (((Return) raw).ret() != null)
                        newIns.add(new Move(rrax, ((Return)raw).ret()));
                    newIns.add(new Return(null));
                } else if (raw instanceof Label) {
                    if (raw == entity.beginLabelINS()) {
                        int i = 0;
                        for (ParameterEntity par : entity.params()) {   // load parameters
                            if (i < paraRegisterRef.size()) {
                                newIns.add(new Move(par.reference(), paraRegisterRef.get(i)));
                            } else {
                                newIns.add(new Move(par.reference(), par.source()));
                            }
                            i++;
                        }
                    }
                    newIns.add(raw);
                }
                else if (raw instanceof Sal || raw instanceof Sar) {
                    if (((Bin)raw).right() instanceof  Immediate) {
                        newIns.add(raw);
                    } else {
                        newIns.add(new Move(rrcx, ((Bin)raw).right()));
                        if (raw instanceof Sal)
                            newIns.add(new Sal(((Bin)raw).left(), rrcx));
                        else
                            newIns.add(new Sar(((Bin)raw).left(), rrcx));
                    }
                } else if (raw instanceof Cmp) {
                    Operand legt = ((Cmp) raw).left();
                    Operand right = ((Cmp) raw).right();
                    transCompare(newIns, raw, left, right);
                } else if (raw instanceof CJump && ((CJump)raw).type() != CJump.Type.BOOL) {
                    Operand left = ((CJump) raw).left();
                    Operand right = ((CJump) raw).right();
                    transCompare(newIns, raw, left, right);
                } else {
                    newIns.add(raw);
                }
            }
            basicBlock.setIns(newIns);
        }
    }

    private int spilledCounter = 0;
    private void rewriteProgram(FunctionEntity entity) {
        Set<Reference> newTemp = new HashSet<>();
        List<Instruction> newIns;

        // allocate memory offset for spilled nodes
        for (Reference ref : spilledNodes) {
            ref.isSpilled = true;
            localOffset += Option.REG_SIZE;
            ref.setOffset(-localOffset, rbp);
        }

        // path compression
        for (Reference ref : coalesceNodes) {
            getAlias(ref);
        }

        // rewrite program
        List<Instruction> stores = new LinkedList<>();
        for (BasicBlock basicBlock : entity.bbs()) {
            newIns = new LinkedList<>();
            for (Instruction ins : basicBlock.ins()) {
                Set<Reference> insUse = ins.use();
                Set<Reference> insDef = ins.def();

                stores.clear();
                if (!(ins instanceof Label)) {
                    for (Reference use : insUse) {
                        if (use.isSpilled) {
                            if (insDef.contains(use)) {
                                Reference tmp = new Reference("spill_" + use.name() + "_" + spilledCounter++, Reference.Type.UNKNOWN);
                                newTemp.add(tmp);
                                newIns.add(new Move(tmp, new Address(rbp, null, 1, use.offset())));
                                ins.replaceAll(use, tmp);
                                stores.add(new Move(new Address(rbp, null, 1, use.offset()), tmp));
                            } else {
                                if (ins instanceof Move && !(((Move) ins).dest()).isAddress() && ((Move)ins).src() == use) {
                                    // optimization for move
                                    ins = new Move(((Move) ins).dest(), new Address(rbp, null, 1, use.offset()));
                                } else {
                                    Reference tmp = new Reference("spill_" + use.name() + "_" + spilledCounter++, Reference.Type.UNKNOWN);
                                    newTemp.add(tmp);
                                    newIns.add(new Move(tmp, new Address(rbp, null, 1, use.offset())));
                                    ins.replaceUse(use, tmp);
                                }
                            }
                        }
                    }

                    for (Reference def : insDef) {
                        if (def.isSpilled) {
                            if (insUse.contains(def)) {
                                ; //already done in previous step
                            } else {
                                if (ins instanceof Move && !(((Move) ins).src()).isAddress() && ((Move) ins).dest() == def) {
                                    // optimization for move
                                    ins = new Move(new Address(rbp, null, 1, def.offset()), ((Move) ins).src());
                                } else {
                                    Reference tmp = new Reference("spill_" + def.name() + "_" + spilledCounter++, Reference.Type.UNKNOWN);
                                    newTemp.add(tmp);
                                    ins.replaceDef(def, tmp);
                                    stores.add(new Move(new Address(rbp, null, 1, def.offset()), tmp));
                                }
                            }
                        }
                    }
                }

                for (Reference ref : ins.allref()) {
                    if (coalescedNodes.contains(ref)) {
                        ins.replaceAll(ref, getAlias(ref));
                    }
                }
                ins.initDefAndUse();
                ins.calcDefAndUse();
                if (ins instanceof Move && ((Move) ins).isRefMove() && ((Move) ins).dest() == ((Move) ins).src())
                    ; // ignore redundant move
                else
                    newIns.add(ins);
                newIns.addAll(stores);
            }
            basicBlock.setIns(newIns);
        }


    }

    private void assignColors(FunctionEntity entity) {
        // restore simplified edges
        for (Edge edge : simpifiedEdge) {
            addEdge(getAlias(edge.u), getAlias(edge.v));
        }

        // begin assign
        LinkedList<Register> okColors = new LinkedList<>();
        while (!selectStack.empty()){
            
        }
     }

        private void transCompare(List<Instruction> newIns, Instruction raw, Operand left, Operand right) {
        if (left.isAddress() && right.isAddress()) {
            Reference tmp = new Reference("tmp_cmp", Reference.Type.UNKNOWN);
            newIns.add(new Move(tmp, left));
            if (raw instanceof Cmp) {
                ((Cmp) raw).setLeft(tmp);
                newIns.add(raw);
                newIns.add(new Move(left, tmp));
            } else  {
                ((CJump)raw).setLeft(tmp);
                newIns.add(raw);
            }
        } else {
            newIns.add(raw);
        }
    }


}
