package scheduleSolver;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ICF;
import solver.constraints.LCF;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.Spaces;

public class Space {
	public final int participants;
	
	protected IntVar id;
	protected IntVar capacity;
	protected IntVar index;
	
	private Constraint constraint;

	Space(int[] ids, int participants, Solver solver, Spaces spaces) {
		this.participants = participants;
		
		if (ids == null) ids = spaces.ids;
		
		id = VariableFactory.enumerated("room ids", ids, solver);
		capacity = VariableFactory.enumerated("room capacities", spaces.capacities, solver);
		index = VariableFactory.bounded("room index", 0, spaces.n - 1, solver);
		
		Constraint idConstraint = ICF.element(id, spaces.ids, index);
		Constraint capConstraint = ICF.element(capacity, spaces.capacities, index);
		Constraint parConstraint = ICF.arithm(capacity, ">=", participants);
		
		constraint = LCF.and(idConstraint, capConstraint, parConstraint);
	}
	
	public Constraint getConstraint() {
		return constraint;
	}
	
	public Constraint diff(Space other) {
		//TODO: Should we use index or id here? 
		return ICF.arithm(index, "!=", other.index);
	}
	
	public IntVar getVar() { return index; }
	
	public int getId() {
		return id.getValue();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
