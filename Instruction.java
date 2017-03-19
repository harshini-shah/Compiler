import java.util.HashMap;
import java.util.Map;

public class Instruction {
    public enum Kind {
        BRANCH, STD, END, PHI, FUNC
    };

    public Kind kind;
    public String operation;
    public Result op1;
    public Result op2;
    public Result op3;
    public int instructionNumber;
    public boolean isDeleted;
    public int regNo;
    public static Map<Integer, Instruction> allInstructions = new HashMap<Integer, Instruction>();
    
    public Instruction() {
        kind = Kind.STD;
        op1 = null;
        op2 = null;
        op3 = null;
        regNo = -1;
        isDeleted = false;
    }

    @ Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(operation);

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

    public String display(Result op) {
        StringBuilder sb = new StringBuilder();
        if (op.kind == Result.Kind.CONST) {
            sb.append("#" + op.value);
        } else if (op.kind == Result.Kind.INSTR) {
            sb.append("(" + op.version + ")");
        } else if (op.kind == Result.Kind.VAR) {
            sb.append(op.name + "_" + instructionNumber);
        }

        return sb.toString();
    }
    
    public boolean isExpression(){
    	return isCommutativeExpression() || this.operation.equals("SUB") || this.operation.equals("DIV");
    }
    
    public boolean isCommutativeExpression(){
    	return this.operation.equals("ADD") || this.operation.equals("MUL");
    }
    
    public boolean isAssignmentInstruction(){
    	return this.operation.equals("MOV");
    }
}
