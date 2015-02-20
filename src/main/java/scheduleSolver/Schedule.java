package scheduleSolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.primitives.Ints;

import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.trace.Chatterbox;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

public class Schedule {
	public final String name;
	
	public final Map<Integer, Event> events;
	public final Map<Integer, Space> spaces;
//	public final Map<Integer, Person> persons;
	
	private Solver solver;
	private IntVar sum;
	private int count;
	private boolean modelBuilt = false;
	private boolean solved = false;
	
	private ArrayList<EventConstraint> eventsConstraints;
	
	private static final boolean DEBUG = true;
	
	public Schedule(String name, Event[] events,
			Space[] spaces /*, Map<Integer, Person> persons*/) {
		this.name = name;
		this.solver = new Solver(this.name);
		
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
	
	private void buildModel() {
		Event[] events_arr = this.events.values().toArray(new Event[0]);
		int n = events_arr.length;
		
		/*
		(1) Event's possible time block not satisfied: Ignore that event
		(2) Conflict involves 2 events (a and b):
			- event a (or b) falls to category (1) -- all is good
			- else: return both, mark the conflict
		*/
		this.eventsConstraints = new ArrayList<EventConstraint>();
		
		for (int i = 0; i < n; i++) {
			eventsConstraints.add(new EventConstraint(i,
					events_arr[i].getConstraint()));
			for (int j = i + 1; j < n; j++) {
				eventsConstraints.add(new EventConstraint(i, j,
						events_arr[i].notOverlap(events_arr[j])));
			}
//			for (Space space : this.spaces) {
//				constraint = LogicalConstraintFactory.and(constraint,
//						this.events[i].roomConstraint(space));
//			}
//			constraint = LogicalConstraintFactory.and(constraint, this.events[i].eventConstraint);
		}
		
		this.count = eventsConstraints.size();
		this.sum = VariableFactory.bounded("total constraints", 0, count, solver);
		
		if (this.count > 0) {
			this.solver.post(IntConstraintFactory.sum(
					getEventsStatus(eventsConstraints)
					.toArray(new BoolVar[0]), sum));
		} else {
			this.solver.post(solver.TRUE);
		}
		
		modelBuilt = true;
	}
	
	private boolean findSolution() {
		if (!modelBuilt) buildModel();
		
//		if (DEBUG) Chatterbox.showDecisions(solver);
		
//		solved = solver.findSolution();
		this.solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, sum);
		
		solved = (sum.getValue() == count);
		
		if (!solved && DEBUG) {
//			System.out.println(solver.getEngine().getContradictionException());
			printSolverData();
		}
		
		return solved;
	}
	
	private void printSolverData() {
		Chatterbox.printStatistics(solver);
		
		for (EventConstraint e : eventsConstraints) {
//			System.out.println(b.getValue());
			boolean b = (e.satisfied.getValue() != 0);
			System.out.printf("%10s: %s\n", e.ids.toString(), b?"True":"False");
		}
		
		for (Event e : this.events.values()) {
			System.out.printf("ID:%2d, Space: %2d, %s-%s\n", e.getID(), e.getSpaceID(), Arrays.toString(e.getDays()), e.getStartTime());
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
	
	public class EventConstraint {
		public ArrayList<Integer> ids;
		public BoolVar satisfied;
		
		public EventConstraint(int id, Constraint constraint) {
			ids = new ArrayList<Integer>(1);
			ids.add(id);
			satisfied = constraint.reif();
		}
		
		public EventConstraint(int id1, int id2, Constraint constraint) {
			ids = new ArrayList<Integer>(2);
			ids.add(id1);
			ids.add(id2);
			satisfied = constraint.reif();
		}
		
		public EventConstraint(int[] ids, Constraint constraint) {
			this.ids = new ArrayList<Integer>();
			this.ids.addAll(Ints.asList(ids));
			satisfied = constraint.reif();
		}
	}
	
	private static List<BoolVar> getEventsStatus(List<EventConstraint> ec) {
		ArrayList<BoolVar> arr = new ArrayList<BoolVar>();
		for (EventConstraint c : ec) {
			arr.add(c.satisfied);
		}
		return arr;
	}
}
