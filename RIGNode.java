import java.util.HashSet;

public class RIGNode {
	private HashSet<RIGNode> _neighbors;
	private int _color;
	//private int _cost;
	private Instruction _instr;
	
	public RIGNode(Instruction instr){
		_neighbors = new HashSet<RIGNode>();
		_color = -1;
		//_cost = 0;
		_instr = instr;
	}
	
	public RIGNode(RIGNode other){
		this._neighbors = new HashSet<RIGNode>(other.get_neighbors());
		this._color = other.get_color();
		this._instr = new Instruction(other.get_instr());
	}
	
	public HashSet<RIGNode> get_neighbors() {
		return _neighbors;
	}
	
	public int getNeighborsSize(){
		return this._neighbors.size();
	}
	
	public void set_neighbors(HashSet<RIGNode> _neighbors) {
		this._neighbors = _neighbors;
	}
	
	public void addNeighbor(RIGNode node){
		_neighbors.add(node);
	}
	
	public void removeNeighbor(RIGNode node){
		if(_neighbors.contains(node))
			_neighbors.remove(node);
	}

	public int get_color() {
		return _color;
	}

	public void set_color(int _color) {
		this._color = _color;
	}

//	public int get_cost() {
//		return _cost;
//	}
//
//	public void set_cost(int _cost) {
//		this._cost = _cost;
//	}
//
//	public void increaseCost(){
//		this._cost++;
//	}
//	
	public Instruction get_instr() {
		return _instr;
	}

	public void set_instr(Instruction _instr) {
		this._instr = _instr;
	}
}
