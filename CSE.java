import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CSE {
	
	public void performCSE(DominatorTreeNode root){
		HashMap<Integer, Integer> replaceWith = new HashMap<Integer, Integer>();
		HashMap<String, HashMap<ExpressionOperands, Integer>> anchor = new HashMap<String, HashMap<ExpressionOperands, Integer>>();
		performCSE(root, replaceWith, anchor);
	}
	
	public void performCSE(DominatorTreeNode root, HashMap<Integer, Integer> replaceWith, HashMap<String, HashMap<ExpressionOperands, Integer>> anchor){
		if(root == null)
			return;
		List<Instruction> blockInstructionsSorted = root.getBlock().getOrderedInstructions();
		for(Instruction instr : blockInstructionsSorted){
			if(!instr.isDeleted){
				if(instr.op1 != null && instr.op1.kind == Result.Kind.INSTR && replaceWith.containsKey(instr.op1.version)){
					instr.op1.version = replaceWith.get(instr.op1.version);
				}
				
				if(instr.op2 != null && instr.op2.kind == Result.Kind.INSTR && replaceWith.containsKey(instr.op2.version)){
					instr.op2.version = replaceWith.get(instr.op2.version);
				}
				
				if(instr.op3 != null && instr.op3.kind == Result.Kind.INSTR && replaceWith.containsKey(instr.op3.version)){
					instr.op3.version = replaceWith.get(instr.op3.version);
				}
				
				if(instr.kind == Instruction.Kind.STD && instr.isExpression()){
					ExpressionOperands eop = new ExpressionOperands(instr.op1, instr.op2);
					if(instr.isCommutativeExpression())
						eop.setCommutative(true);
					if(anchor.containsKey(instr.operation)){
						HashMap<ExpressionOperands, Integer> searchDS = anchor.get(instr.operation);
						if(searchDS.containsKey(eop)){
							instr.isDeleted = true;
							replaceWith.put(instr.instructionNumber, searchDS.get(eop));
						}else{
							searchDS.put(eop, instr.instructionNumber);
						}
					}else{
						HashMap<ExpressionOperands, Integer> searchDS = new HashMap<ExpressionOperands,Integer>();
						searchDS.put(eop, instr.instructionNumber);
						anchor.put(instr.operation, searchDS);
					}
				}
			}
		}
		
		
		for(DominatorTreeNode child : root.getChildren()){
			HashMap<String, HashMap<ExpressionOperands, Integer>> anch = new HashMap<String, HashMap<ExpressionOperands, Integer>>();
			populate(anch, anchor);
			performCSE(child, replaceWith, anch);
			
		}
	}
	
	void populate(Map<String, HashMap<ExpressionOperands, Integer>> anch, Map<String, HashMap<ExpressionOperands, Integer>> anchor){
		for (String str : anchor.keySet()) {
			anch.put(str, new HashMap<ExpressionOperands, Integer>(anchor.get(str)));
		}
	}
}
