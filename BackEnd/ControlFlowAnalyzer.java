package com.mercy.compiler.BackEnd;

import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.INS.CJump;
import com.mercy.compiler.INS.Instruction;
import com.mercy.compiler.INS.Jmp;
import com.mercy.compiler.INS.Label;
import com.mercy.compiler.Option;

import java.io.PrintStream;
import java.util.*;

import static java.lang.System.err;



public class ControlFlowAnalyzer {
    private List<FunctionEntity> functionEntities;

    public ControlFlowAnalyzer(InstructionEmitter emitter) {
        functionEntities = emitter.functionEntities();
    }

    public void buildControFlow() {
        for (FunctionEntity functionEntity : functionEntities) {

            if (functionEntity.isInlined())
                continue;


        }
    }

    private int ct = 0;
    private void buildBasicBlock(FunctionEntity entity) {
        List<BasicBlock> bbs = new LinkedList<>();

        BasicBlock bb = null;
        for (Instruction ins : entity.INS()) {
            if (bb == null && !(ins instanceof Label)) { // add new label
                Label label = new Label("cfg_added_" + ct++);
                bb = new BasicBlock(label);
                bb.ins().add(label);
            }

            if (ins instanceof Label) {
                if (bb != null) {
                    bb.jumpTo().add((Label) ins);
                    bb.ins().add(new Jmp((Label) ins));
                    bbs.add(bb);
                }
                bb = new BasicBlock((Label) ins);
                bb.ins().add(ins);
            } else {
                bb.ins().add(ins);
                if (ins instanceof Jmp) {
                    bb.jumpTo().add(((Jmp) ins).dest());
                    bbs.add(bb);
                    bb = null;
                } else if (ins instanceof CJump) {
                    bb.jumpTo().add(((CJump) ins).trueLabel());
                    bb.jumpTo().add(((CJump) ins).falseLabel());
                    bbs.add(bb);
                    bb = null;
                }
            }
        }

        if (bb != null) { //  handle the case that a function ends without "return"
            bbs.add(bb);
        }

        // link edge
        for (BasicBlock basicBlock : bbs) {
            for (Label label : basicBlock.jumpto()) {

            }
        }

        entity.setBbs(bbs);
        entity.setINS(null); // disable direct instruction access, so you must access instructions by BasicBlocks
    }

    private void buildControlFlowGraph(FunctionEntity entity) {
        for (BasicBlock basicBlock : entity.bbs()) {
            // inside BB
            List<Instruction> ins = basicBlock.ins();
            Iterator<Instruction> iter = ins.iterator();
            if (iter.hasNext()) {
                Instruction pre = iter.next();
                while (iter.hasNext()){
                    Instruction now = iter.next();
                    pre.sucessor().add(now);
                    now.predessor().add(pre);
                    pre = now;
                }
            }
            // between two BBs
            Instruction first = ins.get(0);
            for (BasicBlock pre : basicBlock.predecessor()) {
                pre.ins().get(pre.ins().size()-1).sucessor().add(first);
            }
            Instruction last = ins.get(ins.size()-1);
            for (BasicBlock suc : basicBlock.successor()) {
                suc.ins().get(0).predessor().add(last);
            }
        }
    }

    private void Optimization(FunctionEntity entity) {
        boolean modified = true;
        while (modified) {
            modified = false;

            // merge two blocks A and B such that A is the only predecessor of B and B is the only successor of A.
            BasicBlock now;
            for (BasicBlock basicBlock : entity.bbs()) {
                if (basicBlock.successor().size() == 1 && basicBlock.successor().get(0).predecessor().size() == 1) {
                    now = basicBlock;
                    BasicBlock next = now.successor().get(0);
                    if (next.successor().size() != 0) {
                        modified = true;
                        for (BasicBlock next_next : next.successor()) {
                            next_next.predecessor().remove(next);
                            next_next.predecessor().add(now);
                            now.successor().add(next_next);
                        }

                        // remove label and jmp
                        next.ins().remove(0);
                        now.ins().remove(now.ins().size()-1);

                        now.ins().addAll(next.ins());
                        entity.bbs().remove(next);
                        now.successor().remove(next);
                        break;
                    }
                }
            }

            // remove blocks that contain only one jump instruction
            List<BasicBlock> uselessBasicBlock = new LinkedList<>();
            for (BasicBlock toremove : entity.bbs()) {
                if (toremove.ins().size() < 2)
                    continue;
                Instruction last = toremove.ins().get(1);



            }

            // remove unreachable blocks
            for (BasicBlock basicBlock : entity.bbs()) {
                if (basicBlock.predecessor().size() == 0 && basicBlock.label()) {
                    modified = true;
                    uselessBasicBlock.add(basicBlock);
                }
            }

            // replace CJump that has the same false label and true label


        }


    }

    private void layoutBasicBlock(FunctionEntity entity) {
        List<BasicBlock> bbs = entity.bbs();
        Queue<BasicBlock> queue = new ArrayDeque<>();

        queue.addAll(bbs);

        List<BasicBlock> newBBs = new LinkedList<>();
        // gready layout, to remove fall through jumps
        while (!queue.isEmpty()) {
            BasicBlock bb = queue.remove();
            while (bb != )
        }
    }
}
