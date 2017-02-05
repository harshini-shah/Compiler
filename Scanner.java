import java.util.HashMap;
import java.util.Map;

public class Scanner {
    private static final char SEPARATOR = ' ';
    private static final char NEWLINE = '\n';
    public int sym;
    public int val;
    public int id;
    private FileReader _fr;
    private boolean _getNext;
    private int _nextSym;
    private static int symbolCount;
    Map<String, Integer> tokenTable;
    Map<String, Integer> symbolTable;
    
    public Scanner(String fileName) {
        _fr = new FileReader(fileName);
        _fr.next();
        _getNext = true;
        tokenTable = new HashMap<String, Integer>();
        symbolTable = new HashMap<String, Integer>();
        populateTokenTable();
        
    }
    
    public void next() {
        if (_getNext) {
            StringBuilder token = new StringBuilder();
            while(_getNext && _fr.sym != SEPARATOR && _fr.sym != NEWLINE && _fr.sym != Token.eofToken){
                if (_fr.sym == ';') {
                    _getNext = false;
                    _nextSym = 70;
                } else if (_fr.sym == ',') {
                    _getNext = false;
                    _nextSym = 31;
                } else {
                    token.append(_fr.sym);
                    _fr.next();
                } 
            }
            if(_fr.sym == Token.eofToken){
                sym = Token.eofToken;
            }else{
                setParameters(token.toString());
            }
            // set the sym, val and id using the token class results
        } else {
            sym = _nextSym;
        }
    }
    
    public void error(String errorMsg) {
        System.out.println("ERROR in Scanner class: " + errorMsg);
    }
    
    private void populateTokenTable() {
        tokenTable.put("*", 1);
        tokenTable.put("/", 2);
        tokenTable.put("+", 11);
        tokenTable.put("-", 12);
        tokenTable.put("==", 20);
        tokenTable.put("!=", 21);
        tokenTable.put("<", 22);
        tokenTable.put(">=", 23);
        tokenTable.put("<=", 24);
        tokenTable.put(">", 25);
        tokenTable.put(".", 30);
        tokenTable.put(",", 31);
        tokenTable.put("[", 32);
        tokenTable.put("]", 34);
        tokenTable.put(")", 35);
        tokenTable.put("<-", 40);
        tokenTable.put("then", 41);
        tokenTable.put("do", 42);
        tokenTable.put("(", 50);
        tokenTable.put(";", 70);
        tokenTable.put("}", 80);
        tokenTable.put("od", 81);
        tokenTable.put("fi", 82);
        tokenTable.put("else", 90);
        tokenTable.put("let", 100);
        tokenTable.put("call", 101);
        tokenTable.put("if", 102);
        tokenTable.put("while", 103);
        tokenTable.put("return", 104);
        tokenTable.put("var", 110); 
        tokenTable.put("array", 111);
        tokenTable.put("function", 112);
        tokenTable.put("procedure", 113);
        tokenTable.put("{", 150);
        tokenTable.put("main", 200);
        tokenTable.put("endOfFile", 255);
    }
    
    private void setParameters(String token) {
      if(tokenTable.containsKey(token)){
          sym = tokenTable.get(token);
      }else if(Character.isDigit(token.charAt(0))){
          sym = 60;
          val = Integer.parseInt(token);
      }else{
          sym = 61;
          id = symbolCount++;
          symbolTable.put(token, id);
      }
    } 
}
