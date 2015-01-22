package scheduleSolver;

import java.util.ArrayList;

import com.google.common.primitives.Ints;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

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
	
	public Constraint getConstraintsEventsSpaces() {
		ArrayList<IntVar> timeBlocks = new ArrayList<IntVar>();
		for (Event event : this.events) {
			// 1. Extract domain from event.possibleStartTimes
			//		Things to consider:
			//		- Convert from human-friendly strings to integers
			//		- Space's capacity
			//		- Space's availability (can just ignore for now...)
			//    For now, we will assume that possibleStartTimes are in
			//      correct format already (010910 instead of T:09:10 for instance)
			// 2. time0 = IntVariable.enumerated(domain)
			// 3. populate array of time_offset (c1_s1_t0, c1_s1_t1, c1_s1_t2...)
			// 4. Do the same for all the events
			// 5. create alldifferent(array) constraint
			
			// 1.
			ArrayList<Integer> domain = new ArrayList<Integer>();
			for (String t : event.possibleStartTimes) {
				Integer tmp = Integer.parseInt(t) * 100;
				for (Space space : this.spaces) {
					if (space.capacity >= event.maxParticipants) {
						domain.add(tmp + space.id);
					}
				}
			}
			
			// 2.
			IntVar time0 = VariableFactory.enumerated("start time",
					Ints.toArray(domain), this.solver);
			
			// 3.
			for (int i = 0; i < event.duration; i += 5) {
				IntVar timeVar = VariableFactory.offset(time0, i);
				timeBlocks.add(timeVar);
			}
			
			// 4. -- Continue the loop
		}
		
		// 5.
		return IntConstraintFactory.alldifferent((IntVar[]) timeBlocks.toArray());
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public class Space {
		int id, capacity;
	}

}