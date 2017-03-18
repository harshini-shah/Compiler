import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class BasicBlock {
    public Map<String, List<Integer>> variables;
    public int blockId;
    public BasicBlock joinBlock;
    public BasicBlock leftBlock;
    public BasicBlock rightBlock;
    public BasicBlock joinParent;
    public BasicBlock leftParent;
    public BasicBlock rightParent;
    public Map<Integer, Instruction> instructions;
    boolean written;
    public Set<String> arrNames;
    public int id;
    
    public enum Kind {STD, IF, ELSE, WHILE, JOIN, FOLLOW, DO}
    
    Kind kind;
    
    public BasicBlock(int blockNum) {
        kind = Kind.STD;
        leftBlock = null;
        rightBlock = null;
        joinBlock = null;
        leftParent = null;
        rightParent = null;
        joinParent = null;
        variables = new HashMap<String, List<Integer>>();
        instructions = new HashMap<Integer, Instruction>();
        written = false;
        arrNames = new HashSet<String>();
        blockId =blockNum;
    }
    
    public Map<Integer, Instruction> getPhiInstructions(){
        Map<Integer, Instruction> phiInstructions = new HashMap<Integer, Instruction>();
        for (Map.Entry<Integer, Instruction> ent : instructions.entrySet()) {
            if (ent.getValue().kind == Instruction.Kind.PHI) {
                phiInstructions.put(ent.getKey(),  ent.getValue());
            }
        }
        return phiInstructions;
    }
    
    public List<Instruction> getOrderedInstructions(){
    	List<Instruction> orderedInstructions = new ArrayList<Instruction>();
    	Map<Integer, Instruction> blockInstructionsSorted = new TreeMap<Integer, Instruction>(instructions); 
    	for (Instruction instr: blockInstructionsSorted.values()) {
            if (instr.kind == Instruction.Kind.PHI) {
            	orderedInstructions.add(instr);
            }
        }
    	for(Instruction instr : blockInstructionsSorted.values()){
    		if(instr.kind != Instruction.Kind.PHI){
    			orderedInstructions.add(instr);
    		}
    	}
    	return orderedInstructions;
    }
    
    public List<Instruction> getReverseOrderedInstructions(){
    	List<Instruction> orderedInstructions = new ArrayList<Instruction>();
    	Map<Integer, Instruction> blockInstructionsReverse = new TreeMap<Integer,Instruction>(Collections.reverseOrder());
    	blockInstructionsReverse.putAll(instructions);
    	for(Instruction instr : blockInstructionsReverse.values()){
    		if(instr.kind != Instruction.Kind.PHI){
    			orderedInstructions.add(instr);
    		}
    	}
    	for (Instruction instr: blockInstructionsReverse.values()) {
            if (instr.kind == Instruction.Kind.PHI) {
            	orderedInstructions.add(instr);
            }
        }
    	
    	return orderedInstructions;
    }
}
