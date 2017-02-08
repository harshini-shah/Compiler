
public class Test {
	public static void main(String[] args){
		Scanner s = new Scanner("test.txt");
		while(s.sym != Token.eofToken){
			s.next();
			System.out.println(s.sym);
		}
	}
}
