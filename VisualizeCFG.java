import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class VisualizeCFG {
    private BasicBlock startBlock;
    private int num = 1;
    
    public VisualizeCFG(BasicBlock startBlock) {
        this.startBlock = startBlock;
        displayCFG(this.startBlock);
    }
    
    public void displayCFG(BasicBlock currBlock) {
        if (currBlock == null || currBlock.written) {
            return;
        }
        display(currBlock);
        currBlock.written = true;
        displayCFG(currBlock.leftBlock);
        displayCFG(currBlock.rightBlock);
        displayCFG(currBlock.joinBlock);
        return;
    }
    
    public void display(BasicBlock block) {
        if (block == null) {
            return;
        }
        
        System.out.println("BASIC BLOCK: " + num++ + "(" + block.kind + ")");
//        Set<Integer> set = block.instructions.keySet();
        Map<Integer, Instruction> blockInstructionsSorted = new TreeMap<Integer, Instruction>(block.instructions); 
        for (Map.Entry<Integer, Instruction> ent : blockInstructionsSorted.entrySet()) {
        	if(!ent.getValue().isDeleted)
        		System.out.println(ent.getKey() + "\t" + ent.getValue().toString());
        }
        System.out.println();
    }
}
