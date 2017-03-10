import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Identifier{
//    public int lineNum;
//    public int id;
    public String name;
    
    public static enum Type {VAR, ARR}
    
    public Type type;
    
    public List<Integer> dimensions;
//    List<Identifier> params;
    
//    public Identifier(Type type, String name, int id){
//        this.id = id;
//        this.name = name;
//        this.type =type;
//        if(type == Type.FUNC){
//            params = new LinkedList<Identifier>();
//        }else if(type == Type.ARR){
//            dimensions = new ArrayList<Integer>();
//        }
//    }
//    
    
    public Identifier() {
        
    }
    
    public Identifier(Identifier ident){
//        this.id = ident.id;
        this.name = ident.name;
        this.type = ident.type;
        
        if(type == Type.ARR){
            this.dimensions = new ArrayList<Integer>(ident.dimensions);
        }
        
//        if(type == Type.FUNC){
//            this.params = new LinkedList<Identifier>(ident.params);
//        }else if(type == Type.ARR){
//            this.dimensions = new ArrayList<Integer>(ident.dimensions);
//        }
    }
}
