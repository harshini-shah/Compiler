import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RIG {
	private Map<Instruction, RIGNode> _rig;
	
	public RIG(){
		_rig = new HashMap<Instruction, RIGNode>();
	}
	
	public void addNode(Instruction instr, Set<Instruction> neighbors){
		RIGNode node;
		if(!_rig.containsKey(instr)){
			node = new RIGNode(instr);
			_rig.put(instr, node);
		}else{
			node = _rig.get(instr);
		}
		for(Instruction i : neighbors){
			RIGNode neighbor;
			if(_rig.containsKey(i)){
				neighbor = _rig.get(i);
			}else{
				neighbor = new RIGNode(i);
				_rig.put(i, neighbor);
			}
			node.addNeighbor(neighbor);
			neighbor.addNeighbor(node);
		}
	}
	
	public RIGNode getNode(Instruction instr){
		return _rig.get(instr);
	}
	public Set<Instruction> getNodes(){
		return _rig.keySet();
	}
	
	public int getDegree(Instruction instr){
		int degree = 0;
		if(_rig.containsKey(instr)){
			degree = _rig.get(instr).getNeighborsSize();
		}
		return degree;
	}
	
	public Set<RIGNode> getNeighbors(Instruction instr){
		if(_rig.containsKey(instr))
			return _rig.get(instr).get_neighbors();
		return null;
	}
}
