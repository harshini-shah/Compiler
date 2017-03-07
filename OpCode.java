
public class OpCode {
    static final String get(int token) {
        switch(token) {
        case Token.plusToken: 
            return "ADD";
        case Token.timesToken:
            return "MUL";
        case Token.divToken:
            return "DIV";
        case Token.minusToken: 
            return "SUB";
        case Token.becomesToken:
            return "MOV";
        case Token.compareToken:
            return "CMP";
        case Token.branchToken: 
            return "BRA";
        case Token.eqlToken:
            return "BEQ";
        case Token.neqToken:
            return "BNE";
        case Token.lssToken: 
            return "BLT";
        case Token.geqToken:
            return "BGE";
        case Token.leqToken:
            return "BLE";
        case Token.gtrToken: 
            return "BGT";
        }
        
        return "";
    }
}
