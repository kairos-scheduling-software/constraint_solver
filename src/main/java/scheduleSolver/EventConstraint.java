package scheduleSolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import solver.Solver;
import solver.constraints.Constraint;
import solver.variables.BoolVar;

public class EventConstraint {
	public Event e1;
	public Event e2;
	public BoolVar satisfied;
	
	public Constraint constraint;
	
	public final int id1;
	public final int id2;
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
		this.id1 = e1.getID();
		this.id2 = e2.getID();
		this.satisfied = constraint.reif();
		this.constraint = constraint;
		this.constraintBuilt = true;
	}
	
	public void initialize(Solver solver, Map<Integer, Event> events) {
		if (constraintBuilt) return;
		Map<Integer, Event> e = events;
		e1 = e.get(id1);
		e2 = e.get(id2);
		if ((e1 == null) || (e2 == null))
			constraint = solver.TRUE;
		else switch (relation) {
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
				constraint = solver.TRUE;
		}
		this.satisfied = constraint.reif();
	}
	
	public boolean getStatus() { return satisfied.getValue() != 0; }
	
	public static BoolVar[] getStatus(List<EventConstraint> ec) {
		ArrayList<BoolVar> arr = new ArrayList<BoolVar>();
		for (EventConstraint c : ec) {
			arr.add(c.satisfied);
		}
		return arr.toArray(new BoolVar[0]);
	}
}