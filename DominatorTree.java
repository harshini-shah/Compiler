import java.util.HashSet;

public class DominatorTree {
	public DominatorTreeNode root;
	
	public DominatorTree(CFG cfg){
		root = new DominatorTreeNode(cfg.startBlock);
	}
	
	public void constructDT(){
		HashSet<BasicBlock> visited = new HashSet<BasicBlock>();
		visited.add(root.getBlock());
		constructDT(root, visited);
	}
	
	public void constructDT(DominatorTreeNode root, HashSet<BasicBlock> visited){
		if(root == null)
			return;
		if(root.getBlock().leftBlock != null && !visited.contains(root.getBlock().leftBlock)){
			DominatorTreeNode leftChild = new DominatorTreeNode(root.getBlock().leftBlock);
			root.addChild(leftChild);
			visited.add(root.getBlock().leftBlock);
		}
		if(root.getBlock().rightBlock != null && !visited.contains(root.getBlock().rightBlock)){
			DominatorTreeNode rightChild = new DominatorTreeNode(root.getBlock().rightBlock);
			root.addChild(rightChild);
			visited.add(root.getBlock().rightBlock);
		}
		if(root.getBlock().joinBlock != null && !visited.contains(root.getBlock().joinBlock)){
			DominatorTreeNode joinChild = new DominatorTreeNode(root.getBlock().joinBlock);
			root.addChild(joinChild);
			visited.add(root.getBlock().joinBlock);
		}
		
			
		for(DominatorTreeNode child : root.getChildren()){
			constructDT(child, visited);
		}
	}
}
