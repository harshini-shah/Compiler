public class Result{
        public Kind kind;
        public int value;
        //public int id;
        public String name;
        public int cond;
        public int fixupLocation;
        public int version;
        
        public Result() {   
        }
        public enum Kind{
            CONST, VAR, CONDN, INSTR
        }
        
        public Result(Result x) {
            this.kind = x.kind;
            this.value = x.value;
            this.fixupLocation = x.fixupLocation;
            //this.id = x.id;
            this.name = x.name;
            this.cond = x.cond;
            this.version = x.version;
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