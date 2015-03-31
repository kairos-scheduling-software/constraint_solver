package scheduleSolver;

public class Person {
	public static final int DEFAULT_ID = -1;
	
	public final String name;
	public final int id;
	public final int restTime;
	
	public Person(int _id) {
		this("default person name", _id);
	}
	
	public Person(String _name, int _id) {
		this(_name, _id, 0);
	}
	
	public Person(String name, int id, int restTime) {
		this.name = name;
		this.id = id;
		this.restTime = (restTime >= 0 ? restTime : 0);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
