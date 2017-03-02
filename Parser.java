import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Parser {
    private Scanner scanner;
    private Map<Integer, String> opCodeImm;
    private Map<Integer, String> opCode;
    private Map<Integer, Integer> negatedBranchOp;
    private static int _lineNum = 1;
    public static Map<Integer, Identifier> symbolTable;
    BasicBlock firstBlock;
    
    public Parser(String fileName){
        scanner = new Scanner(fileName);
        opCodeImm = new HashMap<Integer, String>();
        opCode = new HashMap<Integer, String>();
        negatedBranchOp = new HashMap<Integer, Integer>();
        symbolTable = new HashMap<Integer, Identifier>();
        firstBlock = new BasicBlock();
        populateOpCodes();
        computation(firstBlock);
    }
    
    private void populateOpCodes(){
        opCodeImm.put(11, "ADDI");
        opCodeImm.put(1, "MULI");
        opCodeImm.put(2, "DIVI");
        opCodeImm.put(12, "SUBI");
        opCodeImm.put(40, "MOVI");
        opCodeImm.put(19, "CMP");
        opCodeImm.put(18, "BRA");
        opCodeImm.put(20, "BEQ");
        opCodeImm.put(21, "BNE");
        opCodeImm.put(22, "BLT");
        opCodeImm.put(23, "BGT");
        opCodeImm.put(24, "BLE");
        opCodeImm.put(25, "BGE");
        
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
                    varDecl();
                }
                while(scanner.sym == Token.funcToken || scanner.sym == Token.procToken){
                    funcDecl();
                }
                if(scanner.sym == Token.beginToken){
                    scanner.next();
                    statSequence();
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
        
//        int count = 1;
//        for (Instruction i : instructions.values()) {
//            System.out.println(count++ + "\t" + i.toString());
//        }
    }
    
    private void varDecl(){
        Identifier ident = typeDecl();
        if(scanner.sym == Token.identToken){
            Result x = ident();
            ident.name = x.name;
            symbolTable.put(symbolTable.size(), ident);
            
            while(scanner.sym == Token.commaToken){
                scanner.next();
                x = ident();
                Identifier ident2 = new Identifier(ident);
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
    
    private void funcDecl() {
        if (scanner.sym == Token.funcToken || scanner.sym == Token.procToken) {
            scanner.next();
            ident();
            formalParam();
            if (scanner.sym == Token.semiToken) {
                scanner.next();
            } else {
                error("No ; after formal paramater in function ");
            }
            funcBody();
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
    
    private void funcBody() {
        if (scanner.sym == Token.varToken || scanner.sym == Token.arrToken) {
            varDecl();
        }
        
        if (scanner.sym == Token.beginToken) {
            scanner.next();
            statSequence();
            if (scanner.sym == Token.endToken) {
                scanner.next();
            } else {
                error("NO } in function body");
            }
        } else {
            error("No { in function body");
        }
    }
    
    private void statSequence() {
        statement();
        while (scanner.sym == Token.semiToken) {
            scanner.next();
            statement();
        }
    }
    
    private void statement(){
        if (scanner.sym == Token.letToken){
            assignment();
        } else if (scanner.sym == Token.callToken){
            funcCall();
        } else if (scanner.sym == Token.ifToken){
            ifStatement();
        } else if (scanner.sym == Token.whileToken){
            whileStatement();
        } else if (scanner.sym == Token.returnToken){
            returnStatement();
        } else {
            return;
        }
    }

    private void assignment(BasicBlock currBlock){
        if(scanner.sym == Token.letToken){
            scanner.next();
            Result x = designator();
            if(scanner.sym == Token.becomesToken){
                scanner.next();
                Result y = expression();
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

    private void funcCall(){
        if(scanner.sym == Token.callToken){
            scanner.next();
            ident();
            if(scanner.sym == Token.openparanToken){
                scanner.next();
                expression();
                while(scanner.sym == Token.commaToken){
                    scanner.next();
                    expression();
                }
                if(scanner.sym == Token.closeparanToken){
                    scanner.next();
                }else{
                    error("function call missing closing paran");
                }
            }else{
                error("function call missing open paran");
            }
        }else{
            error("Error in funcCall");
        }
    }
    
    private void ifStatement(){
        if(scanner.sym == Token.ifToken){
            Result follow = new Result();
            follow.fixupLocation = 0;
            scanner.next();
            Result x = relation();
            CondNegBraFwd(x);
            if(scanner.sym == Token.thenToken){
                scanner.next();
                statSequence();
                if(scanner.sym == Token.elseToken){
                    scanner.next();
                    UnCondBraFwd(follow);
                    Fixup(x.fixupLocation);
                    statSequence();
                    if(scanner.sym == Token.fiToken){
                        scanner.next();
                        Fixup(follow.fixupLocation);
                    }else{
                        error("If missing corresponding fi");
                    }
                } else{
                    Fixup(x.fixupLocation);
                }
            }
        }else{
            error("Error in if statement");
        }
    }

    private void whileStatement(){
        Result x = new Result();
        Result follow = new Result();
        follow.fixupLocation = _lineNum;
        if(scanner.sym == Token.whileToken){
            scanner.next();
            x = relation();
            CondNegBraFwd(x);
            if(scanner.sym == Token.doToken){
                scanner.next();
                statSequence();
                UnCondBraFwd(follow);
                Fixup(x.fixupLocation);
                if(scanner.sym == Token.odToken){
                    scanner.next();
                }else{
                    error("Do missing corresponding od");
                }
            }
        } else {
            error("Error in while statement");
        }
    }

    private void returnStatement(){
        if(scanner.sym == Token.returnToken){
            scanner.next();
            expression();
        }else{
            error("Error in return");
        }
    }

    private void error(String errorMsg){
        scanner.error(errorMsg);
    }
    
    private Result number() {
        Result x = new Result();
        x.kind = Kind.CONST;
        x.value = scanner.val;
        scanner.next();
        return x;
    }
    
    private Result ident() {
        Result x = new Result();
        x.kind = Kind.VAR;
        x.name = scanner.name;
        scanner.next();
        return x;
    }
    
    private Result designator() {
        Result x = ident();
        if (currBB.variables.containsKey(x.name)) {
            x.instructionNum = currBB.variables.get(x.name).get(currBB.variables.get(x.name).size() - 1);
        } else {
            x.instructionNum = -1;
        }
        while (scanner.sym == Token.openbracketToken) {
            scanner.next();
            Result y = expression();
            if (scanner.sym == Token.closebracketToken) {
                scanner.next();
                // HANDLE ARRAYS
            } else {
                error("No ] after the expression in designator (for array)");
            }
        }
        return x;
    }
    
    private Result factor() {
        Result x = new Result();
        if (scanner.sym == Token.callToken) {
            funcCall();
        } else if (scanner.sym == Token.openparanToken) {
            scanner.next();
            x = expression();
            if (scanner.sym == Token.closeparanToken) {
                scanner.next();
            } else {
                error("No closing parenthesis for factor");
            }
        } else if (scanner.sym == Token.numberToken) {
            x = number();
        } else {
            x = designator();
            if (x.instructionNum == -1) {
                error("Variable used in Factor has not been defined previously");
            } else {
                x.kind = Kind.INSTR;
            }
        }
        return x;
    }
    
    private Result term() {
        Result x = factor();
        while (scanner.sym == Token.timesToken || scanner.sym == Token.divToken) {
            int op = scanner.sym;
            scanner.next();
            Result y = factor();
            Compute(op, x, y);
        }
        return x;
    }
    
    private Result expression() {
        Result x = term();
        while (scanner.sym == Token.plusToken || scanner.sym == Token.minusToken) {
            int op = scanner.sym;
            scanner.next();
            Result y = term();
            Compute(op, x, y);
        }
        return x;
    }
    
    private Result relation(BasicBlock currBlock) {
        Result x = expression();
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
            Result y = expression();
            Compute(Token.compare, x, y);
            x.kind = Kind.CONDN;
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
        if (x.kind == Kind.CONST && y.kind == Kind.CONST){
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
            if (x.kind == Kind.CONST || y.kind == Kind.CONST) {
                opCodes = opCodeImm;
            } else {
                opCodes = opCode;
            }
            
            Instruction instr = new Instruction();
            instr.instructionNumber = _lineNum;
            instr.operation = opCodes.get(op);
            instr.op1 = new Result(x);
            instr.op2 = y;
            currBlock.instructions.put(_lineNum++, instr);
            x.kind = Kind.INSTR;
            x.instructionNum = _lineNum - 1;
        }
    }
    
    public enum Kind{
        CONST, VAR, CONDN, INSTR
    }
    
    public class Result{
        public Kind kind;
        public int value;
        public int id;
        public String name;
        public int cond;
        public int fixupLocation;
        public int instructionNum;
        
        public Result() {   
        }
        
        public Result(Result x) {
            this.kind = x.kind;
            this.value = x.value;
            this.fixupLocation = x.fixupLocation;
            this.id = x.id;
            this.name = x.name;
            this.cond = x.cond;
            this.instructionNum = x.instructionNum;
        }
    }
}