import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class DLXMachine {
    
    // state variables
    static int[] R = new int[32];   // The value of R0 is always 0; R31 stores the return address 
    public enum Format {F1, F2, F3};

    public static int pc, op, a, b, c;
    public Format format;
    
    // Assuming 10000 bytes available in memory
    public static int memorySize = 10000;
    public static int[] memory = new int[memorySize/4];
    
    // To print operations, need a map of op to the instruction
    static final String[] mnemonics = {"ADD", "SUB", "MUL", "DIV", "MOD", "CMP", "ERR", "ERR", "OR", 
            "AND", "BIC", "XOR", "LSH", "ASH", "CHK", "ERR", "ADDI", "SUBI", "MULI", "DIVI", "MODI", 
            "CMPI", "ERR", "ERR", "ORI", "ANDI", "BICI", "XORI", "LSHI", "ASHI", "CHKI", "ERR", "LDW",
            "LDX", "POP", "ERR", "STW", "STX", "PSH", "ERR", "BEQ", "BNE", "BLT", "BGE", "BLE", "BGT", 
            "BSR", "ERR", "JSR", "RET", "RDD", "WRD", "WRH", "WRL"};
    
    
    
    public static Set<Integer> F1ops;
    public static Set<Integer> F2ops;
    public static Set<Integer> F3ops;
    
    public void load(int[] program) {
        populateOpCodeSetsByFormat();
        int i = 0;
        for (i = 0; i < program.length; i++) {
            memory[i] = program[i];
        }
    }
    
    public void execute() {
        for (int i = 0; i < R.length; i++) {
            R[i] = 0;
        }
        
        // R30 has to point to the globals section
        R[30] = 4*memory.length - 1;
        
        // Initializing program counter to zero
        pc = 0;
        
        program: while (true) {
            getInstructionParameters(memory[pc]);
            int saved_c = c;
            int nextpc = 0;
            
            if (format == Format.F2) {
                c = R[c];
            }
            
            switch(op) {
                case DLXInstruction.ADD:
                case DLXInstruction.ADDI:
                    R[a] = R[b] + c;
                    break;
                case DLXInstruction.SUB:
                case DLXInstruction.SUBI:
                    R[a] = R[b] - c;
                    break;
                case DLXInstruction.MUL:
                case DLXInstruction.MULI:
                    System.out.println("a is " + a);
                    System.out.println(" b is " + b);
                    System.out.println("c is " + c);
                    R[a] = R[b] * c;
                    break;
                case DLXInstruction.DIV:
                case DLXInstruction.DIVI:
                    R[a] = R[b] / c;
                    break;
                case DLXInstruction.MOD:
                case DLXInstruction.MODI:
                    R[a] = R[b] % c;
                    break;
                case DLXInstruction.CMP:
                case DLXInstruction.CMPI:
                    R[a] = R[b] - c;
                    if (R[a] < 0) {
                        R[a] = -1;
                    } else if (R[a] > 0){
                        R[a] = 1;
                    }
                    break;
                case DLXInstruction.OR:
                case DLXInstruction.ORI:
                    R[a] = R[b] | c;
                    break;
                case DLXInstruction.AND:
                case DLXInstruction.ANDI:
                    R[a] = R[b] & c;
                    break;
                case DLXInstruction.BIC:
                case DLXInstruction.BICI:
                    R[a] = R[b] & ~c;
                    break;
                case DLXInstruction.XOR:
                case DLXInstruction.XORI:
                    R[a] = R[b] ^ c;
                    break;
                case DLXInstruction.LSH:
                case DLXInstruction.LSHI:
                    if (c < 0) {
                        R[a] = R[b] >>> -c;
                    } else {
                        R[a] = R[b] << c;
                    }
                    
                    break;
                case DLXInstruction.ASH:
                case DLXInstruction.ASHI:
                    if (c < 0) {
                        R[a] = R[b] >> -c;
                    } else {
                        R[a] = R[b] << c;
                    }
                    break;
                case DLXInstruction.CHK:
                case DLXInstruction.CHKI:
                    if (R[a] < 0) {
                        System.out.println("Shold halt: CHK or CHKI");
                    } else if(R[a] >= c) {
                        System.out.println("Should halt: CHK or CHKI");
                    }
                    break;
                case DLXInstruction.LDW:
                case DLXInstruction.LDX:
                    R[a] = memory[R[b/4] + c];
                    break;
                case DLXInstruction.POP:
                    R[a] = memory[R[b/4]];
                    R[b] += c;
                    break;
                case DLXInstruction.STW:
                case DLXInstruction.STX:
                    memory[R[b/4] + c] = R[a];
                    break;
                case DLXInstruction.PSH:
                    R[b] += c;
                    memory[R[b/4]] = R[a];
                    break;
                case DLXInstruction.BEQ:
                    if (R[a] == 0) {
                        nextpc = pc + c;
                    }
                    break;
                case DLXInstruction.BNE:
                    if (R[a] != 0) {
                        nextpc = pc + c;
                    }
                    break;
                case DLXInstruction.BLT:
                    if (R[a] < 0) {
                        nextpc = pc + c;
                    }
                    break;
                case DLXInstruction.BGE:
                    if (R[a] >= 0) {
                        nextpc = pc + c;
                    }
                    break;
                case DLXInstruction.BLE:
                    if (R[a] <= 0) {
                        nextpc = pc + c;
                    }
                    break;
                case DLXInstruction.BGT:
                    if (R[a] > 0) {
                        nextpc = pc + c;
                    }
                    break;
                case DLXInstruction.BSR:
                    R[31] = pc * 4;  
                    nextpc = pc + c;
                    break;
                case DLXInstruction.JSR:
                    R[31] = pc * 4;
                    nextpc = c/4;
                    break;
                case DLXInstruction.RET:
                    if (saved_c == 0) {
                        System.out.println("EOF");
                        break program;
                    }
                    nextpc = c/4;
                    break;
                case DLXInstruction.RDD:
                    System.out.println("Enter a number: ");
                    String input = "";
                    try {
                        input = new BufferedReader(new InputStreamReader(System.in)).readLine();
                    } catch (IOException e) {
                        System.out.println("Could not take in input at RDD");
                    }
                    R[a] = Integer.parseInt(input);
                    break;
                case DLXInstruction.WRD:
                    System.out.println(R[b]);
                    break;
                case DLXInstruction.WRH:
                    System.out.print("0x" + Integer.toHexString(R[b]) + " ");
                    break;
                case DLXInstruction.WRL:
                    System.out.println();
                    break;
            }
            
            // setting the next pc
            pc = nextpc == 0 ? pc + 1 : nextpc;
            if ((pc < 0) || (pc > memory.length)) { // check about the multiplication / division by 4
                System.out.println("The pc is out of bounds");
            }
        }  
    }
    
    private void populateOpCodeSetsByFormat() {
        F1ops = new HashSet<Integer>();
        F2ops = new HashSet<Integer>();
        F3ops = new HashSet<Integer>();
        
        F2ops.add(DLXInstruction.SUB);
        F2ops.add(DLXInstruction.ADD);
        F2ops.add(DLXInstruction.MUL);
        F2ops.add(DLXInstruction.DIV);
        F2ops.add(DLXInstruction.MOD);
        F2ops.add(DLXInstruction.CMP);
        F2ops.add(DLXInstruction.OR);
        F2ops.add(DLXInstruction.AND);
        F2ops.add(DLXInstruction.BIC);
        F2ops.add(DLXInstruction.XOR);
        F2ops.add(DLXInstruction.LSH);
        F2ops.add(DLXInstruction.ASH);
        F2ops.add(DLXInstruction.CHK);
        F2ops.add(DLXInstruction.LDX);
        F2ops.add(DLXInstruction.STX);
        F2ops.add(DLXInstruction.RET);
        F2ops.add(DLXInstruction.RDD);
        F2ops.add(DLXInstruction.WRD);
        F2ops.add(DLXInstruction.WRH);
        
        F1ops.add(DLXInstruction.SUBI);
        F1ops.add(DLXInstruction.ADDI);
        F1ops.add(DLXInstruction.MULI);
        F1ops.add(DLXInstruction.DIVI);
        F1ops.add(DLXInstruction.MODI);
        F1ops.add(DLXInstruction.CMPI);
        F1ops.add(DLXInstruction.ORI);
        F1ops.add(DLXInstruction.ANDI);
        F1ops.add(DLXInstruction.BICI);
        F1ops.add(DLXInstruction.XORI);
        F1ops.add(DLXInstruction.LSHI);
        F1ops.add(DLXInstruction.ASHI);
        F1ops.add(DLXInstruction.CHKI);
        F1ops.add(DLXInstruction.LDW);
        F1ops.add(DLXInstruction.POP);
        F1ops.add(DLXInstruction.STW);
        F1ops.add(DLXInstruction.PSH);
        F1ops.add(DLXInstruction.BEQ);
        F1ops.add(DLXInstruction.BNE);
        F1ops.add(DLXInstruction.BLT);
        F1ops.add(DLXInstruction.BGE);
        F1ops.add(DLXInstruction.BLE);
        F1ops.add(DLXInstruction.BGT);
        F1ops.add(DLXInstruction.BSR);
        F1ops.add(DLXInstruction.WRL);
        
        F3ops.add(DLXInstruction.JSR);
    }
 
    // Sets the op, a, b, c from the instruction
    public void getInstructionParameters(int instruction) { 
        int opNum = instruction >>> 26;
                        
        if (F1ops.contains(opNum)) {
            format = Format.F1;
            op = opNum;
            // Getting a, b, c
            a = (instruction & 0x3FFFFFF) >>> 21;
            b = (instruction & 0x1FFFFF) >>> 16;
            c = instruction & 0xFFFF;
            
        } else if (F2ops.contains(opNum)) {
            format = Format.F2;
            op = opNum;
            
            // Getting a, b, c
            a = (instruction & 0x3FFFFFF) >>> 21;
            b = (instruction & 0x1FFFFF) >>> 16;
            c = instruction & 0x1F;
        } else if (F3ops.contains(opNum)) {
            format = Format.F3;
            op = opNum;
            
            // No a or b so setting to min
            a = Integer.MIN_VALUE;
            b = Integer.MIN_VALUE;
            
            // Getting c
            c = instruction & 0x3FFFFFF;
        } else {
            System.out.println("ERROR : Invalid op code");
            op = -1;
        }
    }
    
    public void printInstruction(int instruction) {
        getInstructionParameters(instruction);
        
        StringBuilder stb = new StringBuilder();
        stb.append(mnemonics[op]);
        while (stb.length() < 6) {
            stb.append(" ");
        }
        
        switch(op) {
            case DLXInstruction.WRL:
                break;
            case DLXInstruction.RDD:
                stb.append(a);
                break;
            case DLXInstruction.WRD:
            case DLXInstruction.WRH:
                stb.append(b);
                break;
            case DLXInstruction.BSR:
            case DLXInstruction.JSR:
            case DLXInstruction.RET:
                stb.append(c);
                break;
            case DLXInstruction.BEQ:
            case DLXInstruction.BNE:
            case DLXInstruction.BLT:
            case DLXInstruction.BGE:
            case DLXInstruction.BLE:
            case DLXInstruction.BGT:
            case DLXInstruction.CHK:
            case DLXInstruction.CHKI:
                stb.append(a + " " + c);
                break;
            case DLXInstruction.ADD:
            case DLXInstruction.ADDI:
            case DLXInstruction.SUB:
            case DLXInstruction.SUBI:
            case DLXInstruction.MUL:
            case DLXInstruction.MULI:
            case DLXInstruction.DIV:
            case DLXInstruction.DIVI:
            case DLXInstruction.MOD:
            case DLXInstruction.MODI:
            case DLXInstruction.CMP:
            case DLXInstruction.CMPI:
            case DLXInstruction.OR:
            case DLXInstruction.ORI:
            case DLXInstruction.AND:
            case DLXInstruction.ANDI:
            case DLXInstruction.BIC:
            case DLXInstruction.BICI:
            case DLXInstruction.XOR:
            case DLXInstruction.XORI:
            case DLXInstruction.LSH:
            case DLXInstruction.LSHI:
            case DLXInstruction.ASH:
            case DLXInstruction.ASHI:
            case DLXInstruction.LDW:
            case DLXInstruction.LDX:
            case DLXInstruction.POP:
            case DLXInstruction.STW:
            case DLXInstruction.STX:
            case DLXInstruction.PSH:
                stb.append(a + " " + b + " " + c);
               break;
            default:
                System.out.println("ERROR : Invalid opCode");
        }
        
        System.out.println(stb.toString());
        return;
    }
    
    /*
     * The op has to be WRL
     */
    public int makeInstruction(int op) {
        if (op != DLXInstruction.WRL){
            System.out.println("ERROR: NO operand and opcode not WRL");
            return -1;
        }
        
        return makeF1Instruction(op, 0, 0, 0);   
    }
    
    /*
     * For ops : RDD, WRD, WRH, BSR, JSR, RET
     */
    public int makeInstruction(int op, int arg) {
        if (op == DLXInstruction.RDD) {
            return makeF2Instruction(op, arg, 0, 0);
        } else if (op == DLXInstruction.WRD || op == DLXInstruction.WRH) {
            return makeF2Instruction(op, 0, arg, 0);
        } else if (op == DLXInstruction.BSR) {
            return makeF1Instruction(op, 0, 0, arg);
        } else if (op == DLXInstruction.JSR) {
            return makeF3Instruction(op, arg);           
        } else if (op == DLXInstruction.RET) {
            return makeF2Instruction(op, 0, 0, arg);            
        } else {
            System.out.println("ERROR : Op code not matching any one operand opcode");
            return -1;
        }
    }
    
    /*
     * For ops : BEQ, BNE, BLT, BGE, BLE, BGT, CHK, CHKI
     */
    public int makeInstruction(int op, int a, int c) {
        if (op == DLXInstruction.CHKI || op == DLXInstruction.BEQ || op == DLXInstruction.BNE || 
                op == DLXInstruction.BLT || op == DLXInstruction.BGE || op == DLXInstruction.BLE || 
                op == DLXInstruction.BGT) {
            return makeF1Instruction(op, a, 0, c);
        } else if (op == DLXInstruction.CHK) {
            return makeF2Instruction(op, a, 0, c);
        } else {
            System.out.println("ERROR: Op code does not match any two operand opcode");
            return -1;
        }
    }
    
    /*
     * For all other ops
     */
    public int makeInstruction(int op, int a, int b, int c) {
        if (op == DLXInstruction.ADD || op == DLXInstruction.SUB || op == DLXInstruction.MUL || 
                op == DLXInstruction.DIV || op == DLXInstruction.MOD || op == DLXInstruction.CMP || 
                op == DLXInstruction.OR || op == DLXInstruction.AND || op == DLXInstruction.BIC || 
                op == DLXInstruction.XOR || op == DLXInstruction.LSH || op == DLXInstruction.ASH || 
                op == DLXInstruction.LDX || op == DLXInstruction.STX) {
            return makeF2Instruction(op, a, b, c);
        } else if (op == DLXInstruction.ADDI || op == DLXInstruction.SUBI || op == DLXInstruction.MULI || 
                op == DLXInstruction.DIVI || op == DLXInstruction.MODI || op == DLXInstruction.CMPI || 
                op == DLXInstruction.ORI || op == DLXInstruction.ANDI || op == DLXInstruction.BICI ||
                op == DLXInstruction.XORI || op == DLXInstruction.LSHI || op == DLXInstruction.ASHI || 
                op == DLXInstruction.LDW || op == DLXInstruction.STW || op == DLXInstruction.POP || 
                op == DLXInstruction.PSH) {
            return makeF1Instruction(op, a, b, c);
        } else {
            System.out.println("ERROR: Op code does not match any three operand opcode");
            return -1;
        }
    }
    
    public int makeF1Instruction(int op, int a, int b, int c) {
        if (c < 0) {
            c = c ^ 0xFFFF0000;
        }
        if ((a & ~0x1F) != 0 || (b & ~0x1F) != 0 || (c & ~0xFFFF) != 0) {
            System.out.println("ERROR: Incorrect operands for F1");
            return -1;
        }
        return (op << 26 | a << 21 | b << 16 | c);
    }
    
    public int makeF2Instruction(int op, int a, int b, int c) {
        if ((a & ~0x1F) != 0 || (b & ~0x1F) != 0 || (c & ~0x1F) != 0) {
            System.out.println("ERROR: Incorrect operands for F2");
            return -1;
        }
        return (op << 26 | a << 21 | b << 16 | c);
    }
    
    public int makeF3Instruction(int op, int arg) {
        if ((c & ~0x3FFFFFF) != 0) {
            System.out.println("ERROR: Incorrect operands for F3");
            return -1;
        }
        return (op << 26 | c);
    }
}
