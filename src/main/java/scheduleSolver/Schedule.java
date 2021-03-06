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
import solver.constraints.IntConstraintFactory;
import solver.search.limits.FailCounter;
import solver.search.loop.lns.LNSFactory;
import solver.search.loop.monitors.SMF;
import solver.search.strategy.IntStrategyFactory;
import solver.trace.Chatterbox;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.ConstraintData;
import util.EventData;
import util.ScheduleData;
import util.SpaceData;
import util.TimeData;

public class Schedule {
	public final String name;
	
	private final Map<Integer, Event> events;
	private final Spaces spaces;
	private final List<EventConstraint> constraints;
//	public final Map<Integer, Person> persons;
	
	private ScheduleData data;
	
	private Solver solver;
	private IntVar satisfiedCount;
	private boolean modelBuilt = false;
	
	private static final boolean DEBUG = false;
	
	public enum SolutionLevel {
		CONFLICTS, ALL_EVENTS, ALL_DATA
	}
	
//	public Schedule(String name, EventData[] events,
//			SpaceData[] spaces /*, Map<Integer, Person> persons*/) {
//		this(name, events, spaces, null);
//	}
//	
//	public Schedule(String name, EventData[] eData,
//			SpaceData[] sData, EventConstraint[] constraints) {
//		this.name = name;
//		this.solver = new Solver(this.name);
//		SMF.limitTime(this.solver, "2m");
//		
//		this.spaces = new Spaces(sData);
////		this.sData = sData;
//		
//		this.events = new HashMap<Integer, Event>(eData.length);
//		for (EventData e : eData) {
//			Event event = new Event(e, this.solver, this.spaces);
//			this.events.put(event.id, event);
//		}
//		
//		this.constraints = new ArrayList<EventConstraint>();
//		if (constraints != null) {
//			for (EventConstraint c : constraints)
//				this.constraints.add(c);
//		}
//	}
	
	public Schedule(ScheduleData _data) {
		data = _data;
		name = data.name;
		solver = new Solver(name);
		SMF.limitTime(solver, "2m");
		
		spaces = new Spaces(data.spaces);
		
		events = new HashMap<Integer, Event>(data.events.length);
		for (EventData e : data.events) {
			Event event = new Event(e, solver, spaces);
			events.put(event.getId(), event);
		}
		
		this.constraints = new ArrayList<EventConstraint>();
		if (data.constraints != null) {
			for (ConstraintData c : data.constraints)
				this.constraints.add(new EventConstraint(c, solver, events));
		}
	}
	
	public HashMap<String, Object> getSolution(SolutionLevel level) {
		boolean failure = !findSolution();
		HashMap<String, Object> jsonMap = new HashMap<String, Object>();
		
		ArrayList<Object> eventsList = new ArrayList<Object>();
		for(Map.Entry<Integer, Event> entry : events.entrySet()) {
			Event e = entry.getValue();
			Map<String, Object> map = getEvent(e, level);
			
			if (map != null) eventsList.add(map);
		}
		
		jsonMap.put("wasFailure", failure);
		jsonMap.put("EVENTS", eventsList);
		
		if (level == SolutionLevel.ALL_DATA) {
			ArrayList<Object> spacesList = new ArrayList<Object>();
			for(SpaceData s : data.spaces) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("id", s.getId());
				map.put("capacity", s.getCapacity());
				spacesList.add(map);
			}
			jsonMap.put("SPACES", spacesList);
		}
		
		return jsonMap;
	}
	
	private void buildModel() {
		Event[] events_arr = this.events.values().toArray(new Event[0]);
		int n = events_arr.length;
		
//		for (EventConstraint c : constraints) {
//			c.initialize(solver, events);
//		}
		
		for (int i = 0; i < n; i++) {
			this.solver.post(events_arr[i].getConstraint());
			
			for (int j = i + 1; j < n; j++) {
				if (events_arr[i].isSameTime(events_arr[j])) continue;
				constraints.add(new EventConstraint(events_arr[i], events_arr[j],
						events_arr[i].defaultConstraint(events_arr[j])));
			}
			
//			for (Space space : this.spaces) {
//				constraint = LogicalConstraintFactory.and(constraint,
//						this.events[i].roomConstraint(space));
//			}
//			constraint = LogicalConstraintFactory.and(constraint, this.events[i].eventConstraint);
		}
		
		this.satisfiedCount = VariableFactory.bounded("total constraints", 0, constraints.size(), solver);
		
		if (constraints.size() > 0) {
			this.solver.post(IntConstraintFactory.sum(
					EventConstraint.getStatus(constraints), satisfiedCount));
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
		
//		Constraint c = IntConstraintFactory.arithm(satisfiedCount, "=", constraints.size());
//		this.solver.post(c);
//		solved = this.solver.findSolution();
		
		this.solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, satisfiedCount);
		boolean solved = (satisfiedCount.getValue() == constraints.size());
		
		for (Event e : this.events.values()) {
			if (!e.isPossible()) {
				solved = false;
				break;
			}
		}
		
		if (DEBUG) {
			printSolverData();
		}
		
		return solved;
	}
	
	private void printSolverData() {
		Chatterbox.printStatistics(solver);
		
		int conflictCount = 0;
		for (EventConstraint e : constraints) {
			boolean b = (e.isSatisfied());
//			System.out.printf("[%2d-%2d]: %s\n", e.id1, e.id2, b?"True":"False");
			if (!b) conflictCount += 1;
		}
		System.out.println("Conflicts count: " + conflictCount);
		
		for (Event e : this.events.values()) {
			System.out.printf("ID:%2d, Space: %2d, %s-%s\n", e.getId(), e.getSpaceId(), e.getDays(), e.getStartTime());
		}
		
		
//		for (Constraint c : solver.getCstrs()) {
//			ESat esat = c.isSatisfied();
//			System.out.println(esat);
//			if (esat.equals(ESat.FALSE)) {
//				System.out.println("Failed constraint: " + c);
//			}
//		}
	}
	
	private List<Integer> getConflicts(Event e) {
		ArrayList<Integer> conflicts = new ArrayList<Integer>();
		for (EventConstraint c : constraints) {
			if (!c.isSatisfied()) {
				if (c.id1 == e.getId()) conflicts.add(c.id2);
				else if (c.id2 == e.getId()) conflicts.add(c.id1);
			}
		}
		
		return conflicts;
	}
	
	private Map<String, Object> getEvent(Event e, SolutionLevel level) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", e.getId());
		
		// Space + DayTime
		if (!e.isPossible()) {
			map.put("wasFailure", true);
			if (level != SolutionLevel.ALL_DATA) return map;
		} else if (level != SolutionLevel.CONFLICTS) {
			map.put("spaceId", e.getSpaceId());
			
			Map<String, Object> tm = new HashMap<String, Object>();
			if (level == SolutionLevel.ALL_EVENTS) {
				tm.put("days", e.getDays());
				tm.put("startTime", e.getStartTime());
			}
			else if (level == SolutionLevel.ALL_DATA) {
				Map<String, Object> startTmMap = new HashMap<String, Object>();
				startTmMap.put(e.getDays(), new String[]{e.getStartTime()});
				tm.put("startTimes", startTmMap);
				tm.put("duration", e.getDuration());
			}
			map.put("time", tm);
		}
		
		List<Integer> conflicts = getConflicts(e);
		if (conflicts.size() == 0 && level == SolutionLevel.CONFLICTS)
			return null;
		map.put("conflictsWith", conflicts);
		
		// Extra info
		if (level == SolutionLevel.ALL_DATA) {
			map.put("maxParticipants", e.getMaxParticipants());
			map.put("personId", e.getPerson());
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
		
		EventData e0 = new EventData(0 /*id*/, 50 /*maxParticipants*/,
				new TimeData(m /*startTimes*/, 50 /*duration*/),
				0 /*personId*/, new int[]{0} /*spaceId*/);
		
		EventData e1 = new EventData(1 /*id*/, 70 /*maxParticipants*/,
				new TimeData(m /*startTimes*/, 70 /*duration*/),
				1 /*personId*/, new int[]{1} /*spaceId*/);
		
		EventData e2 = new EventData(2 /*id*/, 80 /*maxParticipants*/,
				new TimeData(m2 /*startTimes*/, 50 /*duration*/),
				1 /*personId*/, null /*spaceId*/);
		
		SpaceData s0 = new SpaceData(0, 60);
		SpaceData s1 = new SpaceData(1, 70);
		
		ScheduleData data = new ScheduleData("test schedule", new EventData[]{e0, e1, e2},
				new SpaceData[] {s0, s1}, null);
		Schedule sc = new Schedule(data);
		
		Map<String, Object> jsonMap = sc.getSolution(SolutionLevel.ALL_DATA);
		
		Gson gson = new Gson();
		System.out.println(gson.toJson(jsonMap));
	}
}
