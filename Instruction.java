
public class Instruction {
    public enum Kind {BRANCH, STD, END};
    
    public Kind kind;
    public String operation;
    public Parser.Result op1;
    public Parser.Result op2;
    public Parser.Result op3;
    int instructionNumber;
    
    public Instruction() {
        kind = Kind.STD;
        op1 = null;
        op2 = null;
        op3 = null;
    }
    
    @ Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(operation);
        
        if (kind == Kind.END) {
            return sb.toString();
        }
        
        while (sb.length() < 6) {
            sb.append(" ");
        }
        if (kind == Kind.BRANCH) {
            sb.append("(" + op1.fixupLocation + ")");
            return sb.toString();
        }
        
        if (op1 != null) {
            sb.append(display(op1));
        }
        
        if (op2 != null) {
            sb.append(", ");
            sb.append(display(op2));
        }
        
        if (op3 != null) {
            sb.append(", ");
            sb.append(display(op3));
        }
        
        return sb.toString();
    }
    
    public String display(Parser.Result op) {
        StringBuilder sb = new StringBuilder();
        if (op.kind == Parser.Kind.CONST) {
            sb.append("#" + op.value);
        } else if (op.kind == Parser.Kind.INSTR) {
            sb.append("(" + op.instructionNum + ")");
        } else if (op.kind == Parser.Kind.VAR) {
            sb.append(op.name + "_" + instructionNumber);
        }
        
        return sb.toString();
    }
}
