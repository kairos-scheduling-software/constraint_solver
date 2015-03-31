package scheduleSolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import solver.Solver;
import solver.constraints.Constraint;
import solver.variables.BoolVar;
import util.ConstraintData;

public class EventConstraint {
	private Event e1;
	private Event e2;
	private BoolVar satisfied;
	
	private Constraint constraint;
	
	public final int id1;
	public final int id2;
	
	public EventConstraint(Event e1, Event e2, Constraint constraint) {
		this.e1 = e1;
		this.e2 = e2;
		this.id1 = e1.getId();
		this.id2 = e2.getId();
		this.satisfied = constraint.reif();
		this.constraint = constraint;
	}
	
	public EventConstraint(ConstraintData data, Solver solver,
			Map<Integer, Event> events) {
		this.id1 = data.id1;
		this.id2 = data.id2;
		initialize(solver, events, data.relation);
	}
	
	private void initialize(Solver solver, Map<Integer, Event> events, String relation) {
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
	
	public boolean isSatisfied() { return satisfied.getValue() != 0; }
	
	public static BoolVar[] getStatus(List<EventConstraint> ec) {
		ArrayList<BoolVar> arr = new ArrayList<BoolVar>();
		for (EventConstraint c : ec) {
			arr.add(c.satisfied);
		}
		return arr.toArray(new BoolVar[0]);
	}
}