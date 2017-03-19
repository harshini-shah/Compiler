import java.util.ArrayList;
import java.util.List;

public class Result{
        public Kind kind;
        public int value;
        public String name;
        public int cond;
        public int fixupLocation;
        public int version;
        public int regNo;
        public List<Result> dimensions;
        
        public enum Kind{
            CONST, VAR, CONDN, INSTR, ARR, REG
        }
        
        public Result() {  
            dimensions = new ArrayList<Result>();
        }
        
        public Result(Result x) {
            this.kind = x.kind;
            this.value = x.value;
            this.fixupLocation = x.fixupLocation;
            this.name = x.name;
            this.cond = x.cond;
            this.version = x.version;
            dimensions = new ArrayList<Result>(x.dimensions);

        }
        
        @Override
    	public int hashCode(){
        	int returnValue = this.kind.hashCode();
        	if(this.kind == Kind.CONST)
        		returnValue += this.value;
        	else if(this.kind == Kind.INSTR)
        		returnValue += this.version;
        	return returnValue;
        }
        
        @Override
        public boolean equals(Object o){
        	if(o==null || !(o instanceof Result))
        		return false;
        	Result r = (Result) o;
        	boolean returnValue = false;
        	if(this.kind == Kind.INSTR && this.kind == r.kind && this.version == r.version)
        		returnValue = true;
        	else if(this.kind == Kind.CONST && this.kind == r.kind && this.value == r.value)
        		returnValue = true;
        	return returnValue;
        }
    }