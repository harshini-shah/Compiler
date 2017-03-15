import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BasicBlock {
    public Map<String, List<Integer>> variables;
    public BasicBlock joinBlock;
    public BasicBlock leftBlock;
    public BasicBlock rightBlock;
    public BasicBlock joinParent;
    public BasicBlock leftParent;
    public BasicBlock rightParent;
    public Map<Integer, Instruction> instructions;
    boolean written;
    public Set<String> arrNames;
    
    public enum Kind {STD, IF, ELSE, WHILE, JOIN, FOLLOW, DO}
    
    Kind kind;
    
    public BasicBlock() {
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
    }
    
    public Map<Integer, Instruction> getPhiInstructions(){
        Map<Integer, Instruction> phiInstructions = new HashMap<Integer, Instruction>();
        for (int i : instructions.keySet()) {
            if (instructions.get(i).kind == Instruction.Kind.PHI) {
                phiInstructions.put(i,  instructions.get(i));
            }
        }
        return phiInstructions;
    }
}
