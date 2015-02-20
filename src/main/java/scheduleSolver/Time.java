package scheduleSolver;

import java.util.ArrayList;
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
//	private int asInt;         /* in range 0 - 61439, the set of valid asInt values is {0,1,...,59,100,101,...,159,...,2359,10000,10001,...,12359,...62359} */
//	private String asDayTime;  /* will be on character from {M,T,W,H,F,S,U} followed by four numeric digits representing the time, based on a 24-hour clock */
	
	private Map<Integer, List<Integer>> daysTimes;
	private int duration;  // event's duration in minutes
	
	private SetVar days;
	private IntVar startTime;
	private Constraint constraint;
	
//	/* constructors */
//	public Time(int i){
//		asInt = i;
//		setDayTime();
//	}
//	
//	public Time(String s){
//		asDayTime = s;
//		setInt();
//	}
	
	public Time(Map<String, String[]> startTimes, int duration) {
		this.daysTimes = new HashMap<Integer, List<Integer>>();
		this.duration = duration;
		
		String week = "MTWHFSU";
		
		for (Entry<String, String[]> entry : startTimes.entrySet()) {
			int key = 0;
			ArrayList<Integer> value = new ArrayList<Integer>();
			
			String days = entry.getKey().toUpperCase();
			String[] times = entry.getValue();
			
			for (int i = 0; i < days.length(); i++)
				key += (1 << week.indexOf(days.charAt(i)));
			
			for(int i = 0; i < times.length; i++){
				value.add(stringToTime(times[i]));
			}
			this.daysTimes.put(key, value);
		}
	}
	
	public void initialize(Solver solver) {
		this.days = VariableFactory.set("TimeBlock", 0, 6, solver);
		
		HashSet<Integer> totalTimes = new HashSet<Integer>();
		for (List<Integer> times : this.daysTimes.values()) {
			totalTimes.addAll(times);
		}
		this.startTime = VariableFactory.enumerated("startTime",
				Ints.toArray(totalTimes), solver);
		
		ArrayList<Constraint> conArr = new ArrayList<Constraint>();
		for (Entry<Integer, List<Integer>> entry : daysTimes.entrySet()) {
			int[] daysArr = convertIntToArray(entry.getKey());
			
			SetVar daySet = VariableFactory.fixed("days set",
					daysArr, solver);
			
			Constraint tmp = IntConstraintFactory.member(
					this.startTime, Ints.toArray(entry.getValue()));
			
			Constraint timeConstraint = LogicalConstraintFactory.and(
					SetConstraintsFactory.offSet(this.days, daySet, 0), tmp);

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
		Constraint daysConstraint = SetConstraintsFactory.disjoint(this.days, other.days);
		Constraint time1Constraint = IntConstraintFactory.arithm(this.startTime, ">=", other.startTime, "+", other.duration);
		Constraint time2Constraint = IntConstraintFactory.arithm(other.startTime, ">=", this.startTime, "+", this.duration);
		
		return LogicalConstraintFactory.or(daysConstraint, time1Constraint, time2Constraint);
	}

	public Constraint getConstraint() { return this.constraint; }
	
	public char[] getDays(){
		int[] daysArr = this.days.getValues();
		String week = "MTWHFSU";
		char[] daysChar = new char[daysArr.length];
		
		for (int i = 0; i < daysArr.length; i++) {
			daysChar[i] = week.charAt(daysArr[i]);
		}
		
		return daysChar;
	}
	
	public String getStartTime(){
		int tm = this.startTime.getValue();
		
		return timeToString(tm);
	}
		
	public Time(Map<String, String[]> m){
		this.daysTimes = new HashMap<Integer, List<Integer>>();
		
		for(String key : m.keySet()){
			int newKey = 0;
			for(int i=0; i<key.length(); i++){
				switch(key.charAt(i)){
				case 'M' : newKey += 64;
				break;
				case 'T' : newKey += 32;
				break;
				case 'W' : newKey += 16;
				break;
				case 'H' : newKey += 8;
				break;
				case 'F' : newKey += 4;
				break;
				case 'S' : newKey += 2;
				break;
				case 'U' : newKey += 1;
				break;
				default : newKey += 0; /* this shouldn't happen */
				}
			}
			
			ArrayList<Integer> newVal = new ArrayList<Integer>();
			for(int i=0; i<m.get(key).length; i++){
				newVal.add(Integer.parseInt(m.get(key)[i]));
			}
			this.daysTimes.put(newKey,newVal);
		}
	}
	
	private int[] convertIntToArray(int days) {
		ArrayList<Integer> arrDays = new ArrayList<Integer>();
		int index = 0;
		while (days > 0) {
//			arrDays.add(days % 2);
			if (days % 2 != 0) arrDays.add(index);
			index++;
			days /= 2;
		}
		return Ints.toArray(arrDays);
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
	
//	@Override public String toString(){
//		return this.getDayTime() + ":\t" + Integer.toString(this.getInt());
//	}
}
