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
	
	public void addNode(Instruction instr, RIGNode node){
		_rig.put(instr, node);
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
	
	public Instruction getNodeMaxDegree(){
		Instruction inst = new Instruction();
		int index = 0;
		for(Instruction instr : _rig.keySet()){
			if(index == 0){
				inst = instr;
			}
			if(getDegree(instr) <= 8){
				return instr;
			}
			index++;
		}
		return inst;
	}
	
	public Set<RIGNode> getNeighbors(Instruction instr){
		if(_rig.containsKey(instr))
			return _rig.get(instr).get_neighbors();
		return null;
	}
	
	public boolean isEmpty(){
		return _rig.size() == 0;
	}
	
	public RIGNode removeNode(Instruction instr){
		RIGNode node = _rig.get(instr);
		_rig.remove(instr);
		
		for(RIGNode neighbor : node.get_neighbors()){
			neighbor.removeNeighbor(node);
		}
		return node;
	}
	
	public boolean isEdge(Instruction instr1, Instruction instr2){
		RIGNode node1 = _rig.get(instr1);
		RIGNode node2 = _rig.get(instr2);
		return node1.get_neighbors().contains(node2) && node2.get_neighbors().contains(node1);
	}
}
