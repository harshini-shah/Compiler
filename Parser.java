/*
 * THINGS NOT 100% SURE OF:
 * 1. Not sure of what the symbol table does exactly
 * 2. The function blocks is now a list of CFGs - will have to change later once deal with functions
 * 3. For all the functions, the 'function (or CFG)' should also be passed as a parameter? 
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Parser {
    private Scanner scanner;
    private static int _lineNum = 1;
    public static Map<String, Identifier> symbolTable;
    public Set<Integer> relationOperators;
    public List<CFG> functionCFGs;
    
    /*
     * The parser is initialized with a file name, which is turn initializes the scanner with this file.
     * The Symbol table is a HashMap of String to Identifiers - which stores each variable, array and function
     * of the program. 
     * Every function has its own control flow graph - for now, assuming there is only the main function, one CFG
     * is initialized and its starting block is created. 
     * The main CFG is added to the list of CFGs (the size of this list = number of functions in the program)
     * then the main computation is started.
     */
    public Parser(String fileName) {
        scanner = new Scanner(fileName);
        relationOperators = new HashSet<Integer>();
        relationOperators.add(Token.eqlToken);
        relationOperators.add(Token.neqToken);
        relationOperators.add(Token.leqToken);
        relationOperators.add(Token.lssToken); 
        relationOperators.add(Token.geqToken);
        relationOperators.add(Token.gtrToken);
        symbolTable = new HashMap<String, Identifier>();
        CFG mainFunction = new CFG();
        functionCFGs = new LinkedList<CFG>();
        functionCFGs.add(mainFunction);
        computation(mainFunction.startBlock);
    }
    
    /*
     * Checks the syntax - assume for now that there are no functions.
     * The current block is sent to the variable declarations, since all declarations will be
     * in the same block only. 
     * The current block is sent to the statement sequence and the last block is gotten back from it. 
     * The EOF instruction is appended at the end of the last block on encountering the final 'period' token.
     */
    private void computation(BasicBlock currBlock) {
        scanner.next();
        while (scanner.sym != Token.eofToken) {
            if (scanner.sym == Token.mainToken) {
                scanner.next();
                while (scanner.sym == Token.varToken || scanner.sym == Token.arrToken) {
                    varDecl(currBlock);
                }
                while (scanner.sym == Token.funcToken || scanner.sym == Token.procToken) {
                    funcDecl();
                }
                if (scanner.sym == Token.beginToken) {
                    scanner.next();
                    BasicBlock finalBlock = statSequence(currBlock, null);
                    if(scanner.sym == Token.endToken) {
                        scanner.next();
                        
                        // Inserting the EOF instruction
                        Instruction instr = new Instruction();
                        instr.kind = Instruction.Kind.END;
                        instr.operation = "EOF";
                        finalBlock.instructions.put(_lineNum, instr);
                        
                        if (scanner.sym != Token.periodToken) {
                            error("Program doesn't end with a period");
                        } else {
                            scanner.next();
                        }
                    } else {
                        error("Missing } of main");
                    }
                } else {
                    error("Missing { of main");
                }
            } else {
                error("Missing 'main' in computation");
            }
        }
        
        // Printing out the IR - only temporary
        for (CFG cfg : functionCFGs) {
            VisualizeCFG test = new VisualizeCFG(cfg.startBlock);
        }
    }
    
    /*
     * Prints the appropriate error message for syntax errors
     */
    private void error(String errorMsg){
        scanner.error(errorMsg);
    }
    
    /*
     * If the token is a constant - makes a Result object, sets its type and value and returns it.
     */
    private Result number() {
        Result x = new Result();
        x.kind = Result.Kind.CONST;
        x.value = scanner.val;
        scanner.next();
        return x;
    }
    
    /*
     * Make a Result - sets the type to Var, sets its name and returns it.
     * For now, assumed there are only single variables, no arrays.
     */
    private Result ident() {
        Result x = new Result();
        x.kind = Result.Kind.VAR;
        x.name = scanner.name;
        scanner.next();
        return x;
    }
    
    /*
     * Always returns a Result of type Var - Depending on the calling function, this is unchanged (if it is
     * an assignment that calls it) or it is changed to type "INSTR" (for factor - or any other calling function).
     * For now, not handled arrays.
     */
    private Result designator(BasicBlock currBlock) {
        Result x = ident();
        if (currBlock.variables.containsKey(x.name)) {
            x.version = currBlock.variables.get(x.name).get(currBlock.variables.get(x.name).size() - 1);
        } else {
            error("Uninitialized Variable");
        }
        while (scanner.sym == Token.openbracketToken) {
            scanner.next();
            Result y = expression(currBlock);
            x.kind = Result.Kind.ARR;
            if (scanner.sym == Token.closebracketToken) {
                scanner.next();
                x.dimensions.add(y);
            } else {
                error("No ] after the expression in designator (for array)");
            }
        }
        
        if (x.kind == Result.Kind.ARR) {
            Result dummy = new Result();
            dummy.kind = Result.Kind.CONST;
            dummy.value = 4;
            
            List<Integer> dimensions = symbolTable.get(x.name).dimensions; 
            int offset = 0;
            int nDims = dimensions.size();
            
            int total = 1;
            for (int j = 1; j < nDims; j++) {
                total *= dimensions.get(j); 
            }
            
//            for (int i = 0; i < x.dimensions.size(); i++) {
//                offset += total*x.dimensions.get(i);
//                
//                
//            }
            Compute(currBlock, Token.timesToken, x.dimensions.get(0), dummy);
            generateAddressInstruction(currBlock, x, Token.plusToken);
            Compute(currBlock, Token.addaToken, x, new Result(x.dimensions.get(0)));
//            if (x.dimensions.size() == 1) {
//                Compute(currBlock, Token.timesToken, x.dimensions.get(0), dummy);
//                generateAddressInstruction(currBlock, x, Token.plusToken);
//                Compute(currBlock, Token.addaToken, x, new Result(x.dimensions.get(0)));
//            } else if (x.dimensions.size() == 2) {
//                
//            } else if (x.dimensions.size() == 3) {
//                
//            } else {
//                
//            }
            
        }
        return x;
    }
    
    private void generateAddressInstruction(BasicBlock currBlock, Result x, int op) {
        if (x.kind != Result.Kind.ARR) {
            System.out.println("ERROR : Request to generate address instruction for non-array");
            return;
        }
        
        Instruction instr = new Instruction();
        instr.instructionNumber = _lineNum;
        instr.operation = OpCode.get(op);
        instr.op1 = new Result();
        instr.op1.kind = Result.Kind.VAR;
        instr.op1.name = "FP";
        instr.op2 = new Result();
        instr.op2.kind = Result.Kind.VAR;
        instr.op2.name = x.name + "_BaseAddress";
        currBlock.instructions.put(_lineNum++, instr);
        x.kind = Result.Kind.INSTR;
        x.name = null;
        x.value = 0;
        x.version = _lineNum - 1;
    }
    
    
    /*
     * Creates a Result - there are four cases:
     * - It is a constant - then call number()
     * - It is a function call - deal with this later
     * - It is an open parenthesis - call expression()
     * - It is a designator 
     */
    private Result factor(BasicBlock currBlock) {
        Result x = new Result();
        if (scanner.sym == Token.callToken) {
            x = funcCall(currBlock);
        } else if (scanner.sym == Token.openparanToken) {
            scanner.next();
            x = expression(currBlock);
            if (scanner.sym == Token.closeparanToken) {
                scanner.next();
            } else {
                error("No closing parenthesis for expression in factor");
            }
        } else if (scanner.sym == Token.numberToken) {
            x = number();
        } else {
            x = designator(currBlock);
            x.kind = Result.Kind.INSTR;
            x.name = null;
        }
        return x;
    }
    
    /*
     * Takes two Results, generates the instruction for their multiplication/division, 
     * and returns the results of type instruction with instruction number set. (= lineNum)
     */
    private Result term(BasicBlock currBlock) {
        Result x = factor(currBlock);
        while (scanner.sym == Token.timesToken || scanner.sym == Token.divToken) {
            int op = scanner.sym;
            scanner.next();
            Result y = factor(currBlock);
            Compute(currBlock, op, x, y);
        }
        return x;
    }
    
    /*
     * Takes in two Results, and generates an instruction according to the opCode.
     * - If both are constants, no instruction is generated, the result is still of type CONST
     * and the value is modified according to the opcode.
     * - Otherwise, an instruction is generated - and the return type is a instruction with the instruction
     * line set accordingly.
     */
    private void Compute(BasicBlock currBlock, int op, Result x, Result y) {
        if (x.kind == Result.Kind.CONST && y.kind == Result.Kind.CONST && op != Token.compareToken) {
            if(op == Token.timesToken)
                x.value *= y.value;
            else if(op == Token.plusToken)
                x.value += y.value;
            else if(op == Token.minusToken)
                x.value -= y.value;
            else if(op == Token.divToken)
                x.value /= y.value;
        } else {
            Instruction instr = new Instruction();
            instr.instructionNumber = _lineNum;
            instr.operation = OpCode.get(op);
            instr.op1 = new Result(x);
            instr.op2 = y;
            currBlock.instructions.put(_lineNum++, instr);
            x.kind = Result.Kind.INSTR;
            x.name = null;
            x.value = 0;
            x.version = _lineNum - 1;
        }
    }    
    
    /*
     * Generates a compare instruction and returns a result of type CONDN and the condition set to the op
     */
    private Result relation(BasicBlock currBlock) {
        Result x = expression(currBlock);
        if (relationOperators.contains(scanner.sym)) {
            int op = scanner.sym;
            scanner.next();
            Result y = expression(currBlock);
            Compute(currBlock, Token.compareToken, x, y);
            x.kind = Result.Kind.CONDN;
            x.cond = op;
            x.fixupLocation = 0;
        } else {
            error("Relation does not have a relation Operator");
        }
        return x;
    }
    
    /* 
     * Generates an instruction, and returns a result of the type instruction
     */
    private Result expression(BasicBlock currBlock) {
        Result x = term(currBlock);
        while (scanner.sym == Token.plusToken || scanner.sym == Token.minusToken) {
            int op = scanner.sym;
            scanner.next();
            Result y = term(currBlock);
            Compute(currBlock, op, x, y);
        }
        return x;
    }
    
    /*
     * Copies the variables map - which is a map of variables to a linked list of all the versions of it
     * from block1 to block2.
     */
    private void copyVariables(BasicBlock block1, BasicBlock block2) {
        for (String str : block1.variables.keySet()) {
            block2.variables.put(str, new ArrayList<Integer>(block1.variables.get(str)));
        }
        
        for (String str : block1.arrNames) {
            block2.arrNames.add(str);
        }
    }
    
    /*
     * Generates a conditional branch instruction - the operation is the negative equivalent of  
     * the condition of x. The fixup location of x is adjusted to point to this location so that
     * the instruction number of the instruction can be changed later.
     */
    private void CondNegBraFwd(BasicBlock currBlock, Result x){
        x.fixupLocation = _lineNum;
        Instruction instr = new Instruction();
        instr.kind = Instruction.Kind.BRANCH;
        instr.instructionNumber = _lineNum;
        instr.op1 = x;
        instr.operation = OpCode.get(NegatedBranchOp.get(x.cond));
        currBlock.instructions.put(_lineNum++, instr);
    }
    
    /*
     * Generates an Unconditional branch statement - and sets the fixup location of the x to point to this
     * instruction so the instruction can be changed later
     */
    private void UnCondBraFwd(BasicBlock currBlock, Result x){
        Instruction instr = new Instruction();
        instr.kind = Instruction.Kind.BRANCH;
        instr.instructionNumber = _lineNum;
        instr.op1 = new Result(x);
        instr.operation = OpCode.get(Token.branchToken);
        currBlock.instructions.put(_lineNum++, instr);
        x.fixupLocation = _lineNum - 1;
    }
    
    /*
     * Changes the instruction in 'currBlock' which is at 'fixupLocation' which is always
     * going to be a branch instruction - to now point to the current line number
     */
    private void Fixup(BasicBlock currBlock, int fixupLocation) {
        Instruction instr = currBlock.instructions.get(fixupLocation);
        instr.op1.fixupLocation = _lineNum;
    }
    
    /*
     * The last blocks for the if and else blocks of the branching are obtained (by calling statSequence on them).
     * After getting these, 2 things are done:
     * - The phi functions for the join block are generated by comparing the version number for every variable in the 
     * 2 blocks (assumes that there are no new variables - so some version of all variables does exist in both the blocks.
     * - depending on the phi functions, the versions of the variables are updated in the joinBlock. 
     * 
     *  The child links are set as follows: 
     *  - currBlock.joinBlock --> joinBlock (always set)
     *  - currBlock.leftBlock --> ifBlock (always set)
     *  - currBlock.rightBlock --> ElseBlock (if else does exist)
     *  
     * For the finalIfBlock (same as ifBlock if no nested loops within if branch, different otherwise):
     * - finalIfBlock.rightBlock --> joinBlock (always set)
     * 
     * If the finalElseBlock exists: (same as elseBlock if no nesting within the else statements, different otherwise)
     * - finalElseBlock.leftBlock --> joinBlock
     * 
     * The parent links are set as:
     * - ifBlock.rightParent --> currBlock
     * - elseBlock.leftParent --> currBlock
     * - joinBlock.joinParent --> currBlock
     * - joinBlock.leftParent --> ifBlock
     * - joinBlock.rightParent --> elseBlock
     * 
     * In case of nesting loops, every variable that has some phi condition anywhere within any of the nested loops 
     * in the if/else blocks generate a phi function in the current join block.
     * 
     * The if, else and join blocks are added to the joinBlocks list (in that order to the head of the list) so that 
     * if this is within a nested while loop, the changes by the phi functions can be reflected in these too.
     */
    private BasicBlock ifStatement(BasicBlock currBlock, List<BasicBlock> joinBlocks){
        BasicBlock joinBlock = new BasicBlock();
        joinBlock.kind = BasicBlock.Kind.JOIN;

        currBlock.joinBlock = joinBlock;
        joinBlock.joinParent = currBlock;
        copyVariables(currBlock, joinBlock);
        
        if(scanner.sym == Token.ifToken){
            Result follow = new Result();
            follow.fixupLocation = 0;
            scanner.next();
            Result x = relation(currBlock);
            CondNegBraFwd(currBlock, x);
            
            if(scanner.sym == Token.thenToken){
                scanner.next();

                BasicBlock ifBlock = new BasicBlock();
                ifBlock.kind = BasicBlock.Kind.IF;
                currBlock.leftBlock = ifBlock;
                ifBlock.rightParent = currBlock;
                joinBlock.leftParent = ifBlock;
                copyVariables(currBlock, ifBlock);
                
                if (joinBlocks == null) {
                    joinBlocks = new ArrayList<BasicBlock>();
                }
                
                BasicBlock finalIfBlock = statSequence(ifBlock, joinBlocks);
                joinBlocks.add(0, ifBlock);
                joinBlocks.add(0, joinBlock);

                
                if(scanner.sym == Token.elseToken){
                    scanner.next();
                    BasicBlock elseBlock = new BasicBlock();
                    elseBlock.kind = BasicBlock.Kind.ELSE;
                    copyVariables(currBlock, elseBlock);
                    currBlock.rightBlock = elseBlock;
                    elseBlock.leftParent = currBlock;
                    joinBlock.rightParent = elseBlock;
                    
                    UnCondBraFwd(finalIfBlock, follow);
                    Fixup(currBlock, x.fixupLocation);
                    
                    BasicBlock finalElseBlock = statSequence(elseBlock, joinBlocks);
                    joinBlocks.add(0, elseBlock);

      
                    if(scanner.sym == Token.fiToken){
                        scanner.next();
                        Fixup(finalIfBlock, follow.fixupLocation);
                        
                        // Fill join block here
                        finalIfBlock.rightBlock = joinBlock;
                        finalElseBlock.leftBlock = joinBlock;
                        
                        // Get the phi functions for this block by comparing the variable versions of the lastIfBlock
                        // and lastElseBlock
                        generatePhiFunctions(joinBlock, finalIfBlock, finalElseBlock);
                        
                        // Update variable version numbers in join block
                        updateVariables(joinBlock, joinBlock);
//                        joinBlocks.add(joinBlock);

                    } else {
                        error("If missing corresponding fi");
                    }
                } else {
                    Fixup(currBlock, x.fixupLocation);
                    joinBlocks.add(0, ifBlock);

                    if (scanner.sym == Token.fiToken) {
                        scanner.next();
                        finalIfBlock.rightBlock = joinBlock;
                        
                        // Get phi functions by comparing the variable versions of the lastIfBlock and the currBlock
                        generatePhiFunctions(joinBlock, finalIfBlock, currBlock);
                        
                        // Update variable versions in join block
                        updateVariables(joinBlock, joinBlock);

                        joinBlocks.add(joinBlock);
                    } 
                }
            } else {
                error("No then after if");
            }
        } else {
            error("Error in if statement");
        }
        
        return joinBlock;
    }
    
    /*
     * Updating all the variable version numbers to be the results of the phi instructions
     */
    private void updateVariables(BasicBlock phiBlock, BasicBlock updateBlock) {
        Map<Integer, Instruction> phiInstructions = phiBlock.getPhiInstructions();
        for (Instruction instr : phiInstructions.values()) {
//            System.out.println(instr.op2.name);
            String var = instr.op1.name;
//            System.out.println(var);
            updateBlock.variables.get(var).add(instr.instructionNumber);
        }
    }
    
    /*
     * Calls statement() for all the statements in the program and returns the final basic block
     */
    private BasicBlock statSequence(BasicBlock currBlock, List<BasicBlock> joinBlocks) {
        currBlock = statement(currBlock, joinBlocks);
        while (scanner.sym == Token.semiToken) {
            scanner.next();
            currBlock = statement(currBlock, joinBlocks);
        }
        
        return currBlock;
    }
    
    /*
     * Depending on the token, the appropriate function is called and the final basic block is returned 
     * - in case of if - it is the last join block
     * - in case of while, it is the follow block
     * - for any other statements, it is the same block that is passed in
     */
    private BasicBlock statement(BasicBlock currBlock, List<BasicBlock> joinBlocks){
        if (scanner.sym == Token.letToken){
            assignment(currBlock);
        } else if (scanner.sym == Token.callToken){
            funcCall(currBlock);
        } else if (scanner.sym == Token.ifToken){
            currBlock = ifStatement(currBlock, joinBlocks);
        } else if (scanner.sym == Token.whileToken){
            currBlock = whileStatement(currBlock, joinBlocks);
        } else if (scanner.sym == Token.returnToken){
            returnStatement(currBlock);
        }
        
        return currBlock;
    }
    
    /*
     * Comparing the lastIfBlock and the lastElseBlock (or the current block) and generating Phi Functions based on the difference in
     * version numbers
     */
    private void generatePhiFunctions(BasicBlock joinBlock, BasicBlock leftBlock, BasicBlock rightBlock) {
        Map<String, List<Integer>> leftVariables = leftBlock.variables;
        Map<String, List<Integer>> rightVariables = rightBlock.variables;
        
        for (String str : leftBlock.arrNames) {
            System.out.println(str);
        }
        for (String var : leftVariables.keySet()) {
            System.out.println("variable before checking " + var);
            if (leftBlock.arrNames.contains(var) || var == null) {
                continue;
            }
            System.out.println("after checking " + var);
            if (rightVariables.containsKey(var)) {
                
                List<Integer> leftVersions = leftVariables.get(var);
                List<Integer> rightVersions = rightVariables.get(var);
                
                int left = leftVersions.get(leftVersions.size() - 1);
                int right = rightVersions.get(rightVersions.size() - 1);
                
                if (left != right) {
                    System.out.println("PHI Generated for " + var);

                    Instruction instr = new Instruction();
                    instr.kind = Instruction.Kind.PHI;
                    instr.operation = "PHI";
                    
                    Result op1 = new Result();
                    op1.name = var;
                    op1.kind = Result.Kind.VAR;
                    op1.version = _lineNum;
                    
                    Result op2 = new Result();
                    op2.name = var;
                    op2.kind = Result.Kind.INSTR;
                    op2.version = left;
                    
                    Result op3 = new Result();
                    op3.name = var;
                    op3.kind = Result.Kind.INSTR;
                    op3.version = right;
                    
                    instr.op1 = op1;
                    instr.op2 = op2;
                    instr.op3 = op3;
                    instr.instructionNumber = _lineNum;
                    joinBlock.instructions.put(_lineNum++, instr);
                }
            }
        }
    }    
    
    /*
     * The function is used to update the values of operands according to the phi functions introduced at the 
     * header of the while loop. It is only called from the while loop. 
     * 
     * It takes in as parameters the block in which the values have to be checked and changed if necessary and
     * a mapping of the old version number of a variable to a new one according to the phi function.
     * 
     * It first looks at all the phi functions of the block, and replaces its operands (op1 and op2 only) if 
     * required. Then using the phi functions, the mappings is updated to reflect new version numbers, and then
     * the replacement is done for all the rest of the instructions.
     */
    private void updateVariablesToPhiValues(BasicBlock currBlock, Map<Integer, Integer> mappings) {
        Map<Integer, Instruction> phiInstructions = currBlock.getPhiInstructions();
        
        for (Instruction instr : phiInstructions.values()) {                
            if (mappings.containsKey(instr.instructionNumber)) {
                mappings.remove(instr.instructionNumber);
            }
            if (mappings.containsKey(instr.op3.version)) {
                instr.op3.version = mappings.get(instr.op3.version);
            }
            
            if (mappings.containsKey(instr.op2.version)) {
                instr.op2.version = mappings.get(instr.op2.version);
            }
        }
              
        for (Instruction instr : phiInstructions.values()) {
            if (!mappings.containsKey(instr.op2.version)) {
                mappings.put(instr.op2.version, instr.instructionNumber);
            }
            
            if (!mappings.containsKey(instr.op3.version)) {
                mappings.put(instr.op3.version, instr.instructionNumber);
            }
        }
        
        for (Instruction instr : currBlock.instructions.values()) {
            if (instr.kind == Instruction.Kind.STD) {
                
                if (mappings.containsKey(instr.instructionNumber)) {
                    mappings.remove(instr.instructionNumber);
                }
                if (mappings.containsKey(instr.op1.version)) {
                    instr.op1.version = mappings.get(instr.op1.version);
                }
                
                if (mappings.containsKey(instr.op2.version)) {
                    instr.op2.version = mappings.get(instr.op2.version);
                }
            }
        }
    }
    
    /*
     * currBlock.leftBlock --> whileBlock
     * whileBlock.rightParent --> currBlock
     * whileBlock.leftBlock --> doBlock
     * whileBlock.rightBlock --> followBlock
     * followBlock.leftParent --> whileBlock
     * whileBlock.joinParent = whileBlock
     * whileBlock.joinBlock = whileBlock
     * doBlock.leftBlock = whileBlock
     * doBlock.rightParent = whileBlock
     */
    private BasicBlock whileStatement(BasicBlock currBlock, List<BasicBlock> joinBlocks){
        BasicBlock followBlock = new BasicBlock();
        followBlock.kind = BasicBlock.Kind.FOLLOW;
        copyVariables(currBlock, followBlock);
        
        Result x = new Result();
        Result follow = new Result();
        follow.fixupLocation = _lineNum;
        
        if(scanner.sym == Token.whileToken){
            scanner.next();
            BasicBlock whileBlock = new BasicBlock();
            currBlock.leftBlock = whileBlock;
            whileBlock.rightParent = currBlock; 
            whileBlock.kind = BasicBlock.Kind.WHILE;
            copyVariables(currBlock, whileBlock);

            x = relation(whileBlock);
            CondNegBraFwd(whileBlock, x);
            
            if(scanner.sym == Token.doToken){
                scanner.next();
                
                BasicBlock doBlock = new BasicBlock();
                whileBlock.leftBlock = doBlock;
                whileBlock.rightBlock = followBlock;
                followBlock.leftParent = whileBlock;
                whileBlock.joinParent = whileBlock;
                whileBlock.joinBlock = whileBlock;
                
                doBlock.leftBlock = whileBlock;
                doBlock.rightParent = whileBlock;
                doBlock.kind = BasicBlock.Kind.DO;
                copyVariables(whileBlock, doBlock);
                
                if (joinBlocks == null) {
                    joinBlocks = new ArrayList<BasicBlock>();
                }
                
                BasicBlock lastDoBlock = statSequence(doBlock, joinBlocks);
                joinBlocks.add(0, followBlock);
                joinBlocks.add(0, doBlock);
                joinBlocks.add(0, whileBlock);
                
                UnCondBraFwd(lastDoBlock, follow);
                
                if(scanner.sym == Token.odToken) {
                    scanner.next();
                    
                    // Generate the phi functions in the header block based on the difference in version
                    // numbers of variables in the while block and the lastDoBlock
                    generatePhiFunctions(whileBlock, whileBlock, lastDoBlock);
                    
                    //add the final values of variables in the follow block variables
                    updateVariables(whileBlock, followBlock);
                    
                    // make changes to the variables used in the Do Block and the While block too 
                    // if any of them have corresponding phi functions
                    Map<Integer, Integer> mappings = new HashMap<Integer, Integer>();
                    Set<BasicBlock> bb = new HashSet<BasicBlock>();
                    update(whileBlock, bb, mappings);
                    //bb.add(whileBlock);
                    
                    
//                    System.out.println("One time ");
//                    for (BasicBlock joinBlock : joinBlocks) {
//                        updateVariablesToPhiValues(joinBlock, mappings);
//                        System.out.println(joinBlock.kind);
//                        if (joinBlock.instructions.keySet().iterator().hasNext()) {
//                            int a = joinBlock.instructions.keySet().iterator().next();
//                            System.out.println(" line - " + a);
//                        }
//                        
//                        for (int key : mappings.keySet()) {
//                            System.out.println(key + " --> " + mappings.get(key));
//                        }
//                        
//                    }
                    
                    
                    
                    Fixup(whileBlock, x.fixupLocation);
                    
                } else {
                    error("Do missing corresponding od");
                }
            } else {
                error("No do following while");
            }
        } else {
            error("Error in while statement");
        }
        
        return followBlock;
    }
    
    private void update (BasicBlock block, Set<BasicBlock> set, Map<Integer, Integer> map) {
        if (block == null || set.contains(block)) {
            return;
        }
        
        updateVariablesToPhiValues(block, map);
        set.add(block);
        update(block.leftBlock, set, new HashMap<Integer, Integer>(map));
        update(block.rightBlock, set, new HashMap<Integer, Integer>(map));
        update(block.joinBlock, set, new HashMap<Integer, Integer>(map));
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private void assignment(BasicBlock currBlock){
        if (scanner.sym == Token.letToken) {
            scanner.next();
            Result x = designator(currBlock);
            if (scanner.sym == Token.becomesToken) {
                scanner.next();
                Result y = expression(currBlock);
                if (currBlock.variables.containsKey(x.name)) {
                    currBlock.variables.get(x.name).add(_lineNum);
                } else {
                    if (!currBlock.arrNames.contains(x.name)){
                        List<Integer> lineNumbers = new ArrayList<Integer>();
                        lineNumbers.add(_lineNum);
                        currBlock.variables.put(x.name, lineNumbers);
                    }
                    
                }
                Compute(currBlock, Token.becomesToken, y, x);

            } else {
                error("Assignment: designator must be followed by <-");
            }
        } else {
            error("Error in assignment");
        }
    }
    
    /*
     * Checks for the 3 predefined functions - OutputNum, InputNum, OutputNewLine
     */
    private boolean isPredefinedFunction(Result x) {
        return x.name.equals("OutputNum") || x.name.equals("InputNum") || x.name.equals("OutputNewLine");
    }
    
    private Result generateFunctionCall(BasicBlock currBlock, Result x, Result y) {
        Result out = new Result();
        out.kind = Result.Kind.INSTR;
        out.version = _lineNum;
        if (x.name.equals("OutputNum")) {
            Instruction instr = new Instruction();
            instr.kind = Instruction.Kind.FUNC;
            instr.operation = "WRITE";
            instr.op1 = y;
            currBlock.instructions.put(_lineNum++, instr);
        } else if (x.name.equals("InputNum")) {
            Instruction instr = new Instruction();
            instr.kind = Instruction.Kind.FUNC;
            instr.operation = "READ";
            currBlock.instructions.put(_lineNum++, instr);
        } else if (x.name.equals("OutputNewLine")) {
            Instruction instr = new Instruction();
            instr.kind = Instruction.Kind.FUNC;
            instr.operation = "WRITE NEW LINE";
            currBlock.instructions.put(_lineNum++, instr);
        }
        return out;
    }
    
    private Result funcCall(BasicBlock currBlock){
        Result res = new Result();
        if (scanner.sym == Token.callToken) {
            scanner.next();
            Result x = ident();
            if (scanner.sym == Token.openparanToken) {
                scanner.next();
                Result y = expression(currBlock);
                if (isPredefinedFunction(x)) {
                    res = generateFunctionCall(currBlock, x, y);
                }
                while (scanner.sym == Token.commaToken) {
                    scanner.next();
                    expression(currBlock);
                }
                if (scanner.sym == Token.closeparanToken) {
                    scanner.next();
                } else {
                    error("function call missing closing paran");
                }
            } else {
                error("function call missing open paran");
            }
        } else {
            error("Error in funcCall");
        }
        
        return res;
    }
    
    private Result returnStatement(BasicBlock currBlock){
        Result x = null;
        if(scanner.sym == Token.returnToken){
            scanner.next();
            x = expression(currBlock);
        } else {
            error("Error in return");
        }
        
        return x;
    }
    
    private void varDecl(BasicBlock currBlock){
        Identifier ident = typeDecl();
        
        if(scanner.sym == Token.identToken){
            Result x = ident();
            
            if (ident.type == Identifier.Type.VAR) {
                Result dummy1 = new Result();
                dummy1.kind = Result.Kind.CONST;
                dummy1.value = 0;
                List<Integer> list = new ArrayList<Integer>();
                list.add(_lineNum);
                currBlock.variables.put(x.name, list);
                Compute(currBlock, Token.becomesToken, dummy1, x);
            } else if (ident.type == Identifier.Type.ARR) {
               currBlock.arrNames.add(x.name);
                Instruction instr = new Instruction();
                instr.operation =  "ARR " + x.name + " defined";
                currBlock.instructions.put(_lineNum++, instr);
                List<Integer> list = new ArrayList<Integer>();
                list.add(-1);
                currBlock.variables.put(x.name, list);
                symbolTable.put(ident.name, ident);
            }
              
            while(scanner.sym == Token.commaToken){
                scanner.next();
                x = ident();
                Identifier ident2 = new Identifier(ident);
 
                if (ident2.type == Identifier.Type.VAR) {
                    Result dummy1 = new Result();
                    dummy1.kind = Result.Kind.CONST;
                    dummy1.value = 0;
                    List<Integer> list2 = new ArrayList<Integer>();
                    list2.add(_lineNum);
                    currBlock.variables.put(x.name, list2);
                    Compute(currBlock, Token.becomesToken, dummy1, x);
                } else if (ident2.type == Identifier.Type.ARR) {
                    currBlock.arrNames.add(x.name);
                    Instruction instr2 = new Instruction();
                    instr2.operation =  "ARR " + x.name + " defined";
                    currBlock.instructions.put(_lineNum++, instr2);
                    List<Integer> list2 = new ArrayList<Integer>();
                    list2.add(-1);
                    currBlock.variables.put(x.name, list2);
                    symbolTable.put(ident2.name, ident2);
                }
            }
            if(scanner.sym == Token.semiToken){
                scanner.next();
            }else{
                error("Missing ; in varDecl");
            }
        } else {
            error("var declaration isn't followed by identifier");
        }
    }
    
    private Identifier typeDecl(){
        Identifier ident = new Identifier();

        if (scanner.sym == Token.varToken){
            ident.type = Identifier.Type.VAR;
            scanner.next();
        } else if (scanner.sym == Token.arrToken){
            scanner.next();
            ident.type = Identifier.Type.ARR;
            if (scanner.sym == Token.openbracketToken){
                scanner.next();
                Result x = number();
//                System.out.println(x.kind);
//                System.out.println(x.value);
//                System.out.println(ident.dimensions);
                ident.dimensions.add(x.value);
                if (scanner.sym == Token.closebracketToken){
                    scanner.next();
                } else {
                    error("Array decl missing ]");
                }
                
                while(scanner.sym == Token.openbracketToken){
                    scanner.next();
                    x = number();
                    ident.dimensions.add(x.value);
                    if (scanner.sym == Token.closebracketToken){
                        scanner.next();
                    } else {
                        error("Array decl missing ]");
                    }
                }
            } else {
                error("Array decl missing [");
            }
        } else {
            error("Type decl missing var and arr");
        }
        
        return ident;
    }
    
    private void funcDecl() {
        if (scanner.sym == Token.funcToken || scanner.sym == Token.procToken) {
            scanner.next();
            Result func = ident();
            
            // Creating a new CFG for the new function 
            CFG newFunction = new CFG();
            newFunction.functionName = func.name;
            newFunction.startBlock = new BasicBlock();
            functionCFGs.add(newFunction);
            
            // Setting the current block to be the start block of this function 
            BasicBlock currBlock = newFunction.startBlock;
            
            
            formalParam(currBlock, newFunction);
            
            if (scanner.sym == Token.semiToken) {
                scanner.next();
                
                funcBody(currBlock, null);
                if (scanner.sym == Token.semiToken) {
                    scanner.next();
                } else {
                    error("No ; after function body in function declaration");
                }
            } else {
                error("No ; after formal paramater in function ");
            }  
        } else {
            error("No function or procedure declaration in function declaration");
        }
    }
    
    // No arrays allowed - only simple variables
    private void formalParam(BasicBlock currBlock, CFG function) {
        if (scanner.sym == Token.openparanToken) {
            scanner.next();
            
            if (scanner.sym == Token.closeparanToken) {
                scanner.next();
            } else {
                Result x = ident();
                Identifier ident = new Identifier();
                ident.type = Identifier.Type.VAR;
                ident.name = x.name;
                
                function.argumentList.add(ident);
                
                Result dummy = new Result();
                dummy.kind = Result.Kind.CONST;
                dummy.value = 0;
                List<Integer> list = new ArrayList<Integer>();
                list.add(_lineNum);
                currBlock.variables.put(x.name, list);
                Compute(currBlock, Token.becomesToken, dummy, x);
                
                while (scanner.sym == Token.commaToken) {
                    scanner.next();
                    Result x1 = ident();
                    Identifier ident1 = new Identifier();
                    ident.type = Identifier.Type.VAR;
                    ident.name = x1.name;
                    
                    function.argumentList.add(ident1);
                    
                    Result dummy1 = new Result();
                    dummy1.kind = Result.Kind.CONST;
                    dummy1.value = 0;
                    List<Integer> list1 = new ArrayList<Integer>();
                    list.add(_lineNum);
                    currBlock.variables.put(x1.name, list1);
                    Compute(currBlock, Token.becomesToken, dummy1, x1);
                }
                if (scanner.sym == Token.closeparanToken) {
                    scanner.next();
                } else {
                    error("No ) after formalParam");
                }
            }
            
        } else {
            error("No opening ( in formalParam");
        }
    }
    
    private void funcBody(BasicBlock currBlock, List<BasicBlock> joinBlocks) {
        if (scanner.sym == Token.varToken || scanner.sym == Token.arrToken) {
            varDecl(currBlock);
        }
        
        if (scanner.sym == Token.beginToken) {
            scanner.next();
            statSequence(currBlock, joinBlocks);
            if (scanner.sym == Token.endToken) {
                scanner.next();
            } else {
                error("NO } in function body");
            }
        } else {
            error("No { in function body");
        }
    }
}