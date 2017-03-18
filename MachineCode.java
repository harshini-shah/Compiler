import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MachineCode {
    
    public int[] buf;
    private int pc;
    private static Map<String, Integer> f2code;
    private static Map<String, Integer> f1code;
    private static Map<String, Integer> f3code;

    
    public MachineCode() {
        // The + 1000 for the saving and restoring registers and variables etc
        f2code = new HashMap<String, Integer>();
        f1code = new HashMap<String, Integer>();
        f3code = new HashMap<String, Integer>();

        populateIR2Code();
        buf = new int[Instruction.allInstructions.size() + 1000];
        pc = 0;
    }
    
    private void populateIR2Code() {
        // Not encoded : BSR & WRH coz don't think will ever use it
        
        // Instructions that will be there in "Instruction" class but not sure what to map to
        f2code.put("ADD", DLXInstruction.ADD);
        f2code.put("SUB", DLXInstruction.SUB);
        f2code.put("MUL", DLXInstruction.MUL);
        f2code.put("DIV", DLXInstruction.DIV);
        f2code.put("MOD", DLXInstruction.MOD);
        f2code.put("CMP", DLXInstruction.CMP);
        f2code.put("OR", DLXInstruction.OR);
        f2code.put("AND", DLXInstruction.AND);
        f2code.put("BIC", DLXInstruction.BIC);
        f2code.put("XOR", DLXInstruction.XOR);
        f2code.put("LSH", DLXInstruction.LSH);
        f2code.put("ASH", DLXInstruction.ASH);
        f2code.put("CHK", DLXInstruction.CHK);
        
        f1code.put("ADD", DLXInstruction.ADDI);
        f1code.put("SUB", DLXInstruction.SUBI);
        f1code.put("MUL", DLXInstruction.MULI);
        f1code.put("DIV", DLXInstruction.DIVI);
        f1code.put("MOD", DLXInstruction.MODI);
        f1code.put("CMP", DLXInstruction.CMPI);
        f1code.put("OR", DLXInstruction.ORI);
        f1code.put("AND", DLXInstruction.ANDI);
        f1code.put("BIC", DLXInstruction.BICI);
        f1code.put("XOR", DLXInstruction.XORI);
        f1code.put("LSH", DLXInstruction.LSHI);
        f1code.put("ASH", DLXInstruction.ASHI);
        f1code.put("CHK", DLXInstruction.CHKI);
        
        f3code.put("BRA", DLXInstruction.JSR);
        f2code.put("RET", DLXInstruction.RET);
        
        f1code.put("BEQ", DLXInstruction.BEQ);
        f1code.put("BNE", DLXInstruction.BNE);
        f1code.put("BLT", DLXInstruction.BLT);
        f1code.put("BGE", DLXInstruction.BGE);
        f1code.put("BLE", DLXInstruction.BLE);
        f1code.put("BGT", DLXInstruction.BGT);
        
        f2code.put("READ", DLXInstruction.RDD);
        f2code.put("WRITE", DLXInstruction.WRD);
        
        f1code.put("WRITE NEW LINE", DLXInstruction.WRL);
    }
    
    /*
     * Start with the main function - for each instruction, encode it depending on the
     * format of the function (first depending on the operands and the instruction name, 
     * decide which command will be required. 
     * - ignore all phi instructions
     * - instead of the EOF instruction, encode "RET 0"
     * - whenever there is a call to a function, save all the registers that are currently being
     * used to memory, and then restore them after the function call - the instruction will
     * be a JSR
     * - will have to store the pcs for the call instructions because these will have to be
     * corrected once the EOF for the first function is done and subsequent functions are 
     * encoded. (probably will have to use Fixup)
     * - same thing will probably apply to branches
     * 
     * Things to figure out:
     * - there should not be any move instructions apart from moving constants, right?
     * - what to do for constants being assigned to variables
     * - need to know which registers to store and restore - or should just do all?
     * - arrays?
     * - how to reference the frame pointer? is it what R30 points to?
     */
    public void generateCode() {
        Map<Integer, Instruction> allInstructions = new TreeMap<Integer, Instruction>(FinalInstructions.finalInstructions);
        
        for (Instruction inst : allInstructions.values()) {
            
        }
        
        
        
        // Assuming each op of each line is either a constant or a register
        
        
    }
}
