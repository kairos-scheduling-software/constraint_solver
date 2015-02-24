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
import solver.constraints.set.SetConstraintsFactory;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.VariableFactory;

public class Time {
	/* members */
	
	private Map<Days, List<Integer>> daysTimes;
	private int duration;  // event's duration in minutes
	
	private SetVar daySet;
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
		this.daySet = VariableFactory.set("TimeBlock", 0, 6, solver);
		
		HashSet<Integer> totalTimes = new HashSet<Integer>();
		for (List<Integer> times : this.daysTimes.values()) {
			totalTimes.addAll(times);
		}
		this.startTime = VariableFactory.enumerated("startTime",
				Ints.toArray(totalTimes), solver);
		
		ArrayList<Constraint> conArr = new ArrayList<Constraint>();
		for (Entry<Days, List<Integer>> entry : daysTimes.entrySet()) {
			int[] daysArr = entry.getKey().getArr();
			
			SetVar daySet = VariableFactory.fixed("days set",
					daysArr, solver);
			
			Constraint tmp = IntConstraintFactory.member(
					this.startTime, Ints.toArray(entry.getValue()));
			
			Constraint timeConstraint = LogicalConstraintFactory.and(
					SetConstraintsFactory.offSet(this.daySet, daySet, 0), tmp);

			conArr.add(timeConstraint);
		}
		
		if (conArr.size() > 1) {
			constraint = LogicalConstraintFactory.or(conArr.toArray(new Constraint[0]));
		} else if (conArr.size() == 1) {
			constraint = conArr.get(0);
		} else {
			constraint = solver.FALSE;
		}
	}
	
	public Constraint notOverlap(Time other) {
		Constraint daysConstraint = SetConstraintsFactory.disjoint(this.daySet, other.daySet);
		Constraint time1Constraint = IntConstraintFactory.arithm(this.startTime, ">=", other.startTime, "+", other.duration);
		Constraint time2Constraint = IntConstraintFactory.arithm(other.startTime, ">=", this.startTime, "+", this.duration);
		
		return LogicalConstraintFactory.or(daysConstraint, time1Constraint, time2Constraint);
	}

	public Constraint getConstraint() { return this.constraint; }
	
	public String getDays() {
		int[] daysArr = this.daySet.getValues();
		Days days = new Days(daysArr);
		
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
		} catch (IndexOutOfBoundsException | NumberFormatException e) {
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
			days = "";
			daySet = new HashSet<Integer>();
			for (char c : dayStr.toUpperCase().toCharArray()) {
				int index = week.indexOf(c);
				if (index >= 0) {
					val |= (1 << index);
					daySet.add(index);
				}
			}
			
			int[] arr = Ints.toArray(daySet);
			Arrays.sort(arr);
			for (int d : arr) {
				days += week.charAt(d);
			}
		}
		
		public Days(int[] dayArr) {
			val = 0;
			days = "";
			daySet = new HashSet<Integer>();
			for (int index : dayArr) {
				if (index >= 0 && index < 7) {
					val |= (1 << index);
					daySet.add(index);
				}
			}
			
			int[] arr = Ints.toArray(daySet);
			Arrays.sort(arr);
			for (int index : arr) {
				days += week.charAt(index);
			}
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
		
		public int hashCode() { return ((Integer) val).hashCode(); }
		public boolean equals(Object other) {
			if (other instanceof Days) {
				return (this.val == ((Days) other).val);
			} else return false;
		}
	}
}
