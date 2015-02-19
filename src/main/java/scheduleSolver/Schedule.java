package scheduleSolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.LogicalConstraintFactory;
import solver.explanations.ExplanationFactory;
import solver.explanations.RecorderExplanationEngine;
import solver.trace.Chatterbox;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.Variable;
import solver.variables.impl.BitsetIntVarImpl;

public class Schedule {
	public final String name;
	
	public final Map<Integer, Event> events;
	public final Map<Integer, Space> spaces;
//	public final Map<Integer, Person> persons;
	
	private Solver solver;
	//private ConflictBasedBackjumping exp;
	private boolean modelBuilt = false;
	private boolean solved = false;
	
	private static final boolean DEBUG = true;
	
	public Schedule(String name, Event[] events,
			Space[] spaces /*, Map<Integer, Person> persons*/) {
		this.name = name;
		this.solver = new Solver(this.name);
		
//		ExplanationFactory.CBJ.plugin(solver, true);
//		solver.set(new RecorderExplanationEngine(solver));
		//exp = new ConflictBasedBackjumping(solver.getExplainer());
		// Then active end-user explanation
		//exp.activeUserExplanation(true);
		
		this.spaces = new HashMap<Integer, Space>(spaces.length);
		for (Space space : spaces) {
			this.spaces.put(space.ID, space);
		}
		
		this.events = new HashMap<Integer, Event>(events.length);
		for (Event event : events) {
			event.initialize(this.solver, this.spaces);
			this.events.put(event.ID, event);
		}
		
		//this.persons = persons;
	}
	
	public ArrayList<EventPOJO> getSolution(){
		ArrayList<EventPOJO> eps = new ArrayList<EventPOJO>();
		
		if(findSolution()){
			for(Map.Entry<Integer, Event> event : events.entrySet()){
				EventPOJO ep = new EventPOJO();
				Event e = event.getValue();
				ep.ID = e.getID();                        
				ep.days = e.getDays();                  
				ep.roomID = e.getSpaceID();                   
				ep.startTime = e.getStartTime();
				ep.wasFailure = false;
				eps.add(ep);
			}
		}
		else{
			EventPOJO ep = new EventPOJO();
			ep.wasFailure = true;
			eps.add(ep);
		}
		return eps;
	}
	
//	public Constraint getConstraintsEventsSpaces() {
//		ArrayList<IntVar> timeBlocks = new ArrayList<IntVar>();
//		for (Event event : this.events.values()) {
//			// 1. Extract domain from event.possibleStartTimes
//			//		Things to consider:
//			//		- Convert from human-friendly strings to integers
//			//		- Space's capacity
//			//		- Space's availability (can just ignore for now...)
//			//    For now, we will assume that possibleStartTimes are in
//			//      correct format already (010910 instead of T:09:10 for instance)
//			// 2. time0 = IntVar.enumerated(domain)
//			// 3. populate array of time_offset (c1_s1_t0, c1_s1_t1, c1_s1_t2...)
//			// 4. Do the same for all the events
//			// 5. create alldifferent(array) constraint
//			
//			// 1.
//			ArrayList<Integer> domain = new ArrayList<Integer>();
//			for (Time t : event.possibleStartTimes) {
//				Integer tmp = t.getInt() * 100;
//				for (Space space : this.spaces.values()) {
//					if (space.capacity >= event.maxParticipants) {
//						domain.add(tmp + space.ID);
//					}
//				}
//			}
//			
//			// 2.
//			IntVar time0 = VariableFactory.enumerated("start time",
//					Ints.toArray(domain), this.solver);
//			
//			// 3.
//			for (int i = 0; i < event.duration; i += 5) {
//				IntVar timeVar = VariableFactory.offset(time0, i * 100);
//				timeBlocks.add(timeVar);
//			}
//			
//			// 4. -- Continue the loop
//		}
//		
//		// 5.
//		return IntConstraintFactory.alldifferent((IntVar[]) timeBlocks.toArray());
//	}
	
	private void buildModel() {
		Event[] events_arr = this.events.values().toArray(new Event[0]);
		int n = events_arr.length;
		
		ArrayList<Constraint> constraintList = new ArrayList<Constraint>();
		
		for (int i = 0; i < n; i++) {
			constraintList.add(events_arr[i].getConstraint());
			for (int j = i + 1; j < n; j++) {
				constraintList.add(events_arr[i].notOverlap(events_arr[j]));
			}
//			for (Space space : this.spaces) {
//				constraint = LogicalConstraintFactory.and(constraint,
//						this.events[i].roomConstraint(space));
//			}
//			constraint = LogicalConstraintFactory.and(constraint, this.events[i].eventConstraint);
		}
		
		this.solver.post(LogicalConstraintFactory.and(
				constraintList.toArray(new Constraint[0])));
		
		modelBuilt = true;
	}
	
	private boolean findSolution() {
		if (!modelBuilt) buildModel();
		
//		if (DEBUG) Chatterbox.showDecisions(solver);
		
		solved = solver.findSolution();
		
		if (!solved && DEBUG) {
			//if (exp.getUserExplanation() == null)
				//System.out.println("Error: No explanation.");
			//else
				//System.out.println("Explain: " + exp.getUserExplanation());
			
			// Print the most recent conflict (that causes the solver fail)
			System.out.println(solver.getEngine().getContradictionException());
			//solver.getEngine().getContradictionException().printStackTrace();
			
			// For debugging
			printSolverData();
		}
		
		return solved;
	}
	
	private void printSolverData() {
		Chatterbox.printStatistics(solver);
		
		for (Variable variable : solver.getVars()) {
			String name = variable.getName();
            String value;
            if (variable instanceof IntVar) {
                IntVar intVar = (IntVar) variable;
                int val = intVar.getValue();
                value = new String(val + "");

            } else if (variable instanceof SetVar) {
                SetVar intVar = (SetVar) variable;
                int[] val = intVar.getValues();
                value = Arrays.toString(val);

            } else if (variable instanceof BitsetIntVarImpl) {
                BitsetIntVarImpl intVar = (BitsetIntVarImpl) variable;
                int val = intVar.getValue();
                value = new String(val + "");
            } else
                throw new RuntimeException();
            System.out.println(String.format("%-20s%-20s%-20s", name,
                    value, variable.getClass().getSimpleName()));
		}
		for (Constraint c : solver.getCstrs()) {
			System.out.println(c);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		HashMap<String, String[]> m = new HashMap<String, String[]>();
		m.put("M", new String[]{"0930"});
		
		HashMap<String, String[]> m2 = new HashMap<String, String[]>();
		m2.put("T", new String[]{"0930"});
		
		Event e0 = new Event(0 /*id*/, 50 /*maxParticipants*/,
				m /*startTimes*/, 50 /*duration*/,
				0 /*personId*/, 1 /*spaceId*/);
		
		Event e1 = new Event(1 /*id*/, 70 /*maxParticipants*/,
				m /*startTimes*/, 70 /*duration*/,
				1 /*personId*/, 1 /*spaceId*/);
		
		Event e2 = new Event(2 /*id*/, 50 /*maxParticipants*/,
				m2 /*startTimes*/, 50 /*duration*/,
				1 /*personId*//*, 1 /*spaceId*/);
		
		Space s0 = new Space(0, 60);
		Space s1 = new Space(1, 70);
		
		Schedule sc = new Schedule("test schedule", new Event[]{e0, e1, e2},
				new Space[] {s0, s1});
		
		boolean result = sc.findSolution();
		System.out.println("has_solution = " + result);
	}
	
	public class EventPOJO {
		public int ID;
		public char[] days;
		public int roomID;
		public String startTime;
		public boolean wasFailure;
	}
}
