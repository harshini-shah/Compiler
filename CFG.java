import java.util.ArrayList;
import java.util.List;

public class CFG {
    public String functionName;
    public List<Identifier> argumentList;
    public BasicBlock startBlock;
    public String name;
    
    public CFG() {
        functionName = "MAIN";
        argumentList = new ArrayList<Identifier>();
    }
}
