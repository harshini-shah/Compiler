import java.util.ArrayList;

public class DominatorTreeNode {
	private BasicBlock _block;
	private ArrayList<DominatorTreeNode> _children;
	
	DominatorTreeNode(BasicBlock block){
		this._block = block;
		this._children = new ArrayList<DominatorTreeNode>();
	}

	public BasicBlock getBlock() {
		return _block;
	}

	public void setBlock(BasicBlock block) {
		this._block = block;
	}

	public ArrayList<DominatorTreeNode> getChildren() {
		return _children;
	}

	public void setChildren(ArrayList<DominatorTreeNode> children) {
		this._children = children;
	}
	
	public void addChild(DominatorTreeNode child){
		if(child != null)
			this._children.add(child);
	}
}
