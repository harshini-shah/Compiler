import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    public static Map<Integer, Instruction> finalInstructions;
    private int instructionNumber;
    private Map<Integer, Integer> old2new;
    private ArrayList<Integer> toBeChanged;
    
    public FinalInstructions() {
        instructionNumber = 0;
        old2new = new HashMap<Integer, Integer>();
        finalInstructions = new HashMap<Integer, Instruction>();
        toBeChanged = new ArrayList<Integer>();
        getFinalInstructions();
        modifyInstructions();
    }
    
    private void modifyInstructions() {
        for (Integer lineNum : toBeChanged) {
            Instruction inst = finalInstructions.get(lineNum);
            while (!old2new.containsKey(inst.op1.version)) {
                inst.op1.version++;
            }
            inst.op1.version = old2new.get(inst.op1.version);
            finalInstructions.put(lineNum, inst);
        }
    }
    
    private void getFinalInstructions() {
        // Iterate over each block (from the list of blocks), and then over each instruction in that
        // block in order (check if the function is right)
        
        for (CFG cfg : Parser.functionCFGs) {
            
            for (BasicBlock block : cfg.bbs) {
                ArrayList<Instruction> instructions = (ArrayList<Instruction>) block.getOrderedInstructions();
                for (Instruction instr : instructions) {
                    if (instr.isDeleted) {
                        continue;
                    }
                    if (instr.kind == Instruction.Kind.PHI) {
                        // do nothing
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
                                if (instr.op1.kind != Result.Kind.INSTR) {
                                    System.out.println("ERROR : Branch Op is of kind " + instr.op1.kind);
                                }
                                instr.op2.kind = Result.Kind.INSTR;
                                instr.op2.version = old2new.get(instr.instructionNumber - 1);
                                instr.op3 = null;
                                old2new.put(instr.instructionNumber, instructionNumber);
                                instr.instructionNumber = instructionNumber;
                                toBeChanged.add(instructionNumber);
                                finalInstructions.put(instructionNumber++, instr);
                                break;
                            default: 
                                System.out.println("ERROR: Branch instruction doesn't match any branch opcode");
                        }
                    } else if (instr.kind == Instruction.Kind.END) {
                        instr.operation = "RET";
                        instr.op1.kind = Result.Kind.CONST;
                        instr.op1.value = 0;
                        instr.op2 = null;
                        instr.op3 = null;
                        old2new.put(instr.instructionNumber, instructionNumber);
                        instr.instructionNumber = instructionNumber;
                        finalInstructions.put(instructionNumber++, instr);
                    } else if (instr.kind == Instruction.Kind.FUNC) {
                        switch (instr.operation) {
                            case "WRITE NEW LINE":
                                instr.op1 = null;
                                instr.op2 = null;
                                instr.op3 = null;
                                old2new.put(instr.instructionNumber, instructionNumber);
                                instr.instructionNumber = instructionNumber;
                                finalInstructions.put(instructionNumber++, instr);
                                break;
                            case "READ":
                                instr.op1 = null;
                                instr.op2 = null;
                                instr.op3 = null;
                                old2new.put(instr.instructionNumber, instructionNumber);
                                instr.instructionNumber = instructionNumber;
                                finalInstructions.put(instructionNumber++, instr);
                                break;
                            case "WRITE":
                                if (instr.op1.kind != Result.Kind.CONST && instr.op1.kind != Result.Kind.INSTR) {
                                    System.out.println("ERROR : Op is of kind " + instr.op1.kind);
                                } else if (instr.op1.kind == Result.Kind.INSTR) {
                                    instr.op1.version = old2new.get(instr.op1.version);
                                }
                                
                                instr.op2 = null;
                                instr.op3 = null;
                                old2new.put(instr.instructionNumber, instructionNumber);
                                instr.instructionNumber = instructionNumber;
                                finalInstructions.put(instructionNumber++, instr);
                                break;
                            default:
                                System.out.println("ERROR: Instruction is of type func but doesn't match any predefined function");
                        }
                        
                    } else if (instr.kind == Instruction.Kind.STD) {
                        switch(instr.operation) {
                        case "ADD":
                        case "SUB":
                        case "MUL":
                        case "DIV":
                        case "CMP":
                            if (instr.op1.kind != Result.Kind.CONST && instr.op1.kind != Result.Kind.INSTR) {
                                System.out.println("ERROR : Op is of kind " + instr.op1.kind);
                            } else if (instr.op1.kind == Result.Kind.INSTR) {
                                instr.op1.version = old2new.get(instr.op1.version);
                            }
                            
                            instr.op3 = null;
                            old2new.put(instr.instructionNumber, instructionNumber);
                            instr.instructionNumber = instructionNumber;
                            finalInstructions.put(instructionNumber++, instr);
                            break;
                        case "RET":
                        case "CALL":
                        case "ADDA":
                        default:
                            System.out.println("ERROR : The instruction operation is not any standard instr " + instr.operation);
                        }
                    }
                }
                
            }
        }
    }
}
