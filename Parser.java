import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Parser {
    private Scanner scanner;
    private Map<Integer, String> opCode;
    private Map<Integer, Integer> negatedBranchOp;
    private static int _lineNum = 1;
    public static Map<Integer, Identifier> symbolTable;
    List<CFG> functionBlocks;
    
    public Parser(String fileName){
        scanner = new Scanner(fileName);
        opCode = new HashMap<Integer, String>();
        negatedBranchOp = new HashMap<Integer, Integer>();
        symbolTable = new HashMap<Integer, Identifier>();
        CFG mainFunction = new CFG();
        functionBlocks = new LinkedList<CFG>();
        functionBlocks.add(mainFunction);
        populateOpCodes();
        computation(mainFunction.startBlock);
    }
    
    private void populateOpCodes(){
        opCode.put(11, "ADD");
        opCode.put(1, "MUL");
        opCode.put(2, "DIV");
        opCode.put(12, "SUB");
        opCode.put(40, "MOV");
        opCode.put(19, "CMP");
        opCode.put(18, "BRA");
        opCode.put(20, "BEQ");
        opCode.put(21, "BNE");
        opCode.put(22, "BLT");
        opCode.put(23, "BGE");
        opCode.put(24, "BLE");
        opCode.put(25, "BGT");
        
        negatedBranchOp.put(20, 21);
        negatedBranchOp.put(21, 20);
        negatedBranchOp.put(22, 23);
        negatedBranchOp.put(23, 22);
        negatedBranchOp.put(24, 25);
        negatedBranchOp.put(25, 24);
    }
    
    private void computation(BasicBlock currBlock){
        scanner.next();
        while(scanner.sym != Token.eofToken){
            if(scanner.sym == Token.mainToken){
                scanner.next();
                while(scanner.sym == Token.varToken || scanner.sym == Token.arrToken){
                    varDecl(currBlock);
                }
                while(scanner.sym == Token.funcToken || scanner.sym == Token.procToken){
                    funcDecl(currBlock);
                }
                if(scanner.sym == Token.beginToken){
                    scanner.next();
                    currBlock = statSequence(currBlock);
                    if(scanner.sym == Token.endToken){
                        scanner.next();
                        if(scanner.sym != Token.periodToken){
                            error("Program doesn't end with a period");
                        } else {
                            scanner.next();
                        }
                    }else{
                        error("Missing } of main");
                    }
                } else{
                    error("Missing { of main");
                }
            }
        }
        
        Instruction instr = new Instruction();
        instr.kind = Instruction.Kind.END;
        instr.operation = "EOF";
        currBlock.instructions.put(_lineNum, instr);
        
        for (CFG cfg : functionBlocks) {
            VisualizeCFG test = new VisualizeCFG(cfg.startBlock);
        }
        
//        int count = 1;
//        for (Instruction i : instructions.values()) {
//            System.out.println(count++ + "\t" + i.toString());
//        }
    }
    
    private void varDecl(BasicBlock currBlock){
        Identifier ident = typeDecl();
        if(scanner.sym == Token.identToken){
            Result x = ident();
            ident.name = x.name;
            List<Integer> list = new ArrayList<Integer>();
            list.add(_lineNum);
            currBlock.variables.put(x.name, list);
            Result dummy1 = new Result();
            dummy1.kind = Result.Kind.CONST;
            dummy1.value = 0;
            Compute(currBlock, Token.becomesToken, dummy1, x);
            symbolTable.put(symbolTable.size(), ident);
            
            while(scanner.sym == Token.commaToken){
                scanner.next();
                x = ident();
                Identifier ident2 = new Identifier(ident);
                Result dummy2 = new Result();
                dummy2.kind = Result.Kind.CONST;
                dummy2.value = 0;
                List<Integer> list2 = new ArrayList<Integer>();
                list2.add(_lineNum);
                currBlock.variables.put(x.name, list2);
                Compute(currBlock, Token.becomesToken, dummy2, x);

                symbolTable.put(symbolTable.size(), ident2);
            }
            if(scanner.sym == Token.semiToken){
                scanner.next();
            }else{
                error("Missing ; in varDecl");
            }
        }else{
            error("var isn't followed by identifier");
        }
    }
    
    private Identifier typeDecl(){
        Identifier ident = new Identifier(Identifier.Type.VAR, "", symbolTable.size());;
        if (scanner.sym == Token.varToken){
            scanner.next();
        } else if (scanner.sym == Token.arrToken){
            scanner.next();
            ident.type = Identifier.Type.ARR;
            if (scanner.sym == Token.openbracketToken){
                scanner.next();
                Result x = number();
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
    
    private void funcDecl(BasicBlock currBlock) {
        if (scanner.sym == Token.funcToken || scanner.sym == Token.procToken) {
            scanner.next();
            ident();
            formalParam();
            if (scanner.sym == Token.semiToken) {
                scanner.next();
            } else {
                error("No ; after formal paramater in function ");
            }
            funcBody(currBlock);
            if (scanner.sym == Token.semiToken) {
                scanner.next();
            } else {
                error("No ; after function body in function declaration");
            }
        } else {
            error("No function or procedure declaration in function declaration");
        }
    }
    
    private void formalParam() {
        if (scanner.sym == Token.openparanToken) {
            scanner.next();
            ident();
            while (scanner.sym == Token.commaToken) {
                scanner.next();
                ident();
            }
            if (scanner.sym == Token.closeparanToken) {
                scanner.next();
            } else {
                error("No ) after formalParam");
            }
        } else {
            error("No opening ( in formalParam");
        }
    }
    
    private void funcBody(BasicBlock currBlock) {
        if (scanner.sym == Token.varToken || scanner.sym == Token.arrToken) {
            varDecl(currBlock);
        }
        
        if (scanner.sym == Token.beginToken) {
            scanner.next();
            statSequence(currBlock);
            if (scanner.sym == Token.endToken) {
                scanner.next();
            } else {
                error("NO } in function body");
            }
        } else {
            error("No { in function body");
        }
    }
    
    private BasicBlock statSequence(BasicBlock currBlock) {
        currBlock = statement(currBlock);
        while (scanner.sym == Token.semiToken) {
            scanner.next();
            currBlock = statement(currBlock);
        }
        
        return currBlock;
    }
    
    private BasicBlock statement(BasicBlock currBlock){
        if (scanner.sym == Token.letToken){
            assignment(currBlock);
        } else if (scanner.sym == Token.callToken){
            funcCall(currBlock);
        } else if (scanner.sym == Token.ifToken){
            currBlock = ifStatement(currBlock);
        } else if (scanner.sym == Token.whileToken){
            currBlock = whileStatement(currBlock);
        } else if (scanner.sym == Token.returnToken){
            returnStatement(currBlock);
        }
        
        return currBlock;
    }

    private void assignment(BasicBlock currBlock){
        if(scanner.sym == Token.letToken){
            scanner.next();
            Result x = designator(currBlock);
            if(scanner.sym == Token.becomesToken){
                scanner.next();
                Result y = expression(currBlock);
                if (currBlock.variables.containsKey(x.name)) {
                    currBlock.variables.get(x.name).add(_lineNum);
                } else {
                    List<Integer> lineNumbers = new ArrayList<Integer>();
                    lineNumbers.add(_lineNum);
                    currBlock.variables.put(x.name, lineNumbers);
                }
                Compute(currBlock, Token.becomesToken, y, x);

            }else{
                error("Assignment: designator must be followed by <-");
            }
        } else{
            error("Error in assignment");
        }
    }
    
    private boolean isPredefinedFunction(Result x) {
        return x.name.equals("OutputNum") || x.name.equals("InputNum") || x.name.equals("OutputNewLine");
    }
    
    private Result generateFunctionCall(BasicBlock currBlock, Result x, Result y) {
        Result out = new Result();
        out.kind = Result.Kind.INSTR;
        out.version = _lineNum;
        if (x.name.equals("OutputNum")) {
            Instruction instr = new Instruction();
            instr.operation = "WRITE";
            instr.op1 = y;
            currBlock.instructions.put(_lineNum++, instr);
        } else if (x.name.equals("InputNum")) {
            Instruction instr = new Instruction();
            instr.operation = "READ";
            currBlock.instructions.put(_lineNum++, instr);
        } else if (x.name.equals("OutputNewLine")) {
            Instruction instr = new Instruction();
            instr.operation = "WRITE NEW LINE";
            currBlock.instructions.put(_lineNum++, instr);
        }
        return out;
    }
    
    private Result funcCall(BasicBlock currBlock){
        Result res = new Result();
        if(scanner.sym == Token.callToken){
            scanner.next();
            Result x = ident();
            if(scanner.sym == Token.openparanToken){
                scanner.next();
                Result y = expression(currBlock);
                if (isPredefinedFunction(x)) {
                    res = generateFunctionCall(currBlock, x, y);
                }
                while(scanner.sym == Token.commaToken) {
                    scanner.next();
                    expression(currBlock);
                }
                if(scanner.sym == Token.closeparanToken) {
                    scanner.next();
                } else {
                    error("function call missing closing paran");
                }
            } else {
                error("function call missing open paran");
            }
        }else{
            error("Error in funcCall");
        }
        
        return res;
    }
    
    private void copyVariables(BasicBlock block1, BasicBlock block2) {
        for (String str : block1.variables.keySet()) {
            block2.variables.put(str, new ArrayList<Integer>(block1.variables.get(str)));
        }
    }
    
    private BasicBlock ifStatement(BasicBlock currBlock){
        BasicBlock joinBlock = new BasicBlock();
        joinBlock.kind = BasicBlock.Kind.JOIN;

        currBlock.joinBlock = joinBlock;
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
                copyVariables(currBlock, ifBlock);
                
                ifBlock = statSequence(ifBlock);

                
                if(scanner.sym == Token.elseToken){
                    scanner.next();
                    BasicBlock elseBlock = new BasicBlock();
                    elseBlock.kind = BasicBlock.Kind.ELSE;

                    copyVariables(currBlock, elseBlock);
                    currBlock.rightBlock = elseBlock;
                    UnCondBraFwd(ifBlock, follow);
                    Fixup(currBlock, x.fixupLocation);
                    elseBlock = statSequence(elseBlock);
                    
                    if(scanner.sym == Token.fiToken){
                        scanner.next();
                        Fixup(ifBlock, follow.fixupLocation);
                        // Fill join block here
                        ifBlock.rightBlock = joinBlock;
                        elseBlock.leftBlock = joinBlock;
                        currBlock.joinBlock = joinBlock;
                        generatePhiFunctionsForBranching(joinBlock, ifBlock, elseBlock);
                        return joinBlock;
                    } else {
                        error("If missing corresponding fi");
                    }
                } else {
                    Fixup(currBlock, x.fixupLocation);
                    if (scanner.sym == Token.fiToken) {
                        scanner.next();
                        ifBlock.rightBlock = joinBlock;
                        currBlock.joinBlock = joinBlock;
                        generatePhiFunctionsForBranching(joinBlock, ifBlock, currBlock);
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
    
    private void generatePhiFunctionsForBranching(BasicBlock joinBlock, BasicBlock leftBlock, BasicBlock rightBlock) {
        Map<String, List<Integer>> leftVariables = leftBlock.variables;
        Map<String, List<Integer>> rightVariables = rightBlock.variables;
        
        for (String var : leftVariables.keySet()) {
            if (rightVariables.containsKey(var)) {
                List<Integer> leftVersions = leftVariables.get(var);
                List<Integer> rightVersions = rightVariables.get(var);
                
                int left = leftVersions.get(leftVersions.size() - 1);
                int right = rightVersions.get(rightVersions.size() - 1);
                
                if (left != right) {
                    Instruction instr = new Instruction();
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
                    joinBlock.variables.get(var).add(_lineNum);
                    joinBlock.instructions.put(_lineNum++, instr);
                }
            }
        }
    }

    private BasicBlock whileStatement(BasicBlock currBlock){
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
            whileBlock.kind = BasicBlock.Kind.WHILE;
            copyVariables(currBlock, whileBlock);

            x = relation(whileBlock);
            CondNegBraFwd(whileBlock, x);
            
            if(scanner.sym == Token.doToken){
                scanner.next();
                
                BasicBlock doBlock = new BasicBlock();
                whileBlock.leftBlock = doBlock;
                whileBlock.rightBlock = followBlock;
                doBlock.leftBlock = whileBlock;
                doBlock.kind = BasicBlock.Kind.DO;
                copyVariables(whileBlock, doBlock);
                
                BasicBlock temp = new BasicBlock();
                temp = statSequence(doBlock);
                UnCondBraFwd(temp, follow);
                
                if(scanner.sym == Token.odToken) {
                    scanner.next();
                    Map<String, Integer> phi = generatePhiFunctionsForWhileLoop(whileBlock, whileBlock, temp);
                    //add the final values of variables in the follow block variables
                    updateVariables(phi, followBlock);
                                        
                    // make changes to the variables used in the Do Block if any of them have corresponding phi functions
                    updateVariablesInInstructions(phi, doBlock);
                    
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
    
    private void updateVariablesInInstructions(Map<String, Integer> phi, BasicBlock doBlock) {
        Set<Integer> lineNumbers = new HashSet<Integer>();
        lineNumbers.addAll(doBlock.instructions.keySet());
        Map<String, Integer> newPhi = new HashMap<String, Integer>(phi);
        
        for (Integer line : doBlock.instructions.keySet()) {
            Instruction currInstr = doBlock.instructions.get(line);
            if (currInstr.operation.equals("MOV") || currInstr.operation.equals("MOVI")) {
                changeVariableInstruction(newPhi, currInstr.op1, lineNumbers);
            } else {
                changeVariableInstruction(newPhi, currInstr.op1, lineNumbers);
                changeVariableInstruction(newPhi, currInstr.op2, lineNumbers);
            }
            //For each operand check if the variable is in the phi - if yes, then check if the type is an instruction, if
            // yes then update the number
            
        }
    }
    
    private void changeVariableInstruction(Map<String, Integer> phi, Result x, Set<Integer> lineNumbers) {
        if (x == null || x.name == null) {
            return;
        }
        if (phi.containsKey(x.name) && !lineNumbers.contains(x.version)) {
            if (x.kind == Result.Kind.INSTR) {
                x.version = phi.get(x.name);
            }
        }
    }
    
    private void updateVariables(Map<String, Integer> phi, BasicBlock currBlock) {
        for (String var : phi.keySet()) {
            if (currBlock.variables.containsKey(var)) {
                if (currBlock.variables.get(var).get(currBlock.variables.get(var).size() - 1) != phi.get(var)) {
                    currBlock.variables.get(var).add(phi.get(var));
                }
            }
        }
    }
    
    private Map<String, Integer> generatePhiFunctionsForWhileLoop(BasicBlock headerBlock, BasicBlock leftBlock, BasicBlock rightBlock) {
        Map<String, List<Integer>> leftVariables = leftBlock.variables;
        Map<String, List<Integer>> rightVariables = rightBlock.variables;
        Map<String, Integer> phis = new HashMap<String, Integer>();
        
        for (String var : leftVariables.keySet()) {
            if (rightVariables.containsKey(var)) {
                List<Integer> leftVersions = leftVariables.get(var);
                List<Integer> rightVersions = rightVariables.get(var);
                
                int left = leftVersions.get(leftVersions.size() - 1);
                int right = rightVersions.get(rightVersions.size() - 1);
                
                if (left != right) {
                    Instruction instr = new Instruction();
                    instr.operation = "PHI";
                    instr.instructionNumber = _lineNum;
                    
                    Result op1 = new Result();
                    op1.name = var;
                    op1.kind = Result.Kind.VAR;
                    
                    Result op3 = new Result();
                    op3.name = var;
                    op3.kind = Result.Kind.INSTR;
                    op3.version = left;
                    
                    Result op2 = new Result();
                    op2.name = var;
                    op2.kind = Result.Kind.INSTR;
                    op2.version = right;
                    
                    instr.op1 = op1;
                    instr.op2 = op2;
                    instr.op3 = op3;
                    headerBlock.variables.get(var).add(_lineNum);
                    phis.put(var, _lineNum);
                    headerBlock.instructions.put(_lineNum++, instr);
                }
            }
        }
        
        return phis;
    }

    private void returnStatement(BasicBlock currBlock){
        if(scanner.sym == Token.returnToken){
            scanner.next();
            expression(currBlock);
        }else{
            error("Error in return");
        }
    }

    private void error(String errorMsg){
        scanner.error(errorMsg);
    }
    
    private Result number() {
        Result x = new Result();
        x.kind = Result.Kind.CONST;
        x.value = scanner.val;
        scanner.next();
        return x;
    }
    
    private Result ident() {
        Result x = new Result();
        x.kind = Result.Kind.VAR;
        x.name = scanner.name;
        scanner.next();
        return x;
    }
    
    private Result designator(BasicBlock currBlock) {
        Result x = ident();
        if (currBlock.variables.containsKey(x.name)) {
            x.version = currBlock.variables.get(x.name).get(currBlock.variables.get(x.name).size() - 1);
        } else {
            x.version = -1;
        }
        while (scanner.sym == Token.openbracketToken) {
            scanner.next();
            Result y = expression(currBlock);
            if (scanner.sym == Token.closebracketToken) {
                scanner.next();
                // HANDLE ARRAYS
            } else {
                error("No ] after the expression in designator (for array)");
            }
        }
        return x;
    }
    
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
                error("No closing parenthesis for factor");
            }
        } else if (scanner.sym == Token.numberToken) {
            x = number();
        } else {
            x = designator(currBlock);
            if (x.version == -1) {
                error("Variable used in Factor has not been defined previously");
            } else {
                x.kind = Result.Kind.INSTR;
                x.name = null;
            }
        }
        return x;
    }
    
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
    
    private Result relation(BasicBlock currBlock) {
        Result x = expression(currBlock);
        Set<Integer> relOperators = new HashSet<Integer>();
        relOperators.add(Token.eqlToken);
        relOperators.add(Token.neqToken);
        relOperators.add(Token.leqToken);
        relOperators.add(Token.lssToken); 
        relOperators.add(Token.geqToken);
        relOperators.add(Token.gtrToken);
        if (relOperators.contains(scanner.sym)) {
            int op = scanner.sym;
            scanner.next();
            Result y = expression(currBlock);
            Compute(currBlock, Token.compare, x, y);
            x.kind = Result.Kind.CONDN;
            x.cond = op;
        } else {
            error("Relation has just one operand");
        }
        return x;
    }

    private void CondNegBraFwd(BasicBlock currBlock, Result x){
        x.fixupLocation = _lineNum;
        Instruction instr = new Instruction();
        instr.kind = Instruction.Kind.BRANCH;
        instr.instructionNumber = _lineNum;
        instr.op1 = x;
        instr.operation = opCode.get(negatedBranchOp.get(x.cond));
        currBlock.instructions.put(_lineNum++, instr);
    }
    
    private void UnCondBraFwd(BasicBlock currBlock, Result x){
        Instruction instr = new Instruction();
        instr.kind = Instruction.Kind.BRANCH;
        instr.instructionNumber = _lineNum;
        instr.op1 = new Result(x);
        instr.operation = opCode.get(18);
        currBlock.instructions.put(_lineNum++, instr);
        x.fixupLocation = _lineNum - 1;
    }
    
    private void Fixup(BasicBlock currBlock, int fixupLocation) {
        Instruction instr = currBlock.instructions.get(fixupLocation);
        instr.op1.fixupLocation = _lineNum;
    }
    
    private void Compute(BasicBlock currBlock, int op, Result x, Result y){
        if (x.kind == Result.Kind.CONST && y.kind == Result.Kind.CONST){
            if(op == Token.timesToken)
                x.value *= y.value;
            else if(op == Token.plusToken)
                x.value += y.value;
            else if(op == Token.minusToken)
                x.value -= y.value;
            else if(op == Token.divToken)
                x.value /= y.value;                                                 //check if division by zero needs to be checked
        } else {
            Map<Integer, String> opCodes;
//            if (x.kind == Kind.CONST || y.kind == Kind.CONST) {
//                opCodes = opCodeImm;
//            } else {
                opCodes = opCode;
//            }
            
            Instruction instr = new Instruction();
            instr.instructionNumber = _lineNum;
            instr.operation = opCodes.get(op);
            instr.op1 = new Result(x);
            instr.op2 = y;
            currBlock.instructions.put(_lineNum++, instr);
            x.kind = Result.Kind.INSTR;
            x.version = _lineNum - 1;
        }
    }    
    
}