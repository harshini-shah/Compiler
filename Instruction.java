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
    public BasicBlock thisBlock;
    public static Map<Integer, Instruction> allInstructions = new HashMap<Integer, Instruction>();
    
    public Instruction() {
        kind = Kind.STD;
        op1 = null;
        op2 = null;
        op3 = null;
        regNo = -1;
        isDeleted = false;
    }
    
    public Instruction(Instruction instr) {
        this.kind = instr.kind;
        this.operation = instr.operation;
        this.op1 = instr.op1;
        this.op2 = instr.op2;
        this.op3 = instr.op3;
        this.instructionNumber = instr.instructionNumber;
        this.isDeleted = instr.isDeleted;
        this.regNo = instr.regNo;
        this.thisBlock = instr.thisBlock;
        this.allInstructions = new HashMap<Integer, Instruction>();
        this.allInstructions.putAll(instr.allInstructions);
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
        } else if (op.kind == Result.Kind.REG) {
            sb.append("R" + op.regNo);
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
