package scheduleSolver;

import java.util.HashSet;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.LCF;
import solver.search.strategy.ISF;
import solver.variables.IntVar;

import util.EventData;

public class Event {
	private EventData data;
	
	private Time time;
	private Space space;
	
//	int personId;
//	Person person;			  /* any administrative-type people associated with the event, e.g teachers, speakers, etc */
	
	private Solver solver;
	private Constraint constraint;
	
	private boolean isPossible;
	private HashSet<Integer> sameTimeList;
	
//	public Event(int id, int maxParticipants, TimeData time,
//			int personId) {
//		this(id, maxParticipants, time, personId, null);
//	}
//	
//	public Event(int id, int maxParticipants, TimeData time,
//			int personId, int[] spaceIds) {
//		this.id = id;
//		this.maxParticipants = maxParticipants;
//		this.personId = personId;
//		
//		this.time = new Time(time);
//		
//		this.spaceIds = spaceIds;
//	}
	
	public Event(EventData data, Solver solver, Spaces spaces) {
		this.data = data;
		
		this.time = new Time(data.time, solver);
		
//		initialize(solver, spaces);
		this.solver = solver;
		
		sameTimeList = new HashSet<Integer>();
		
		isPossible = isFeasible(spaces);
		if (!isPossible) return;

		space = new Space(data.spaceIds, data.maxParticipants, solver, spaces);
		constraint = buildConstraint(time, space, solver, spaces);
	}
	
//	private void initialize(Solver solver, Spaces spaces) {
//		this.solver = solver;
//		
//		isPossible = isFeasible(spaces);
//		if (!isPossible) return;
//
//		space = new Space(data.spaceIds, data.maxParticipants, solver, spaces);
//		constraint = buildConstraint(time, space, solver, spaces);
//	}
	
	public Constraint defaultConstraint(Event other) {
		return getConstraint("default", other);
	}
	
	public Constraint before(Event other) {
		return getConstraint("before", other);
	}
	
	public Constraint after(Event other) {
		return getConstraint("after", other);
	}
	
	public Constraint notOverlap(Event other) {
		return getConstraint("notOverlap", other);
	}
	
	public Constraint sameTime(Event other, boolean cache) {
		if (cache) {
			this.sameTimeList.add(other.getId());
			other.sameTimeList.add(this.getId());
		}
		return getConstraint("sameTime", other);
	}
	
	public boolean isSameTime(Event other) {
		return sameTimeList.contains(other.getId());
	}
	
	public Constraint getConstraint() {
		return getConstraint("self", null);
	}
	
	public IntVar[] getVars() {
//		ArrayList<IntVar> vars = new ArrayList<IntVar>();
//		if (isPossible) {
//			IntVar var;
//			var = time.getVar();
//			if (var != null) vars.add(var);
//			var = space.getVar();
//			if (var != null) vars.add(var);
//		}
//		return vars.toArray(new IntVar[0]);
		if (!isPossible) return new IntVar[0];
		else return new IntVar[] {time.getVar(), space.getVar()};
	}
	
	public int getId() { return data.id; }
	public int getMaxParticipants() { return data.maxParticipants; }
	public int getPerson() { return data.personId; }
	
	public int getSpaceId() { return space.getId(); }
	public String getDays() { return time.getDays(); }
	public String getStartTime() { return time.getStartTime(); }
	public int getDuration() { return time.getDuration(); }
	public boolean isPossible() { return isPossible; }
	
	//////// Helper functions ////////
	private boolean isFeasible(Spaces spaces) {
		Solver solver = new Solver();
		Time time = new Time(this.time, solver);
		Space space = new Space(data.spaceIds, data.maxParticipants, solver, spaces);
		
		Constraint c = buildConstraint(time, space, solver, spaces);
		solver.set(ISF.random_value(getVars()));
		solver.post(c);
		
		return solver.findSolution();
	}
	
	private Constraint buildConstraint(Time time, Space space, Solver solver, Spaces spaces) {
		// Set up event constraints
		Constraint _timeConstraint = time.getConstraint();
		Constraint _spaceConstraint = space.getConstraint();
		Constraint _constraint = LCF.and(_timeConstraint, _spaceConstraint);
		
		return _constraint;
	}
	
	private Constraint getConstraint(String type, Event other) {
		if (!isPossible ||
				(!type.equals("self") && !other.isPossible))
			return solver.TRUE;
		
		Constraint c, _space, _time;
		switch(type) {
			case "self":
				c = constraint;
				break;
			case "default":
				_space = this.space.diff(other.space);
				_time = this.time.notOverlap(other.time);
				// time || (space && person)
				if (this.data.personId == other.data.personId &&
						this.data.personId != Person.DEFAULT_ID)
					c = _time;
				else
					c = LCF.or(_space, _time);
				break;
			case "before":
				c = this.time.before(other.time);
				break;
			case "after":
				c = this.time.after(other.time);
				break;
			case "notOverlap":
				c = this.time.notOverlap(other.time);
				break;
			case "sameTime":
				_space = this.space.equals(other.space);
				_time = this.time.sameTime(other.time);
				// time || (space && person)
				if (this.data.personId != other.data.personId &&
						this.data.personId != Person.DEFAULT_ID &&
						other.data.personId != Person.DEFAULT_ID)
					c = solver.FALSE;
				else
					c = LCF.and(_space, _time);
				break;
			default:
				c = solver.TRUE;
		}
		
		return c;
	}
}
