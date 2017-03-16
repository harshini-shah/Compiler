import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class RegisterAllocation {
	private RIG _interferenceGraph;
	private Map<Integer, BlockData> _blockStats;
	private Set<Integer> _whileJoinBlockIds;
	public RegisterAllocation(){
		_interferenceGraph = new RIG();
		_blockStats = new HashMap<Integer, BlockData>();
		_whileJoinBlockIds = new HashSet<Integer>();
	}
	
	public RIG getRIG(){
		return _interferenceGraph;
	}
	
	private class BlockData{
		public Set<Instruction> liveWithinBlock;
		public int visitCount;
		
		public BlockData(){
			liveWithinBlock = new HashSet<Instruction>();
			visitCount = 0;
		}
	}
	
	public void allocate(DominatorTreeNode root){
		calculateLiveRange(root, null, 1);
		calculateLiveRange(root, null, 2);
		//allocateReg();
	}
	
	private void allocateReg(){
		
	}
	
	private Set<Instruction> calculateLiveRange(DominatorTreeNode node, DominatorTreeNode parent, int pass){
		Set<Instruction> live = new HashSet<Instruction>();
		if(node == null)
			return null;
		
		if(!_blockStats.containsKey(node.getBlock().blockId)){
			_blockStats.put(node.getBlock().blockId, new BlockData());
		}
		BlockData data = _blockStats.get(node.getBlock().blockId);
		if(data.visitCount >= pass){
			live.addAll(data.liveWithinBlock);
		}else{
			data.visitCount++;
			if(data.visitCount == 2){
				//whatever is live in the loop join block must be alive in the entire loop
				for(int n : _whileJoinBlockIds){
					BlockData wJoinData = _blockStats.get(n);
					data.liveWithinBlock.addAll(wJoinData.liveWithinBlock);
				}
			}
			
			if(node.getBlock().kind == BasicBlock.Kind.WHILE){
				_whileJoinBlockIds.add(node.getBlock().blockId);
			}
			for(DominatorTreeNode child : node.getChildren()){
				//add the live members from the children to the current block
				live.addAll(calculateLiveRange(child, node, pass));
			}
			if(node.getBlock().kind == BasicBlock.Kind.WHILE){
				_whileJoinBlockIds.remove(node.getBlock().blockId);
			}
			
			List<Instruction> blockInstructionsReverse = node.getBlock().getReverseOrderedInstructions();
			for (Instruction ent : blockInstructionsReverse) {
				if(!ent.isDeleted && ent.kind != Instruction.Kind.PHI && ent.kind != Instruction.Kind.BRANCH){
					
					//i : op op1 op2
					//live = live - {i}
					live.remove(ent);
					_interferenceGraph.addNode(ent, live);
					
					//live = live + {op1}
					if(ent.op1 != null && ent.op1.kind == Result.Kind.INSTR){
						if((parent != null && parent.getBlock().instructions.containsKey(ent.op1.version)) || node.getBlock().instructions.containsKey(ent.op1.version)) 
							live.add(Instruction.allInstructions.get(ent.op1.version));
					}
					
					//live = live + {op2}
					if(ent.op2 != null && ent.op2.kind == Result.Kind.INSTR){
						if((parent != null && parent.getBlock().instructions.containsKey(ent.op2.version)) || node.getBlock().instructions.containsKey(ent.op2.version))
							live.add(Instruction.allInstructions.get(ent.op2.version));
					}
				}
			}
			data.liveWithinBlock = new HashSet<Instruction>();
			data.liveWithinBlock.addAll(live);
		}
		List<Instruction> blockInstructionsReverse = node.getBlock().getReverseOrderedInstructions();
		for (Instruction ent : blockInstructionsReverse) {
			if(!ent.isDeleted && ent.kind == Instruction.Kind.PHI){
				
				//i : op op1 op2
				//live = live - {i}
				live.remove(ent);
				_interferenceGraph.addNode(ent, live);
				
				//live = live + {op1}
				if(ent.op2 != null && ent.op2.kind == Result.Kind.INSTR){
					if((parent != null && parent.getBlock().instructions.containsKey(ent.op2.version)) || node.getBlock().instructions.containsKey(ent.op2.version))
						live.add(Instruction.allInstructions.get(ent.op2.version));
				}
				
				//live = live + {op2}
				if(ent.op3 != null && ent.op3.kind == Result.Kind.INSTR){
					if((parent != null && parent.getBlock().instructions.containsKey(ent.op3.version)) || node.getBlock().instructions.containsKey(ent.op3.version))
						live.add(Instruction.allInstructions.get(ent.op3.version));
				}
			}
		}
		return live;
	}
}
