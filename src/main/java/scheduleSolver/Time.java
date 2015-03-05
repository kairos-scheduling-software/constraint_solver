package scheduleSolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.primitives.Ints;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.LogicalConstraintFactory;
import solver.constraints.extension.Tuples;
import solver.constraints.set.SetConstraintsFactory;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.VariableFactory;
import util.objects.graphs.MultivaluedDecisionDiagram;

public class Time {
	/* members */
	
	private Map<Days, List<Integer>> daysTimes;
	private int _duration;  // event's duration in minutes
	
	private IntVar[] WEEK;
	
	private IntVar duration;

	private IntVar[] vars;
	private IntVar[] dayVars;
	private IntVar startTime;
	
	private Constraint constraint;
	private Solver solver;
	
	public Time(Map<String, String[]> startTimes, int duration) {
		this.daysTimes = new HashMap<Days, List<Integer>>();
		this._duration = duration;
		
		for (Entry<String, String[]> entry : startTimes.entrySet()) {
			Days days = new Days(entry.getKey());
			
			String[] times = entry.getValue();
			ArrayList<Integer> value = new ArrayList<Integer>();
			
			for(int i = 0; i < times.length; i++){
				value.add(stringToTime(times[i]));
			}
			this.daysTimes.put(days, value);
		}
	}
	
	public void initialize(Solver solver) {
		this.solver = solver;
		
		// Initialize fixed variables (week vars, duration)
		WEEK = new IntVar[7];
		for (int i = 0; i < 7; i++)
			WEEK[i] = VariableFactory.fixed(i, solver);
		
		duration = VariableFactory.fixed(_duration, solver);
		
		// Initialize day vars
		dayVars = new IntVar[7];
		for (int i = 0; i < 7; i++) {
			dayVars[i] = VariableFactory.bounded("days", 0, 1, solver);
			dayVars[i] = VariableFactory.enumerated("days", new int[]{0, i}, solver);
		}
		
		// Initialize startTime var
		HashSet<Integer> totalTimes = new HashSet<Integer>();
		for (List<Integer> times : this.daysTimes.values()) {
			totalTimes.addAll(times);
		}
		this.startTime = VariableFactory.enumerated("startTime",
				Ints.toArray(totalTimes), solver);
		
		
		// Setup constraint for possible days-time
		vars = new IntVar[8];
		for (int i = 0; i < 7; i++) vars[i] = dayVars[i];
		vars[7] = this.startTime;
		
		Tuples tuples = new Tuples();
		for (Entry<Days, List<Integer>> entry : daysTimes.entrySet()) {
			int[] daysArr = entry.getKey().getWeekArr();
			for (int i = 0; i < 7; i++)
				daysArr[i] *= i;
			for (int t : entry.getValue()) {
				int[] vals = Arrays.copyOf(daysArr, 8);
				vals[7] = t;
				tuples.add(vals);
			}
		}
		constraint = IntConstraintFactory.mddc(
				vars, new MultivaluedDecisionDiagram(vars, tuples));
	}
	
	public IntVar[] getVars() { return vars; }
	
	public Constraint notOverlap(Time other) {
		IntVar[] dayArr = new IntVar[14];
		for (int i = 0; i < 7; i++) {
			dayArr[i] = this.dayVars[i];
			dayArr[i+7] = other.dayVars[i];
		}
		Constraint daysConstraint = IntConstraintFactory.alldifferent_except_0(dayArr);
		Constraint time1Constraint = IntConstraintFactory.arithm(this.startTime, ">=", other.startTime, "+", other._duration);
		Constraint time2Constraint = IntConstraintFactory.arithm(other.startTime, ">=", this.startTime, "+", this._duration);
		
		return LogicalConstraintFactory.or(daysConstraint, time1Constraint, time2Constraint);
		
//		int n = 7 * 2;
//		IntVar[] X = new IntVar[n];
//		IntVar[] Y = new IntVar[n];
//		IntVar[] W = new IntVar[n];
//		IntVar[] H = new IntVar[n];
//		for (int i = 0; i < 7; i++) {
//			X[i] = this.WEEK[i];
//			X[i + 7] = other.WEEK[i];
//			
//			Y[i] = VariableFactory.eq(this.startTime);
//			Y[i + 7] = VariableFactory.eq(other.startTime);
//			
//			W[i] = this.dayVars[i];
//			W[i + 7] = other.dayVars[i];
//			
//			H[i] = VariableFactory.eq(this.duration);
//			H[i + 7] = VariableFactory.eq(other.duration);
//		}
//		
//		return LogicalConstraintFactory.and(IntConstraintFactory.diffn(X, Y, W, H, true));
	}

	public Constraint before(Time other) {
		Constraint timeConstraint = IntConstraintFactory.arithm(other.startTime, ">=", this.startTime, "+", this._duration);
		
		return timeConstraint;
	}
	
	public Constraint after(Time other) {
		Constraint timeConstraint = IntConstraintFactory.arithm(this.startTime, ">=", other.startTime, "+", other._duration);
		
		return timeConstraint;
	}
		
	public Constraint getConstraint() { return this.constraint; }
	
	public String getDays() {
		int daysVal = 0;
		for (int i = 0; i < 7; i++)
			if (dayVars[i].getValue() > 0) daysVal |= (1 << i);
		
		Days days = new Days(daysVal);
		
		return days.getDays();
	}
	
	public String getStartTime(){
		int tm = this.startTime.getValue();
		
		return timeToString(tm);
	}
	
	private int stringToTime(String tmString) {
		int hour, min;
		try {
			hour = Integer.parseInt(tmString.substring(0, 2));
			min = Integer.parseInt(tmString.substring(2));
		} catch (Exception e) {
			throw new IllegalArgumentException();
		}
		
		return hour * 60 + min;
	}
	
	private String timeToString(int tm) {
		int hour, min;
		
		min = tm % 60;
		hour = tm / 60;
		
		return String.format("%02d%02d", hour, min);
	}
	
	private static class Days {
		private String days;
		private HashSet<Integer> daySet;
		private int val;
		
		private static String week = "MTWHFSU";
		
		public Days(String dayStr) {
			val = 0;
			for (char c : dayStr.toUpperCase().toCharArray()) {
				int index = week.indexOf(c);
				if (index >= 0) val |= (1 << index);
			}
			
			initialize();
		}
		
		public Days(int[] dayArr) {
			val = 0;
			for (int index : dayArr)
				if (index >= 0 && index < 7) val |= (1 << index);
			
			initialize();
		}
		
		public Days(int val) {
			this.val = val & ((1 << 7) - 1);
			
			initialize();
		}
		
		public String getDays() { return days; }
		
		public int[] getArr() {
			int[] arr = Ints.toArray(daySet);
			Arrays.sort(arr);
			for (int d : arr) {
				days += week.charAt(d);
			}
			return arr;
		}
		
		public int[] getWeekArr() {
			int[] arr = new int[7];
			Arrays.fill(arr, 0);
			for (int i = 0; i < 7; i++) {
				if (daySet.contains(i)) arr[i] = 1;
			}
			return arr;
		}
		
		public int hashCode() { return ((Integer) val).hashCode(); }
		public boolean equals(Object other) {
			if (other instanceof Days) {
				return (this.val == ((Days) other).val);
			} else return false;
		}
		
		private void initialize() {
			daySet = new HashSet<Integer>();
			for (int i = 0; i < 7; i++)
				if ((val & (1 << i)) != 0) daySet.add(i);
			
			days = "";
			int[] arr = Ints.toArray(daySet);
			Arrays.sort(arr);
			for (int index : arr) {
				days += week.charAt(index);
			}
		}
	}
}
