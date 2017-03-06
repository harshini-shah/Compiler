import java.util.Map;
import java.util.Set;

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
        Set<Integer> set = block.instructions.keySet();
        for (int lineNumber : set) {
            System.out.println(lineNumber + "\t" + block.instructions.get(lineNumber).toString());
        }
        System.out.println();
    }
}
