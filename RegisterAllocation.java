import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	public void allocate(BasicBlock root){
		calculateLiveRange(root, null, 1);
		calculateLiveRange(root, null, 2);
		allocateReg();
	}
	
	private void allocateReg(){
		if(_interferenceGraph.isEmpty())
			return;
		Instruction instr = _interferenceGraph.getNodeMaxDegree();
		RIGNode node = _interferenceGraph.removeNode(instr);
		if(!_interferenceGraph.isEmpty()){
			allocateReg();
		}
		
		_interferenceGraph.addNode(instr, node);
		Set<Integer> colorsN = new HashSet<Integer>();
		for(RIGNode n : node.get_neighbors()){
			colorsN.add(n.get_instr().regNo);
		}
		int color = 1;
		boolean colored = false;
		while(!colored && color < 32){
			if(!colorsN.contains(color)){
				instr.regNo = color;
				colored = true;
			}
			if(!colored){
				color++;
			}
		}
		if(!colored){
			System.out.println("Error: No color assigned to the instr" + instr.toString());
		}
	}
	
	private Set<Instruction> calculateLiveRange(BasicBlock node, BasicBlock parent, int pass){
		Set<Instruction> live = new HashSet<Instruction>();
		if(node == null)
			return null;
		
		if(!_blockStats.containsKey(node.blockId)){
			_blockStats.put(node.blockId, new BlockData());
		}
		BlockData data = _blockStats.get(node.blockId);
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
			
			if(node.kind == BasicBlock.Kind.WHILE){
				_whileJoinBlockIds.add(node.blockId);
			}
			
			if(node.leftBlock != null){
				live.addAll(calculateLiveRange(node.leftBlock, node, pass));
			}
			
			if(node.rightBlock != null){
				live.addAll(calculateLiveRange(node.rightBlock, node, pass));
			}
//			live.addAll(data.liveWithinBlock);
//			if(node.joinBlock != null){
//				for (Instruction ins : node.joinBlock.getPhiInstructions().values()){
//					live.add(ins);
//				}
//			}
			
			if(node.kind == BasicBlock.Kind.WHILE){
				_whileJoinBlockIds.remove(node.blockId);
			}
			
			List<Instruction> blockInstructionsReverse = node.getReverseOrderedInstructions();
			for (Instruction ent : blockInstructionsReverse) {
				if(!ent.isDeleted && ent.kind == Instruction.Kind.STD){
					
					//i : op op1 op2
					//live = live - {i}
					live.remove(ent);
					_interferenceGraph.addNode(ent, live);
					
					//live = live + {op1}
					if(ent.op1 != null && ent.op1.kind == Result.Kind.INSTR){
						//if((parent != null && parent.instructions.containsKey(ent.op1.version)) || node.instructions.containsKey(ent.op1.version)) 
							live.add(Instruction.allInstructions.get(ent.op1.version));
					}
					
					//live = live + {op2}
					if(ent.op2 != null && ent.op2.kind == Result.Kind.INSTR){
						//if((parent != null && parent.instructions.containsKey(ent.op2.version)) || node.instructions.containsKey(ent.op2.version))
							live.add(Instruction.allInstructions.get(ent.op2.version));
					}
				}else if(!ent.isDeleted && ent.kind == Instruction.Kind.BRANCH && ent.operation != "BRA"){
					live.remove(ent);
					_interferenceGraph.addNode(ent, live);
				}else if(!ent.isDeleted && ent.kind == Instruction.Kind.FUNC){
					//live = live + {op1}
					if(ent.op1 != null && ent.op1.kind == Result.Kind.INSTR){
						//if((parent != null && parent.instructions.containsKey(ent.op1.version)) || node.instructions.containsKey(ent.op1.version)) 
							live.add(Instruction.allInstructions.get(ent.op1.version));
					}
					
					//live = live + {op2}
					if(ent.op2 != null && ent.op2.kind == Result.Kind.INSTR){
						//if((parent != null && parent.instructions.containsKey(ent.op2.version)) || node.instructions.containsKey(ent.op2.version))
							live.add(Instruction.allInstructions.get(ent.op2.version));
					}
				}
			}
			data.liveWithinBlock = new HashSet<Instruction>();
			data.liveWithinBlock.addAll(live);
		}
		List<Instruction> blockInstructionsReverse = node.getReverseOrderedInstructions();
		int branch = 0;
		if(parent != null && node.leftParent != null && parent.blockId == node.leftParent.blockId){
			branch = 1;
		}else if(parent != null && node.rightParent != null && parent.blockId == node.rightParent.blockId){
			branch = 2;
		}
		for (Instruction ent : blockInstructionsReverse) {
			if(!ent.isDeleted && ent.kind == Instruction.Kind.PHI){
				
				//i : op op1 op2
				//live = live - {i}
				if((node.kind == BasicBlock.Kind.WHILE && branch == 1) || node.kind != BasicBlock.Kind.WHILE){
					live.remove(ent);
				}
				_interferenceGraph.addNode(ent, live);
				//live = live + {op1}
				if(branch == 1 && ent.op2 != null && ent.op2.kind == Result.Kind.INSTR){
					live.add(Instruction.allInstructions.get(ent.op2.version));
//					if((parent != null && parent.instructions.containsKey(ent.op2.version)) || node.instructions.containsKey(ent.op2.version))
//						
//					else if(node.joinParent != null && node.joinParent.instructions.containsKey(ent.op2.version)){
//						live.add(Instruction.allInstructions.get(ent.op2.version));
//					}
				}
				
				//live = live + {op2}
				if(branch == 2 && ent.op3 != null && ent.op3.kind == Result.Kind.INSTR){
					live.add(Instruction.allInstructions.get(ent.op3.version));
//					if((parent != null && parent.instructions.containsKey(ent.op3.version)) || node.instructions.containsKey(ent.op3.version))
//						live.add(Instruction.allInstructions.get(ent.op3.version));
//					else if(node.joinParent != null && node.joinParent.instructions.containsKey(ent.op3.version)){
//						live.add(Instruction.allInstructions.get(ent.op3.version));
//					}
				}
			}
		}
		return live;
	}
}
