import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicBlock {
    public Map<String, List<Integer>> variables;
    public BasicBlock joinBlock;
    public BasicBlock leftBlock;
    public BasicBlock rightBlock;
    public Map<Integer, Instruction> instructions;
    boolean written;
    
    public enum Kind {STD, IF, ELSE, WHILE, JOIN, FOLLOW, DO}
    
    Kind kind;
    
    public BasicBlock() {
        kind = Kind.STD;
        leftBlock = null;
        rightBlock = null;
        joinBlock = null;
        variables = new HashMap<String, List<Integer>>();
        instructions = new HashMap<Integer, Instruction>();
        written = false;
    }
}
