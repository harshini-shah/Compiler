
public class ExpressionOperands {
	private Result op1;
	private Result op2;
	private boolean commutative;
	
	public ExpressionOperands(Result op1, Result op2){
		this.op1 = op1;
		this.op2 = op2;
		this.commutative = false;
	}
	
	public Result getOp1() {
		return op1;
	}
	public void setOp1(Result op1) {
		this.op1 = op1;
	}
	public Result getOp2() {
		return op2;
	}
	public void setOp2(Result op2) {
		this.op2 = op2;
	}
	public boolean getCommutative(){
		return this.commutative;
	}
	public void setCommutative(boolean value){
		this.commutative = value;
	}
	
	
	@Override
	public int hashCode(){
		return op1.hashCode() + op2.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(o==null || !(o instanceof ExpressionOperands))
			return false;
		ExpressionOperands eop = (ExpressionOperands)o;
		boolean returnValue = false;
		if(!commutative && op1.equals(eop.getOp1()) && op2.equals(eop.getOp2())){
			returnValue = true;
		}else if(commutative){
			if((op1.equals(eop.getOp1()) && op2.equals(eop.getOp2())) || (op1.equals(eop.getOp2()) && op2.equals(eop.getOp1()))){
				returnValue = true;
			}
		}
		return returnValue;
	}
}
