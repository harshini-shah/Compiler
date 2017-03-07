public class Result{
        public Kind kind;
        public int value;
        public String name;
        public int cond;
        public int fixupLocation;
        public int version;
        
        public enum Kind{
            CONST, VAR, CONDN, INSTR
        }
        
        public Result() {   
        }
        
        public Result(Result x) {
            this.kind = x.kind;
            this.value = x.value;
            this.fixupLocation = x.fixupLocation;
            this.name = x.name;
            this.cond = x.cond;
            this.version = x.version;
        }
    }