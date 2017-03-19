import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegisterAllocation {
	private RIG _interferenceGraph;
	private RIG _graphCopy;
	private Map<Integer, BlockData> _blockStats;
	private Set<Integer> _whileJoinBlockIds;
	private List<Set<Instruction>> _clusters;
	public Map<Integer, Integer> registerMapping;
	private Map<Instruction, BasicBlock> _phis;
	
	public RegisterAllocation(){
		_interferenceGraph = new RIG();
		_graphCopy = new RIG();
		_blockStats = new HashMap<Integer, BlockData>();
		_whileJoinBlockIds = new HashSet<Integer>();
		_clusters = new ArrayList<Set<Instruction>>();
		registerMapping = new HashMap<Integer, Integer>();
		_phis = new HashMap<Instruction, BasicBlock>();
	}
	
	public RIG getRIG(){
		return _graphCopy;
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
		_graphCopy.copy(_interferenceGraph);
		allocateReg();
		insertMoves();
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
		node = _graphCopy.getNode(instr);
		if(!registerMapping.containsKey(instr.instructionNumber)){
			Set<Instruction> cluster = getCluster(instr);
			if(cluster != null){
				int color = colorNode(node, instr, -1);
				registerMapping.put(instr.instructionNumber, instr.regNo);
				if(color != -1){
					for(Instruction mem : cluster){
						if(!registerMapping.containsKey(mem.instructionNumber)){
							RIGNode n = _graphCopy.getNode(mem);
							int memColor = colorNode(n, mem, color);
							if (memColor == color){
								registerMapping.put(mem.instructionNumber, mem.regNo);
							}
						}
					}
				}
				
			}else if(cluster == null){
				int color = colorNode(node, instr, -1);
				registerMapping.put(instr.instructionNumber, instr.regNo);
				
				if(color == -1){
					System.out.println("Spilled Instruction" + instr.toString());
				}
			}
		}
	}
	
	private void insertMoves(){
		for(Map.Entry<Instruction, BasicBlock> entry : _phis.entrySet()){
			Instruction instr2 = null;
			Instruction instr3 = null;
			if(entry.getKey().op2 != null && entry.getKey().op2.kind == Result.Kind.INSTR)
				instr2 = Instruction.allInstructions.get(entry.getKey().op2.version);
			if(entry.getKey().op3 != null && entry.getKey().op3.kind == Result.Kind.INSTR)
				instr3 = Instruction.allInstructions.get(entry.getKey().op3.version);
			if(instr2 != null && instr2.regNo != entry.getKey().regNo){
				generateMoveInstruction(entry.getKey().regNo, instr2.regNo, entry.getValue(), 1, false);
			}else if(instr2 == null && entry.getKey().op2.kind == Result.Kind.CONST){
				generateMoveInstruction(entry.getKey().regNo, entry.getKey().op2.value, entry.getValue(), 1, true);
			}
			
			if(instr3 != null && instr3.regNo != entry.getKey().regNo){
				generateMoveInstruction(entry.getKey().regNo, instr3.regNo, entry.getValue(), 2, false);
			}else if(instr3 == null && entry.getKey().op3.kind == Result.Kind.CONST){
				generateMoveInstruction(entry.getKey().regNo, entry.getKey().op3.value, entry.getValue(), 2, true);
			}
		}
	}
	
	private void generateMoveInstruction(int destReg, int source, BasicBlock current, int branch, boolean constant){
		Instruction instr = new Instruction();
		instr.kind = Instruction.Kind.STD;
		Result dest = new Result();
		dest.kind = Result.Kind.REG;
		dest.regNo = destReg;
		instr.operation = "MOV";
		Result s = new Result();
		if(!constant){
			s.kind = Result.Kind.REG;
			s.regNo = source;
		}else{
			s.kind = Result.Kind.CONST;
			s.value = source;
		}
		instr.op1 = s;
		instr.op2 = dest;
		instr.instructionNumber = Parser._lineNum;
		instr.regNo = destReg;
		if(branch == 1){
			instr.thisBlock = current.leftParent;
			current.leftParent.instructions.put(Parser._lineNum++, instr);
		}else if(branch == 2){
			instr.thisBlock = current.rightParent;
			current.rightParent.instructions.put(Parser._lineNum++, instr);
		}
	}
	
	private int colorNode(RIGNode node, Instruction instr, int color){
		Set<Integer> colorsN = new HashSet<Integer>();
		for(RIGNode ne : node.get_neighbors()){
			colorsN.add(ne.get_instr().regNo);
		}
		if(color == -1){
			color = 1;
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
		}else{
			if(!colorsN.contains(color)){
				instr.regNo = color;
			}else{
				color = -1;
			}
		}
		return color;
	}
	
	private Set<Instruction> getCluster(Instruction instr){
		for(Set<Instruction> mem : _clusters){
			if(mem.contains(instr)){
				return mem;
			}
		}
		return null;
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
				if(!_phis.containsKey(ent)){
					_phis.put(ent, node);
				}
				boolean existsInCluster = false;
				for(Set<Instruction> mem : _clusters){
					if(mem.contains(ent) || 
							(ent.op2!= null && ent.op2.kind == Result.Kind.INSTR && mem.contains(Instruction.allInstructions.get(ent.op2.version))) ||
							(ent.op3!= null && ent.op3.kind == Result.Kind.INSTR && mem.contains(Instruction.allInstructions.get(ent.op3.version)))){
						mem.add(ent);
						existsInCluster = true;
						if(ent.op2!= null && ent.op2.kind == Result.Kind.INSTR){
							mem.add(Instruction.allInstructions.get(ent.op2.version));
						}
						if(ent.op3 != null && ent.op3.kind == Result.Kind.INSTR){
							mem.add(Instruction.allInstructions.get(ent.op3.version));
						}
					}
				}
				if(!existsInCluster){
					Set<Instruction> newCluster = new HashSet<Instruction>();
					newCluster.add(ent);
					if(ent.op2!= null && ent.op2.kind == Result.Kind.INSTR){
						newCluster.add(Instruction.allInstructions.get(ent.op2.version));
					}
					if(ent.op3 != null && ent.op3.kind == Result.Kind.INSTR){
						newCluster.add(Instruction.allInstructions.get(ent.op3.version));
					}
					_clusters.add(newCluster);
				}
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
