import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class VCGPrinter {
    private PrintWriter writer;
    private String outputName;
    
    public VCGPrinter(){
        
    }
    
    public void setOutputName(String outputName){
    	this.outputName  = outputName;
    }
    
    public void init(){
    	try{
            writer = new PrintWriter(new FileWriter("vcg/" + outputName + ".vcg"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void printCFG(CFG cfg) {
        writer.println("graph: { title: \"Control Flow Graph\"");
        writer.println("layoutalgorithm: dfs");
        writer.println("manhattan_edges: yes");
        writer.println("smanhattan_edges: yes");
        printCFGNode(cfg.startBlock);
        writer.println("}");
        writer.close();
    }

    public void printDominatorTree(DominatorTreeNode root){
        writer.println("graph: { title: \"Dominator Tree\"");
        writer.println("layoutalgorithm: dfs");
        writer.println("manhattan_edges: yes");
        writer.println("smanhattan_edges: yes");

        printDominatorTreeUtil(root);

        writer.println("}");
        writer.close();
    }


    public void printRIG(RIG interferenceGraph){
        writer.println("graph: { title: \"Interference Graph\"");
        writer.println("layoutalgorithm: dfs");
        writer.println("manhattan_edges: yes");
        writer.println("smanhattan_edges: yes");

        Set<Instruction> printed = new HashSet<Instruction>();
        for (Instruction a : interferenceGraph.getNodes()) {
            printed.add(a);

            writer.println("node: {");
            writer.println("title: \"" + a.instructionNumber + "\"");
            writer.println("label: \"" + "[ REG: " + interferenceGraph.getNode(a).get_instr().regNo + " ::: " + a.instructionNumber + " : " + a.toString() + "]\"");
            //writer.println("label: \"" +a.instructionNumber+ " [" +  a.toString() + "]\"");
            writer.println("}");

            for (RIGNode b : interferenceGraph.getNeighbors(a)) {
                if (!printed.contains(b.get_instr())) {
                	printEdge(a.instructionNumber, b.get_instr().instructionNumber);
                }
            }
        }
        writer.println("}");
        writer.close();
    }
    
    private void printDominatorTreeUtil(DominatorTreeNode root){
        if(root == null)
            return;
        printDTNode(root);
        for(DominatorTreeNode child : root.getChildren())
        	printDominatorTreeUtil(child);
    }

    private void printCFGNode(BasicBlock block) {
    	if(block == null || block.written)
    		return;
    	block.written = true;
        writer.println("node: {");
        writer.println("title: \"" + block.blockId + "\"");
        writer.println("label: \"" + block.blockId + "[");
        List<Instruction> blockInstructionsSorted = block.getOrderedInstructions();
        for (Instruction instr : blockInstructionsSorted) {
        	if(!instr.isDeleted)
        		writer.println(instr.instructionNumber + "\t" + instr.toString());
        	else if(instr.isDeleted)
        		writer.println("DELETED "+instr.instructionNumber + "\t" + instr.toString());
        }
        writer.println("]\"");
        writer.println("}");

        if(block.leftBlock != null) {
            printEdge(block.blockId, block.leftBlock.blockId);
        }

        if(block.rightBlock != null) {
            printEdge(block.blockId, block.rightBlock.blockId);
        }
        
        printCFGNode(block.leftBlock);
        printCFGNode(block.rightBlock);
        
        //join block
    }

    private void printDTNode(DominatorTreeNode node) {
        writer.println("node: {");
        writer.println("title: \"" + node.getBlock().blockId + "\"");
        writer.println("label: \"" + node.getBlock().blockId + "[");
        List<Instruction> blockInstructionsSorted = node.getBlock().getOrderedInstructions();
        for (Instruction instr : blockInstructionsSorted) {
        	if(!instr.isDeleted)
        		writer.println(instr.instructionNumber + "\t" + instr.toString());
        	else if(instr.isDeleted)
        		writer.println("DELETED "+instr.instructionNumber + "\t" + instr.toString());
        }
        writer.println("]\"");
        writer.println("}");

        for(DominatorTreeNode child : node.getChildren()){
            printEdge(node.getBlock().blockId, child.getBlock().blockId);
        }
    }

    public void printEdge(int sourceId, int targetId){
        writer.println("edge: { sourcename: \"" + sourceId + "\"");
        writer.println("targetname: \"" + targetId + "\"");
        writer.println("color: blue");
        writer.println("}");
    }

    public void printInstruction(Instruction instruction){
        writer.println(instruction.toString());
    }
}