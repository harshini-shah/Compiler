import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/*
 * This class converts the IR in SSA into final instructions that can directly be fed into
 * the "MachineCode" class and then that will convert them into machine code.
 * 
 * Inputs:
 * - In the parser, have a list of all the BasicBlocks (by ID) so that the instructions can be encoded
 * in order. Also, each basic block should have a function id associated with it so that I know that 
 * all the same function blocks are encoded together.
 * - 
 * 
 *  The output is a HashMap of Integer to Instruction of the following form:
 *  - The main Function comes first and then the auxiliary functions
 *  - Each operand of each instruction can either be a constant, a register or a spilled register - which would then 
 *  need a load and store.
 */
public class FinalInstructions {
    public Map<Integer, Instruction> finalInstructions;
    private int instructionNumber;
    private Map<Integer, Integer> old2new;
    private Set<Integer> toBeChanged;
    private RegisterAllocation ra;
    private CFG cfg;
    private Map<Integer, Integer> nextInstructions;
    
    public FinalInstructions(RegisterAllocation ra, CFG cfg) {
        instructionNumber = 0;
        finalInstructions = new HashMap<Integer, Instruction>();
        old2new = new HashMap<Integer, Integer>();
        toBeChanged = new TreeSet<Integer>();
        this.ra = ra;
        this.cfg = cfg;
        nextInstructions = new HashMap<Integer, Integer>();
        getFinalInstructions();
        modifyInstructions();
    }
    
    private void modifyInstructions() {
        for (Integer lineNum : toBeChanged) {
            Instruction inst = finalInstructions.get(lineNum);
            
            if (nextInstructions.containsKey(inst.op1.fixupLocation)) {
                inst.op1.fixupLocation = nextInstructions.get(inst.op1.fixupLocation);
                while (!old2new.containsKey(inst.op1.fixupLocation)) {
                    inst.op1.fixupLocation = nextInstructions.get(inst.op1.fixupLocation);
                }
                inst.op1.fixupLocation = old2new.get(inst.op1.fixupLocation);
              finalInstructions.put(lineNum, inst);

                continue;
            }
//            while (nextInstructions.containsKey(inst.op1.fixupLocation) && old2new.containsKey(inst.op1.fixupLocation)) {
//                inst.op1.fixupLocation = old2new.get(nextInstructions.get(inst.op1.fixupLocation));
//                finalInstructions.put(lineNum, inst);
//                continue;
//            }
            while (!old2new.containsKey(inst.op1.fixupLocation)) {
                
                inst.op1.fixupLocation++;
            }
            inst.op1.fixupLocation = old2new.get(inst.op1.fixupLocation);
            finalInstructions.put(lineNum, inst);
        }
    }
    
    private void getFinalInstructions() {
        // Iterate over each block (from the list of blocks), and then over each instruction in that
        // block in order (check if the function is right)
                int count = 0;    
            for (BasicBlock block : cfg.bbs) {
                ArrayList<Instruction> instructions = (ArrayList<Instruction>) block.getOrderedInstructions();
//                System.out.println("BLOCK " + block.blockId);
                
                for (int i = 0; i < instructions.size(); i++) {
                    
                    Instruction instr = instructions.get(i);
                    if (instr.isDeleted) {
                        continue;
                    }
//                    System.out.println("Instruction: " + instr.instructionNumber + "\t" + instr);

                    if (instr.kind == Instruction.Kind.PHI) {
                        // do nothing
                        if (i != instructions.size() - 1) {
                            nextInstructions.put(instr.instructionNumber, instructions.get(i + 1).instructionNumber);
                        }
//                        for (int jj : nextInstructions.keySet()) {
//                            System.out.println(jj + " -- > " + nextInstructions.get(jj));
//                        }
                    } else if (instr.kind == Instruction.Kind.BRANCH) {
                        
                        // the op1 of the instruction is where it should branch to - will be of type instruction
                        // the op2 is the condition on which it branches - which is basically the previous instruction
                        switch(instr.operation) {
                            case "BRA":    
                            case "BEQ":
                            case "BNE":
                            case "BLT":
                            case "BGE":
                            case "BLE":
                            case "BGT":
                                if (instr.op1.kind != Result.Kind.CONDN) {
                                    System.out.println("ERROR : Branch Op is of kind " + instr.op1.kind);
                                }
                                
                                Instruction newInstr = new Instruction(instr);
                                newInstr.op2 = new Result();
                                newInstr.op2.kind = Result.Kind.INSTR;
                                int cnt = 1;
                                while (!old2new.containsKey(instr.instructionNumber - cnt)) {
                                    cnt++;
                                }
                                newInstr.op2.version = old2new.get(instr.instructionNumber - cnt);
                                newInstr.op3 = null;
                                old2new.put(instr.instructionNumber, instructionNumber);
                                newInstr.instructionNumber = instructionNumber;
                                toBeChanged.add(instructionNumber);
                                finalInstructions.put(instructionNumber++, newInstr);
                                break;
                            default: 
                                System.out.println("ERROR: Branch instruction doesn't match any branch opcode");
                        }
                    } else if (instr.kind == Instruction.Kind.END) {
                        Instruction newInstr = new Instruction(instr);
                        newInstr.operation = "RET";
                        newInstr.op1 = new Result();
                        newInstr.op1.kind = Result.Kind.CONST;
                        newInstr.op1.value = 0;
                        newInstr.op2 = null;
                        newInstr.op3 = null;
                        old2new.put(instr.instructionNumber, instructionNumber);
                        newInstr.instructionNumber = instructionNumber;
                        finalInstructions.put(instructionNumber++, newInstr);
                    } else if (instr.kind == Instruction.Kind.FUNC) {
                        switch (instr.operation) {
                            case "WRITE NEW LINE":                                
                            case "READ":
                                Instruction newInstr = new Instruction(instr);
                                newInstr.op1 = null;
                                newInstr.op2 = null;
                                newInstr.op3 = null;
                                old2new.put(instr.instructionNumber, instructionNumber);
                                newInstr.instructionNumber = instructionNumber;
                                finalInstructions.put(instructionNumber++, newInstr);
                                break;
                            case "WRITE":
                                Instruction Instr = new Instruction(instr);
                                if (instr.op1.kind != Result.Kind.CONST && instr.op1.kind != Result.Kind.INSTR && instr.op1.kind != Result.Kind.REG) {
                                    System.out.println("ERROR : Op is of kind " + instr.op1.kind);
                                } else if (instr.op1.kind == Result.Kind.INSTR) {
                                    Instr.op1.kind = Result.Kind.REG;
//                                    for (int i : old2new.keySet()) {
//                                        System.out.println("Old 2 new : " + i + " " + old2new.get(i));
//                                    }
//                                    System.out.println(instr.op1.version);
                                    
                                    Instr.op1.regNo = ra.registerMapping.get(instr.op1.version);
                                }
                                
                                Instr.op2 = null;
                                Instr.op3 = null;
                                old2new.put(instr.instructionNumber, instructionNumber);
                                Instr.instructionNumber = instructionNumber;
                                finalInstructions.put(instructionNumber++, Instr);
                                break;
                            default:
//                                instr.op2 = null;
//                                instr.op3 = null;
//                                old2new.put(instr.instructionNumber, instructionNumber);
//                                instr.instructionNumber = instructionNumber;
//                                finalInstructions.put(instructionNumber++, instr);
                                break;

                        }
                        
                    } else if (instr.kind == Instruction.Kind.STD) {
                        switch(instr.operation) {
                        case "MOV":
                            if (instructionNumber > 1 && finalInstructions.get(instructionNumber - 1).kind == Instruction.Kind.BRANCH && finalInstructions.get(instructionNumber - 1).thisBlock == instr.thisBlock) {
                                
                                if (finalInstructions.get(instructionNumber - 1).operation.equals("BRA")) {
                                    finalInstructions.put(instructionNumber++, finalInstructions.get(instructionNumber - 2));
                                    finalInstructions.remove(instructionNumber - 2);
                                    if (toBeChanged.contains(instructionNumber - 2)) {
                                        toBeChanged.remove(instructionNumber - 2);

                                    }
                                    toBeChanged.add(instructionNumber - 1);
                                
                                Instruction newInstr = new Instruction(instr);

                                if (instr.op1.kind != Result.Kind.CONST && instr.op1.kind != Result.Kind.INSTR && instr.op1.kind != Result.Kind.REG) {
                                    System.out.println("ERROR : Op 1 is of kind " + instr.op1.kind);
                                } else if (instr.op1.kind == Result.Kind.INSTR) {
                                    newInstr.op1.kind = Result.Kind.REG;
                                    newInstr.op1.regNo = ra.registerMapping.get(instr.op1.version);                            }
                                
                                if (instr.op2.kind != Result.Kind.CONST && instr.op2.kind != Result.Kind.INSTR && instr.op2.kind != Result.Kind.REG) {
                                    System.out.println("ERROR : Op 2 is of kind " + instr.op2.kind);
                                } else if (instr.op2.kind == Result.Kind.INSTR) {
                                    newInstr.op2.kind = Result.Kind.REG;
                                    newInstr.op2.regNo = ra.registerMapping.get(instr.op2.version);                            }
                                
                                newInstr.op3 = null;
                                old2new.put(instr.instructionNumber, instructionNumber - 2);
                                newInstr.instructionNumber = instructionNumber - 2;
                                finalInstructions.put(instructionNumber - 2, newInstr);
                                } else {
                                    finalInstructions.put(instructionNumber++, finalInstructions.get(instructionNumber - 2));
                                    finalInstructions.put(instructionNumber - 2, finalInstructions.get(instructionNumber - 3));
//                                    finalInstructions.remove(instructionNumber - 2);
                                    if (toBeChanged.contains(instructionNumber - 2)) {
                                        toBeChanged.remove(instructionNumber - 2);

                                    }
                                    toBeChanged.add(instructionNumber - 1);
                                
                                Instruction newInstr = new Instruction(instr);
                                if (instr.op1.kind != Result.Kind.CONST && instr.op1.kind != Result.Kind.INSTR && instr.op1.kind != Result.Kind.REG) {
                                    System.out.println("ERROR : Op 1 is of kind " + instr.op1.kind);
                                } else if (instr.op1.kind == Result.Kind.INSTR) {
                                    newInstr.op1.kind = Result.Kind.REG;
                                    newInstr.op1.regNo = ra.registerMapping.get(instr.op1.version);                            }
                                
                                if (instr.op2.kind != Result.Kind.CONST && instr.op2.kind != Result.Kind.INSTR && instr.op2.kind != Result.Kind.REG) {
                                    System.out.println("ERROR : Op 2 is of kind " + instr.op2.kind);
                                } else if (instr.op2.kind == Result.Kind.INSTR) {
                                    newInstr.op2.kind = Result.Kind.REG;
                                    newInstr.op2.regNo = ra.registerMapping.get(instr.op2.version);                            }
                                
                                newInstr.op3 = null;
                                old2new.put(instr.instructionNumber, instructionNumber - 3);
                                newInstr.instructionNumber = instructionNumber - 3;
                                finalInstructions.put(instructionNumber - 3, newInstr);
                                }
                                
                            } else {
                                Instruction newInstr = new Instruction(instr);
                                if (instr.op1.kind != Result.Kind.CONST && instr.op1.kind != Result.Kind.INSTR && instr.op1.kind != Result.Kind.REG) {
                                    System.out.println("ERROR : Op 1 is of kind " + instr.op1.kind);
                                } else if (instr.op1.kind == Result.Kind.INSTR) {
                                    newInstr.op1.kind = Result.Kind.REG;
                                    newInstr.op1.regNo = ra.registerMapping.get(instr.op1.version);                            }
                                
                                if (instr.op2.kind != Result.Kind.CONST && instr.op2.kind != Result.Kind.INSTR && instr.op2.kind != Result.Kind.REG) {
                                    System.out.println("ERROR : Op 2 is of kind " + instr.op2.kind);
                                } else if (instr.op2.kind == Result.Kind.INSTR) {
                                    newInstr.op2.kind = Result.Kind.REG;
                                    newInstr.op2.regNo = ra.registerMapping.get(instr.op2.version);                            }
                                
                                newInstr.op3 = null;
                                old2new.put(instr.instructionNumber, instructionNumber);
                                newInstr.instructionNumber = instructionNumber;
                                finalInstructions.put(instructionNumber++, newInstr);
                            }
                            
                            break;
                        case "ADD":
                        case "SUB":
                        case "MUL":
                        case "DIV":
                        case "CMP":
                            Instruction Instr = new Instruction(instr);
                            if (instr.op1.kind != Result.Kind.CONST && instr.op1.kind != Result.Kind.INSTR && instr.op1.kind != Result.Kind.REG) {
                                System.out.println("ERROR : Op 1 is of kind " + instr.op1.kind);
                            } else if (instr.op1.kind == Result.Kind.INSTR) {
                                Instr.op1.kind = Result.Kind.REG;
                                Instr.op1.regNo = ra.registerMapping.get(instr.op1.version);                            }
                            
                            if (instr.op2.kind != Result.Kind.CONST && instr.op2.kind != Result.Kind.INSTR && instr.op2.kind != Result.Kind.REG) {
                                System.out.println("ERROR : Op 2 is of kind " + instr.op2.kind);
                            } else if (instr.op2.kind == Result.Kind.INSTR) {
                                Instr.op2.kind = Result.Kind.REG;
                                Instr.op2.regNo = ra.registerMapping.get(instr.op2.version);                            }
                            
                            Instr.op3 = null;
                            old2new.put(instr.instructionNumber, instructionNumber);
                            Instr.instructionNumber = instructionNumber;
                            finalInstructions.put(instructionNumber++, Instr);
                            break;
                        case "RET":
                        case "CALL":
                            Instruction newInstr = new Instruction(instr);
                            finalInstructions.put(instructionNumber++, newInstr);
                            break;
                        case "ADDA":
                        default:
                            System.out.println("ERROR : The instruction operation is not any standard instr " + instr.operation);
                        }
                    }
//                
            }
        }
            
//            for (int i : old2new.keySet()) {
//                System.out.println(i + " becomes " + old2new.get(i));
//            }
    }
}
