public class NegatedBranchOp {
    public static int get(int token) {
        switch (token) {
        case Token.eqlToken:
            return Token.neqToken;
        case Token.neqToken:
            return Token.eqlToken;
        case Token.lssToken:
            return Token.geqToken;
        case Token.geqToken:
            return Token.lssToken;
        case Token.leqToken:
            return Token.gtrToken;
        case Token.gtrToken:
            return Token.leqToken;
        }
        
        return 0;
    }
}
