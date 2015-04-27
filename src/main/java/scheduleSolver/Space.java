package scheduleSolver;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ICF;
import solver.constraints.LCF;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

public class Space {
	public final int participants;
	
	private IntVar id;
	private IntVar capacity;
	private IntVar index;
	
	private Constraint constraint;
	
	public final boolean ignore;
	
	public final int SPECIAL_ID = -1;

	public Space(int[] ids, int participants, Solver solver, Spaces spaces) {
		this.participants = participants;
		
		if (ids == null || ids.length == 0) ids = spaces.getIds();
		else if (ids.length > 1) ids = spaces.filterIds(ids);
		
		if (ids.length == 1 && ids[0] == SPECIAL_ID) {
			ignore = true;
			constraint = solver.TRUE;
			id = VariableFactory.fixed("room id", SPECIAL_ID, solver);
			index = VariableFactory.fixed("room index", -1, solver);
			return;
		} else ignore = false;
		
		id = VariableFactory.enumerated("room id", ids, solver);
		capacity = VariableFactory.enumerated("room capacity", spaces.getCapacities(ids), solver);
		index = VariableFactory.bounded("room index", 0, spaces.n - 1, solver);
		
		Constraint spaceConstraint = spaces.getConstraint(id, capacity, index);
		Constraint parConstraint = ICF.arithm(capacity, ">=", participants);
		
		constraint = LCF.and(spaceConstraint, parConstraint);
	}
	
	public Constraint getConstraint() {
		return constraint;
	}
	
	public Constraint diff(Space other) {
		if (this.ignore) return this.constraint;
		if (other.ignore) return other.constraint;
		return ICF.arithm(index, "!=", other.index);
	}
	
	public Constraint equals(Space other) {
		if (this.ignore) return this.constraint;
		if (other.ignore) return other.constraint;
		return ICF.arithm(index, "=", other.index);
	}
	
	public IntVar getVar() {
		return index;
	}
	
	public int getId() {
		return id.getValue();
	}

}
