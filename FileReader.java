import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileReader {
    private Scanner _sc;
    private String _line="";
    private int _index=0;
    
    public char sym;
    public FileReader(String fileName) {
        try {
            _sc = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public void next() {
    	next(false);
    }
    
    public void next(boolean ignoreLine) {
    	if(ignoreLine || _index >= _line.length()){
    		if(_sc.hasNextLine()){
	        	_line = _sc.nextLine().trim();
	        	_index = 0;
	        }else{
	            _line = null;
	        }
    	}
    	if(_line != null){
    		sym = _line.charAt(_index++);
    	}else{
    		sym = Token.eofToken;
    	}
    }
    
    public char lookAhead(){
    	char result='\n';
    	if(_index != _line.length()){
    		result = _line.charAt(_index);
    	}
    	return result;
    }
    
    public void error(String errorMsg) {
        System.out.println("ERROR in FileReader Class: " + errorMsg);
    }
}