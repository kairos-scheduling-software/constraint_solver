package scheduleSolver;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.LogicalConstraintFactory;
import solver.constraints.extension.Tuples;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.TimeData;
import util.objects.graphs.MultivaluedDecisionDiagram;

public class Time {
	/* members */
	
	private TimeData time;
	
	private IntVar indexVar;
	private IntVar[] dayVars;
	private IntVar startTime;
	
	private Constraint constraint;
	
	public Time(TimeData time, Solver solver) {
		this.time = time;
		initialize(solver);
	}
	
	public Time(Time other, Solver solver) {
		this(other.time, solver);
	}
	
//	public Time(Map<String, String[]> startTimes, int duration) {
//		time = new TimeData(startTimes, duration);
//	}
	
	private void initialize(Solver solver) {
		// Initialize day vars
		dayVars = new IntVar[7];
		for (int i = 0; i < 7; i++) {
			dayVars[i] = VariableFactory.enumerated("days", new int[]{0, i+1}, solver);
		}
		
		// Initialize startTime var
		this.startTime = VariableFactory.enumerated("startTime",
				time.getPossibleTimes(), solver);
		
		Tuples tuples = time.getTuples();
		
		// Setup var and days-time constraint
		indexVar = VariableFactory.bounded("daytime var", 0, time.getCount() - 1, solver);
		
		IntVar[] vars = new IntVar[9];
		for (int i = 0; i < 7; i++) vars[i] = dayVars[i];
		vars[7] = this.startTime;
		vars[8] = indexVar;
		constraint = IntConstraintFactory.mddc(
				vars, new MultivaluedDecisionDiagram(vars, tuples));
	}
	
	public IntVar getVar() { return indexVar; }
	
	public Constraint notOverlap(Time other) {
		IntVar[] dayArr = new IntVar[14];
		for (int i = 0; i < 7; i++) {
			dayArr[i] = this.dayVars[i];
			dayArr[i+7] = other.dayVars[i];
		}
		Constraint daysConstraint = IntConstraintFactory.alldifferent_except_0(dayArr);
		Constraint time1Constraint = this.before(other);
		Constraint time2Constraint = this.after(other);
		
		return LogicalConstraintFactory.or(daysConstraint, time1Constraint, time2Constraint);
	}

	public Constraint before(Time other) {
		Constraint timeConstraint = IntConstraintFactory.arithm(other.startTime, ">=", this.startTime, "+", this.getDuration());
		
		return timeConstraint;
	}
	
	public Constraint after(Time other) {
		return IntConstraintFactory.arithm(this.startTime, ">=", other.startTime, "+", other.getDuration());
	}
		
	public Constraint getConstraint() { return this.constraint; }
	
	public String getDays() {
		int daysVal = 0;
		for (int i = 0; i < 7; i++)
			if (dayVars[i].getValue() > 0) daysVal |= (1 << i);
		
		return TimeData.getDays(daysVal);
	}
	
	public String getStartTime(){
		int tm = this.startTime.getValue();
		
		return TimeData.timeToString(tm);
	}
	
	public int getDuration() { return time.getDuration(); }
}
