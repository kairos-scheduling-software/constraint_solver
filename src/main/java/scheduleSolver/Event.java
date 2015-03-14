package scheduleSolver;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.LCF;
import solver.constraints.LogicalConstraintFactory;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import util.Spaces;
import util.TimeData;

public class Event {
//	String name;						  /* name of the event, e.g. "Computer Systems" */
	int ID;							  /* event ID, e.g. cs4400 */
	
	Time time;
	Space space;
	
	int maxParticipants;	/* the maximum number of participants that can be included in the Event */
	int[] spaceIds;
	
	int personId;
//	Person person;			  /* any administrative-type people associated with the event, e.g teachers, speakers, etc */
	
	private Solver solver;
	private Constraint constraint;
	
	private boolean isPossible;
	
	public Event(int id, int maxParticipants, TimeData time,
			int personId) {
		this(id, maxParticipants, time, personId, null);
	}
	
	public Event(int id, int maxParticipants, TimeData time,
			int personId, int[] spaceIds) {
		this.ID = id;
		this.maxParticipants = maxParticipants;
		this.personId = personId;
		
		this.time = new Time(time);
		
		this.spaceIds = spaceIds;
	}
	
	public void initialize(Solver solver, Spaces spaces) {
		this.solver = solver;
		
//		isPossible = true;
		isPossible = getStatus(spaces);
		if (!isPossible) return;

		time.initialize(solver);
		space = new Space(spaceIds, maxParticipants, solver, spaces);
		constraint = buildConstraint(time, space, solver, spaces);
	}
	
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
	
	public Constraint getConstraint() {
		return getConstraint("self", null);
	}
	
	public IntVar[] getVars() {
		if (!isPossible) return new IntVar[0];
		return new IntVar[]{time.getVar(), space.getVar()};
	}
	
	public int getID() { return this.ID; }
	public int getSpaceID() { return space.getId(); }
	public String getDays() { return this.time.getDays(); }
	public String getStartTime() { return this.time.getStartTime(); }
	public int getDuration() { return time.getDuration(); }
	public int getMaxParticipants() { return maxParticipants; }
	public int getPerson() { return personId; }
	public boolean isPossible() { return isPossible; }
	
	//////// Helper functions ////////
	private boolean getStatus(Spaces spaces) {
		Solver solver = new Solver();
		Time time = new Time(this.time);
		time.initialize(solver);
		Space space = new Space(spaceIds, maxParticipants, solver, spaces);
		
		Constraint c = buildConstraint(time, space, solver, spaces);
		solver.set(IntStrategyFactory.random_value(getVars()));
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
				!(type.equals("self") || other.isPossible))
			return solver.TRUE;
		
		Constraint c;
		switch(type) {
			case "self":
				c = constraint;
				break;
			case "default":
				Constraint _space = this.space.diff(other.space);
				Constraint _time = this.time.notOverlap(other.time);
				
				if (this.personId == other.personId)
					c = _time;
				else
					c = LogicalConstraintFactory.or(_space, _time);
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
			default:
				c = solver.TRUE;
		}
		
		return c;
	}
}
