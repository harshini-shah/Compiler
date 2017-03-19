import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CP {
	
	public void performCP(DominatorTreeNode root){
		HashMap<Integer, Instruction> replaceWith = new HashMap<Integer, Instruction>();
		performCP(root, replaceWith);
	}
	public void performCP(DominatorTreeNode root, HashMap<Integer, Instruction> replaceWith){
		if(root == null)
			return;
		
		Map<Integer, Instruction> blockInstructionsSorted = new TreeMap<Integer, Instruction>(root.getBlock().instructions); 
		for(Instruction instr : blockInstructionsSorted.values()){
			if(instr.op1 != null && instr.op1.kind == Result.Kind.INSTR && replaceWith.containsKey(instr.op1.version)){
				Instruction inst = replaceWith.get(instr.op1.version);
				if(inst.op1.kind == Result.Kind.INSTR)
					instr.op1.version = inst.op1.version;
				else if(inst.op1.kind == Result.Kind.CONST){
					instr.op1.kind = Result.Kind.CONST;
					instr.op1.value = inst.op1.value;
				}
			}
			
			if(instr.op2 != null && instr.op2.kind == Result.Kind.INSTR && replaceWith.containsKey(instr.op2.version)){
				Instruction inst = replaceWith.get(instr.op2.version);
				if(inst.op1.kind == Result.Kind.INSTR)
					instr.op2.version = inst.op1.version;
				else if(inst.op1.kind == Result.Kind.CONST){
					instr.op2.kind = Result.Kind.CONST;
					instr.op2.value = inst.op1.value;
				}
			}
			
			if(instr.op3 != null && instr.op3.kind == Result.Kind.INSTR && replaceWith.containsKey(instr.op3.version)){
				Instruction inst = replaceWith.get(instr.op3.version);
				if(inst.op1.kind == Result.Kind.INSTR)
					instr.op3.version = inst.op1.version;
				else if(inst.op1.kind == Result.Kind.CONST){
					instr.op3.kind = Result.Kind.CONST;
					instr.op3.value = inst.op1.value;
				}
			}
			
			if(instr.isAssignmentInstruction()){
				if(instr.op1.kind == Result.Kind.INSTR){
					replaceWith.put(instr.instructionNumber, instr);
					instr.isDeleted = true;
				}else if(instr.op1.kind == Result.Kind.CONST){
					replaceWith.put(instr.instructionNumber, instr);
					instr.isDeleted = true;
				}
			}
		}
		
		for(DominatorTreeNode child : root.getChildren()){
			performCP(child, replaceWith);
		}
		
		if(root.getBlock().kind == BasicBlock.Kind.WHILE){
			for(Instruction instr : root.getBlock().getPhiInstructions().values()){
				if(instr.op2 != null && instr.op2.kind == Result.Kind.INSTR && replaceWith.containsKey(instr.op2.version)){
					Instruction inst = replaceWith.get(instr.op2.version);
					if(inst.op1.kind == Result.Kind.INSTR)
						instr.op2.version = inst.op1.version;
					else if(inst.op1.kind == Result.Kind.CONST){
						instr.op2.kind = Result.Kind.CONST;
						instr.op2.value = inst.op1.value;
					}
				}
				
				if(instr.op3 != null && instr.op3.kind == Result.Kind.INSTR && replaceWith.containsKey(instr.op3.version)){
					Instruction inst = replaceWith.get(instr.op3.version);
					if(inst.op1.kind == Result.Kind.INSTR)
						instr.op3.version = inst.op1.version;
					else if(inst.op1.kind == Result.Kind.CONST){
						instr.op3.kind = Result.Kind.CONST;
						instr.op3.value = inst.op1.value;
					}
				}
			}
		}
	}
}
