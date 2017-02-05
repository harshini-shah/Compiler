
public class Parser {
    private Scanner scanner;
    public Parser(){
        
    }
    
    private void computation(){
        scanner.next();
        while(scanner.sym != Token.eofToken){
            if(scanner.sym == Token.mainToken){
                scanner.next();
                while(scanner.sym == Token.varToken || scanner.sym == Token.arrToken){
                    varDecl();
                    scanner.next();
                }
                while(scanner.sym == Token.funcToken || scanner.sym == Token.procToken){
                    funcDecl();
                    scanner.next();
                }
                if(scanner.sym == Token.beginToken){
                    scanner.next();
                    statSequence();
                    if(scanner.sym == Token.endToken){
                        scanner.next();
                        if(scanner.sym != Token.periodToken){
                            error("Program doesn't end with period");
                        }
                    }else{
                        error("Missing } of main");
                    }
                }else{
                    error("Missing { of main");
                }
            }
        }
    }
    
    private void varDecl(){
        typeDecl();
        if(scanner.sym == Token.identToken){
            ident();
            while(scanner.sym == Token.commaToken){
                scanner.next();
                ident();
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
    
    private void typeDecl(){
        if(scanner.sym == Token.varToken){
            scanner.next();
        }else if (scanner.sym == Token.arrToken){
            scanner.next();
            if(scanner.sym == Token.openbracketToken){
                scanner.next();
                number();
                if(scanner.sym == Token.closebracketToken){
                    scanner.next();
                }else{
                    error("Array decl missing ]");
                }
                while(scanner.sym == Token.openbracketToken){
                    scanner.next();
                    number();
                    if(scanner.sym == Token.closebracketToken){
                        scanner.next();
                    }else{
                        error("Array decl missing ]");
                    }
                }
            }else{
                error("Array decl missing [");
            }
        }else{
            error("Type decl missing var and arr");
        }
    }
    

    private void number() {
        
    }
    
    private void ident() {
        
    }
    
    private void relOp() {
        
    }
    
    private void letter() {
        
    }
    
    private void digit() {
        
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
    
    private void designator() {
        ident();
        while (scanner.sym == Token.openbracketToken) {
            scanner.next();
            expression();
            if (scanner.sym == Token.closebracketToken) {
                scanner.next();
            } else {
                error("No ] after the expression in designator (for array)");
            }
        }
    }
    
    private void factor() {
        if (scanner.sym == Token.callToken) {
            funcCall();
        } else if (scanner.sym == Token.openparanToken) {
            scanner.next();
            expression();
            if (scanner.sym == Token.closeparanToken) {
                scanner.next();
            } else {
                error("No closing parenthesis for factor");
            }
        } else if (scanner.sym == Token.numberToken) {
            number();
        } else {
            designator();
        }
    }
    
    private void term() {
        factor();
        while (scanner.sym == Token.timesToken || scanner.sym == Token.divToken) {
            scanner.next();
            factor();
        }
    }
    
    private void expression() {
        term();
        while (scanner.sym == Token.plusToken || scanner.sym == Token.minusToken) {
            scanner.next();
            term();
        }
    }
    
    private void relation() {
        expression();
        relOp();
        expression();
    }
    
    private void statSequence() {
        statement();
        while (scanner.sym == Token.semiToken) {
            scanner.next();
            statement();
        }
    }
    
    private void statement(){
        if(scanner.sym == Token.letToken){
            assignment();
        }else if(scanner.sym == Token.callToken){
            funcCall();
        }else if(scanner.sym == Token.ifToken){
            ifStatement();
        }else if(scanner.sym == Token.whileToken){
            whileStatement();
        }else if(scanner.sym == Token.returnToken){
            returnStatement();
        }else{
            error("Error in statement");
        }
    }

    private void assignment(){
        if(scanner.sym == Token.letToken){
            scanner.next();
            designator();
            if(scanner.sym == Token.becomesToken){
                scanner.next();
                expression();
            }else{
                error("Assignment: designator must be followed by <-");
            }
        }else{
            error("Error in assignment");
        }
    }

    private void funcCall(){
        if(scanner.sym == Token.callToken){
            scanner.next();
            ident();
            if(scanner.sym == Token.openParan){
                scanner.next();
                expression();
                while(scanner.sym == Token.commaToken){
                    scanner.next();
                    expression();
                }
                if(scanner.sym == Token.closeParan){
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
            scanner.next();
            relation();
            if(scanner.sym == Token.thenToken){
                scanner.next();
                statSequence();
                if(scanner.sym == Token.elseToken){
                    scanner.next();
                    statSequence();
                    if(scanner.sym == Token.fiToken){
                        scanner.next();
                    }else{
                        error("If missing corresponding fi");
                    }
                }else{
                    error("If missing corresponding else");
                }
            }
        }else{
            error("Error in if statement");
        }
    }

    private void whileStatement(){
        if(scanner.sym == Token.whileToken){
            scanner.next();
            relation();
            if(scanner.sym == Token.doToken){
                scanner.next();
                statSequence();
                if(scanner.sym == Token.odToken){
                    scanner.next();
                }else{
                    error("Do missing corresponding od");
                }
            }
        }else{
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
}