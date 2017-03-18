import java.util.ArrayList;
import java.util.List;

public class CFG {
    public String functionName;
    public List<Identifier> argumentList;
    public BasicBlock startBlock;
    public String name;
    public List<BasicBlock> bbs;
    
    public CFG() {
        functionName = "MAIN";
        argumentList = new ArrayList<Identifier>();
        bbs = new ArrayList<BasicBlock>();
    }
}
