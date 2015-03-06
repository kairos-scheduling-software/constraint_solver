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
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.objects.graphs.MultivaluedDecisionDiagram;

public class Time {
	/* members */
	
	private Map<Days, List<Integer>> daysTimes;
	private int duration;  // event's duration in minutes
	
	private IntVar indexVar;
	
	private IntVar[] dayVars;
	private IntVar startTime;
	
	private Constraint constraint;
	
	public Time(Map<String, String[]> startTimes, int duration) {
		this.daysTimes = new HashMap<Days, List<Integer>>();
		this.duration = duration;
		
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
		// Initialize day vars
		dayVars = new IntVar[7];
		for (int i = 0; i < 7; i++) {
//			dayVars[i] = VariableFactory.bounded("days", 0, 1, solver);
			dayVars[i] = VariableFactory.enumerated("days", new int[]{0, i+1}, solver);
		}
		
		// Initialize startTime var
		HashSet<Integer> totalTimes = new HashSet<Integer>();
		for (List<Integer> times : this.daysTimes.values()) {
			totalTimes.addAll(times);
		}
		this.startTime = VariableFactory.enumerated("startTime",
				Ints.toArray(totalTimes), solver);
		
		Tuples tuples = new Tuples();
		int index = 0;
		for (Entry<Days, List<Integer>> entry : daysTimes.entrySet()) {
			int[] daysArr = entry.getKey().getWeekArr();
			
			for (int i = 0; i < 7; i++) {
				daysArr[i] *= (i+1);
			}
			for (int t : entry.getValue()) {
				int[] vals = Arrays.copyOf(daysArr, 9);
				vals[7] = t;
				vals[8] = index;
				index++;
				tuples.add(vals);
			}
		}
		
		// Setup var and days-time constraint
		indexVar = VariableFactory.bounded("daytime var", 0, index - 1, solver);
		
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
		Constraint time1Constraint = IntConstraintFactory.arithm(this.startTime, ">=", other.startTime, "+", other.duration);
		Constraint time2Constraint = IntConstraintFactory.arithm(other.startTime, ">=", this.startTime, "+", this.duration);
		
		return LogicalConstraintFactory.or(daysConstraint, time1Constraint, time2Constraint);
	}

	public Constraint before(Time other) {
		Constraint timeConstraint = IntConstraintFactory.arithm(other.startTime, ">=", this.startTime, "+", this.duration);
		
		return timeConstraint;
	}
	
	public Constraint after(Time other) {
		Constraint timeConstraint = IntConstraintFactory.arithm(this.startTime, ">=", other.startTime, "+", other.duration);
		
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
	
	public int getDuration() { return duration; }
	
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
		
		public Days(int val) {
			this.val = val & ((1 << 7) - 1);
			
			initialize();
		}
		
		public String getDays() { return days; }
		
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
