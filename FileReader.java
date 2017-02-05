import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileReader {
    private Scanner _sc;
    
    public char sym;
    public FileReader(String fileName) {
        try {
            _sc = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public void next() {
        if(_sc.hasNextByte()){
            sym = (char)_sc.nextByte();
        }else{
            sym= Token.eofToken;
        }
    }
    
    public void error(String errorMsg) {
        System.out.println("ERROR in FileReader Class: " + errorMsg);
    }
}
