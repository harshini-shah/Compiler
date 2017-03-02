import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicBlock {
    public Map<String, List<Integer>> variables;
    public BasicBlock joinBlock;
    public BasicBlock leftBlock;
    public BasicBlock rightBlock;
    public Map<Integer, Instruction> instructions;
    
    public BasicBlock() {
        leftBlock = null;
        rightBlock = null;
        joinBlock = null;
        variables = new HashMap<String, List<Integer>>();
        instructions = new HashMap<Integer, Instruction>();
    }
}
