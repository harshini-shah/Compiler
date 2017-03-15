
public class DLXMachine {
    
    // state variables
    static int[] R = new int[32];   // The value of R0 is always 0; R31 stores the return address 
    static int pc, op, a, b, c, format;
    
    // Assuming 1000 4-byte (32 bit) words are available in memory
    static int[] memory = new int[1000];
    
    private void load(int[] program) {
        int i = 0;
        for (i = 0; i < program.length; i++) {
            memory[i] = program[i];
        }
    }
    
    private void execute() {
        
        for (int i = 0; i < R.length; i++) {
            R[i] = 0;
        }
        
        // R30 has to point to the globals section
        R[30] = 4*memory.length - 1;
        
        // Initializing program counter to zero
        pc = 0;
        
        while (true) {
            getInstructionParameters(memory[pc]);
            
            int nextpc = 0;
            if (format == 2) {
                c = R[c];
            }
            
            switch(op) {
            
            }
            
            // setting the next pc
            pc = nextpc == 0 ? pc + 1 : nextpc;
        }  
    }
    
    // Sets the op, a, b, c from the instruction
    private void getInstructionParameters(int instruction) {
        
    }
    
}
