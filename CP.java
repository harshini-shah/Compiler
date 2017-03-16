import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CP {
	
	public void performCP(DominatorTreeNode root){
		HashMap<Integer, Integer> replaceWith = new HashMap<Integer, Integer>();
		performCP(root, replaceWith);
	}
	public void performCP(DominatorTreeNode root, HashMap<Integer, Integer> replaceWith){
		if(root == null)
			return;
		
		Map<Integer, Instruction> blockInstructionsSorted = new TreeMap<Integer, Instruction>(root.getBlock().instructions); 
		for(Instruction instr : blockInstructionsSorted.values()){
			if(instr.op1 != null && instr.op1.kind == Result.Kind.INSTR && replaceWith.containsKey(instr.op1.version)){
				instr.op1.version = replaceWith.get(instr.op1.version);
			}
			
			if(instr.op2 != null && instr.op2.kind == Result.Kind.INSTR && replaceWith.containsKey(instr.op2.version)){
				instr.op2.version = replaceWith.get(instr.op2.version);
			}
			
			if(instr.op3 != null && instr.op3.kind == Result.Kind.INSTR && replaceWith.containsKey(instr.op3.version)){
				instr.op3.version = replaceWith.get(instr.op3.version);
			}
			
			if(instr.isAssignmentInstruction()){
				if(instr.op1.kind == Result.Kind.INSTR){
					replaceWith.put(instr.instructionNumber, instr.op1.version);
					instr.isDeleted = true;
				}
			}
		}
		
		for(DominatorTreeNode child : root.getChildren()){
			performCP(child, replaceWith);
		}
		
		if(root.getBlock().kind == BasicBlock.Kind.WHILE){
			for(Instruction instr : root.getBlock().getPhiInstructions().values()){
				if(instr.op3 != null && instr.op3.kind == Result.Kind.INSTR){
					if(instr.op2 != null && instr.op2.kind == Result.Kind.INSTR && replaceWith.containsKey(instr.op2.version)){
						instr.op2.version = replaceWith.get(instr.op2.version);
					}
					
					if(instr.op3 != null && instr.op3.kind == Result.Kind.INSTR && replaceWith.containsKey(instr.op3.version)){
						instr.op3.version = replaceWith.get(instr.op3.version);
					}
				}
			}
		}
	}
}
