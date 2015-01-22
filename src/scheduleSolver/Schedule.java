package scheduleSolver;

import java.util.ArrayList;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;

public class Schedule {
	public final String name;
	public final Event events[];
	public final Space spaces[];
	public final Person persons[];
	private Solver solver;
	
	public Schedule(String name, Event events[], Space spaces[], Person persons[]) {
		this.name = name;
		this.events = events;
		this.spaces = spaces;
		this.persons = persons;
		this.solver = new Solver(this.name);
	}
	
//	public Constraint getConstraintsEventsSpaces() {
//		Constraint tmp = IntConstraintFactory.TRUE(this.solver);
//		ArrayList<Constraint> constraints = new ArrayList<Constraint>();
//		for (Event event : this.events) {
//			//IntVariable
//		}
//		return tmp;
//	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public class Event {}
	public class Space {}
	public class Time {}

}