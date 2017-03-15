import java.util.ArrayList;
import java.util.List;

public class CFG {
    public BasicBlock startBlock;
    public String functionName;
    public List<Identifier> argumentList;
    
    public CFG() {
        this.startBlock = new BasicBlock();
        functionName = "MAIN";
        argumentList = new ArrayList<Identifier>();
    }
}
