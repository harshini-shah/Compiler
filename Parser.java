
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
    
    private void error(String errorMsg){
        scanner.error(errorMsg);
    }
}
