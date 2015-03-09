package scheduleSolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
	public final List<EventConstraint> constraints;
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
		
		this.constraints = null;
	}
	
	public Schedule(String name, Event[] events,
			Space[] spaces, EventConstraint[] constraints) {
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
		
		this.constraints = new ArrayList<EventConstraint>(constraints.length);
		for (EventConstraint c : constraints) {
			this.constraints.add(c);
		}
	}
	
	public HashMap<String, Object> getSolution(boolean full) {
		boolean failure = !findSolution();
		
		ArrayList<Object> eventsList = new ArrayList<Object>();
		for(Map.Entry<Integer, Event> entry : events.entrySet()) {
			Event e = entry.getValue();
			Map<String, Object> map = getEvent(e, full);
			
			eventsList.add(map);
		}
		
		HashMap<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("wasFailure", failure);
		jsonMap.put("EVENT", eventsList);
		
		if (full) {
			ArrayList<Object> spacesList = new ArrayList<Object>();
			for(Map.Entry<Integer, Space> entry : spaces.entrySet()) {
				Space s = entry.getValue();
				
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("id", s.getID());
				map.put("capacity", s.getCapacity());
				spacesList.add(map);
			}
			jsonMap.put("SPACE", spacesList);
		}
		
		return jsonMap;
	}
	
	private void buildModel() {
		Event[] events_arr = this.events.values().toArray(new Event[0]);
		int n = events_arr.length;
		
		this.constraintList = new ArrayList<EventConstraint>();
		
		for (int i = 0; i < n; i++) {
			this.solver.post(events_arr[i].getConstraint());
			
			for (int j = i + 1; j < n; j++) {
				constraintList.add(new EventConstraint(events_arr[i], events_arr[j],
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
		
		LNSFactory.rlns(solver, vars.toArray(new IntVar[0]),
				30, (new Random()).nextLong(), new FailCounter(100));
		
		solver.set(IntStrategyFactory.random_value(vars.toArray(new IntVar[0])));
		
		this.solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, satisfiedCount);
		solved = (satisfiedCount.getValue() == constraintList.size());
		
		if (DEBUG) {
			printSolverData();
		}
		
		return solved;
	}
	
	private void printSolverData() {
		Chatterbox.printStatistics(solver);
		
		int conflictCount = 0;
		for (EventConstraint e : constraintList) {
			boolean b = (e.satisfied.getValue() != 0);
//			System.out.printf("[%2d-%2d]: %s\n", e.id1, e.id2, b?"True":"False");
			if (!b) conflictCount += 1;
		}
		System.out.println("Conflicts count: " + conflictCount);
		
		for (Event e : this.events.values()) {
			System.out.printf("ID:%2d, Space: %2d, %s-%s\n", e.getID(), e.getSpaceID(), e.getDays(), e.getStartTime());
		}
	}
	
	private List<Integer> getConflicts(Event e) {
		ArrayList<Integer> conflicts = new ArrayList<Integer>();
		for (EventConstraint c : constraintList) {
			if (c.satisfied.getValue() == 0) {
				if (c.id1 == e.getID()) conflicts.add(c.id2);
				else if (c.id2 == e.getID()) conflicts.add(c.id1);
			}
		}
		
		return conflicts;
	}
	
	private Map<String, Object> getEvent(Event e, boolean full) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (full) {
			map.put("id", e.getID());
			map.put("space", e.getSpaceID());
			
			Map<String, Object> tmMap = new HashMap<String, Object>();
			tmMap.put(e.getDays(), new String[]{e.getStartTime()});
			map.put("pStartTm", tmMap);
			
			map.put("duration", e.getDuration());
			map.put("max_participants", e.getMaxParticipants());
			map.put("persons", e.getPerson());
			
			map.put("conflictsWith", getConflicts(e));
		} else {
			map.put("ID", e.getID());
			map.put("days", e.getDays());
			map.put("roomID", e.getSpaceID());
			map.put("startTime", e.getStartTime());
			map.put("conflictsWith", getConflicts(e));
		}
		return map;
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
		
		Map<String, Object> jsonMap = sc.getSolution(true);
		
		Gson gson = new Gson();
		System.out.println(gson.toJson(jsonMap));
	}
	
	public static class EventConstraint {
		public Event e1;
		public Event e2;
		public BoolVar satisfied;
		
		public Constraint constraint;
		
		private int id1;
		private int id2;
		private String relation;
		private boolean constraintBuilt;
		
		public EventConstraint(int id1, int id2, String relation) {
			this.id1 = id1;
			this.id2 = id2;
			this.relation = relation;
		}
		
		public EventConstraint(Event e1, Event e2, Constraint constraint) {
			this.e1 = e1;
			this.e2 = e2;
			this.satisfied = constraint.reif();
			this.constraint = constraint;
			this.constraintBuilt = true;
		}
		
		public void initialize(Schedule schedule) {
			if (constraintBuilt) return;
			Map<Integer, Event> e = schedule.events;
			switch (relation) {
				case "<":
					constraint = e1.before(e2);
					break;
				case ">":
					constraint = e1.after(e2);
					break;
				case "!":
					constraint = e1.notOverlap(e2);
					break;
				default:
					constraint = schedule.solver.TRUE;
			}
			this.satisfied = constraint.reif();
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
