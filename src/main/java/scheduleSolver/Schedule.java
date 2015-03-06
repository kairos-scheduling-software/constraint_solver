package scheduleSolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.google.gson.Gson;

import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.search.limits.FailCounter;
import solver.search.loop.lns.LNSFactory;
import solver.search.strategy.IntStrategyFactory;
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
	private IntVar satisfiedCount;
	private boolean modelBuilt = false;
	private boolean solved = false;
	
	private ArrayList<EventConstraint> constraintList;
	
	private static final boolean DEBUG = false;
	
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
	}
	
	public HashMap<String, Object> getSolution() {
		boolean failure = !findSolution();
		
		ArrayList<Object> eventsList = new ArrayList<Object>();
		for(Map.Entry<Integer, Event> entry : events.entrySet()) {
			Event e = entry.getValue();
			
			ArrayList<Integer> conflicts = new ArrayList<Integer>();
			for (EventConstraint c : constraintList) {
				if (c.satisfied.getValue() == 0) {
					if (c.id1 == e.getID()) conflicts.add(c.id2);
					else if (c.id2 == e.getID()) conflicts.add(c.id1);
				}
			}
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("ID", e.getID());
			map.put("days", e.getDays());
			map.put("roomID", e.getSpaceID());
			map.put("startTime", e.getStartTime());
			map.put("conflictsWith", conflicts);
			
			eventsList.add(map);
		}
		
		HashMap<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("wasFailure", failure);
		jsonMap.put("EVENT", eventsList);
		
		return jsonMap;
	}
	
	public HashMap<String, Object> getSolution2() {
		boolean failure = !findSolution();
		
		ArrayList<Object> eventsList = new ArrayList<Object>();
		for(Map.Entry<Integer, Event> entry : events.entrySet()) {
			Event e = entry.getValue();
			
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("id", e.getID());
			map.put("space", e.getSpaceID());
			
			Map<String, Object> tmMap = new HashMap<String, Object>();
			tmMap.put(e.getDays(), new String[]{e.getStartTime()});
			map.put("pStartTm", tmMap);
			
			map.put("duration", e.getDuration());
			map.put("max_participants", e.getMaxParticipants());
			map.put("persons", e.getPerson());
			
			ArrayList<Integer> conflicts = new ArrayList<Integer>();
			for (EventConstraint c : constraintList) {
				if (c.satisfied.getValue() == 0) {
					if (c.id1 == e.getID()) conflicts.add(c.id2);
					else if (c.id2 == e.getID()) conflicts.add(c.id1);
				}
			}
			map.put("conflictsWith", conflicts);
			
			eventsList.add(map);
		}
		
		ArrayList<Object> spacesList = new ArrayList<Object>();
		for(Map.Entry<Integer, Space> entry : spaces.entrySet()) {
			Space s = entry.getValue();
			
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("id", s.getID());
			map.put("capacity", s.getCapacity());
			spacesList.add(map);
		}
		
		HashMap<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("wasFailure", failure);
		jsonMap.put("EVENT", eventsList);
		jsonMap.put("SPACE", spacesList);
		
//		for (EventConstraint c : constraintList) {
//			if ((c.id1 == 4 && c.id2 == 81) ||
//				(c.id1 == 81 && c.id2 == 4)) {
//				if (!c.satisfied.isInstantiated()) {
//					System.out.println("Var not instantiated!");
//				}
//				System.out.println("constraint satisfied = " + c.satisfied.getValue());
//			}
//		}
//		
//		System.out.println("events count: " + events.size());
//		IntVar[] vars = events.get(4).getVars();
//		for (int i = 0; i < 7; i++) {
//			System.out.print(vars[i].getValue() + ":");
//		}
//		System.out.println(vars[7].getValue());
//		
//		vars = events.get(81).getVars();
//		for (int i = 0; i < 7; i++) {
//			System.out.print(vars[i].getValue() + ":");
//		}
//		System.out.println(vars[7].getValue());
		
		return jsonMap;
	}
	
	private void buildModel() {
		Event[] events_arr = this.events.values().toArray(new Event[0]);
		int n = events_arr.length;
		
		this.constraintList = new ArrayList<EventConstraint>();
		
		for (int i = 0; i < n; i++) {
			this.solver.post(events_arr[i].getConstraint());
			
			for (int j = i + 1; j < n; j++) {
				constraintList.add(new EventConstraint(events_arr[i].ID, events_arr[j].ID,
						events_arr[i].notOverlap(events_arr[j])));
			}
			
//			for (Space space : this.spaces) {
//				constraint = LogicalConstraintFactory.and(constraint,
//						this.events[i].roomConstraint(space));
//			}
//			constraint = LogicalConstraintFactory.and(constraint, this.events[i].eventConstraint);
		}
		
		this.satisfiedCount = VariableFactory.bounded("total constraints", 0, constraintList.size(), solver);
		
		if (constraintList.size() > 0) {
			this.solver.post(IntConstraintFactory.sum(
					getConstraintsStatus(constraintList)
					.toArray(new BoolVar[0]), satisfiedCount));
		}
		
		modelBuilt = true;
	}
	
	private boolean findSolution() {
		if (!modelBuilt) buildModel();
		
//		if (DEBUG) Chatterbox.showDecisions(solver);
		
		List<IntVar> vars = new ArrayList<IntVar>();
		
		for (Event e : events.values()) {
			vars.addAll(Arrays.asList(e.getVars()));
		}
		
		LNSFactory.rlns(solver, vars.toArray(new IntVar[0]), 30, 20140909L, new FailCounter(100));
		
		solver.set(IntStrategyFactory.random_value(vars.toArray(new IntVar[0])));
		
		this.solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, satisfiedCount);
		solved = (satisfiedCount.getValue() == constraintList.size());
		
		if (!solved && DEBUG) {
//			System.out.println(solver.getEngine().getContradictionException());
			printSolverData();
		}
		
		return solved;
	}
	
	private void printSolverData() {
		Chatterbox.printStatistics(solver);
		
		for (EventConstraint e : constraintList) {
			boolean b = (e.satisfied.getValue() != 0);
			System.out.printf("[%2d-%2d]: %s\n", e.id1, e.id2, b?"True":"False");
		}
		
		for (Event e : this.events.values()) {
			System.out.printf("ID:%2d, Space: %2d, %s-%s\n", e.getID(), e.getSpaceID(), e.getDays(), e.getStartTime());
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
		
//		boolean result = sc.findSolution();
//		System.out.println("has_solution = " + result);
		
		Map<String, Object> jsonMap = sc.getSolution2();
		
		Gson gson = new Gson();
		System.out.println(gson.toJson(jsonMap));
	}
	
	public class EventPOJO {
		public int ID;
		public String days;
		public int roomID;
		public String startTime;
		public boolean wasFailure;
		public int[] conflictsWith;
	}
	
	public class EventConstraint {
		public int id1;
		public int id2;
		public BoolVar satisfied;
		
//		public Constraint constraint;
		
		public EventConstraint(int id1, int id2, Constraint constraint) {
			this.id1 = id1;
			this.id2 = id2;
			satisfied = constraint.reif();
//			this.constraint = constraint;
		}
	}
	
	private static List<BoolVar> getConstraintsStatus(List<EventConstraint> ec) {
		ArrayList<BoolVar> arr = new ArrayList<BoolVar>();
		for (EventConstraint c : ec) {
			arr.add(c.satisfied);
		}
		return arr;
	}
}
