import java.util.ArrayList;
import java.util.List;

public class CFG {
    public String functionName;
    public List<Identifier> argumentList;
    public static BasicBlock startBlock;
    public String name;
    
    public CFG() {
        this.startBlock = new BasicBlock();
        functionName = "MAIN";
        argumentList = new ArrayList<Identifier>();
    }
}
