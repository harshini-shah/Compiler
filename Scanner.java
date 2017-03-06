import java.util.HashMap;
import java.util.Map;

public class Scanner {
    public int sym;
    public int val;
    public String name;
    private FileReader _fr;
    public Map<String, Integer> tokenTable;
    
    public Scanner(String fileName) {
        _fr = new FileReader(fileName);
        _fr.next();
        tokenTable = new HashMap<String, Integer>();
        populateTokenTable();
    }
    
    public void next() {
    	if(_fr.sym != Token.eofToken){
    		while(Character.isWhitespace(_fr.sym)){
    			_fr.next();
    		}
	    	if(Character.isLetter(_fr.sym)){
	    		generateIdentifier();
	    	}else if(Character.isDigit(_fr.sym)){
	    		val = getNumber();
	    		sym = Token.numberToken;
	    	}else{
	    		switch(_fr.sym){
		    		case '=':
		    		case '!':
		    		case '>':
		    		case '<':{
		    			if(_fr.lookAhead() == '='){
		    				sym = tokenTable.get(_fr.sym + "=");
	    					_fr.next();
		    			}else if(_fr.sym == '<' && _fr.lookAhead() == '-'){
		    				sym = Token.becomesToken;
		    				_fr.next();
		    			} else if (_fr.sym == '<' || _fr.sym == '>') {
		    			    sym = tokenTable.get(String.valueOf(_fr.sym));
		    			}
		    			break;
		    		}
		    		default:{
		    			if(tokenTable.containsKey(String.valueOf(_fr.sym))){
		    		          sym = tokenTable.get(String.valueOf(_fr.sym));
		    		    }
		    			break;
		    		}
	    		}
	    	}
	    	_fr.next();
    	}else{
    		sym = Token.eofToken;
    	}
    }
    
    private int getNumber() {  
        int rslt = 0;  
        boolean done = false;
        while (Character.isDigit(_fr.sym) && !done){  
	        rslt = rslt * 10 + Character.digit(_fr.sym, 10);  
	        if(Character.isDigit(_fr.lookAhead())){
	        	_fr.next();
	        }else{
	        	done = true;
	        }
        }  
        return rslt;  
    }
    
    private void generateIdentifier(){
    	boolean done = false;
    	StringBuilder token = new StringBuilder();
    	while(Character.isLetterOrDigit(_fr.sym) && !done){
    		token.append(_fr.sym);
    		if(Character.isLetterOrDigit(_fr.lookAhead())){
    			_fr.next();
    		}else{
    			done = true;
    		}
    	}
    	String tok = token.toString();
    	if(tokenTable.containsKey(tok)){
            sym = tokenTable.get(tok);
        }else{
            sym = 61;
            name = tok;
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
}
