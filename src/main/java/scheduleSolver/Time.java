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
		
//		this.constraint = solver.TRUE;
		
		ArrayList<Constraint> conArr = new ArrayList<Constraint>();
		for (Entry<Integer, List<Integer>> entry : daysTimes.entrySet()) {
			int[] daysArr = convertIntToArray(entry.getKey());
			
//			System.out.println("Days Array: " + Arrays.toString(daysArr));
			
			SetVar daySet = VariableFactory.fixed("days set",
					daysArr, solver);
			
//			System.out.println("Time Array: " + Arrays.toString(Ints.toArray(entry.getValue())));
			
			Constraint tmp = IntConstraintFactory.member(
					this.startTime, Ints.toArray(entry.getValue()));
			
			Constraint timeConstraint = LogicalConstraintFactory.and(
					SetConstraintsFactory.offSet(this.days, daySet, 0), tmp);
			//TODO: Finish Time's initialization method
//			constraint = LogicalConstraintFactory.or(constraint, timeConstraint);
			
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
			hour = Integer.parseInt(tmString.substring(1, 3));
			min = Integer.parseInt(tmString.substring(3));
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
	
	/* getters */
	//public Map<String[],String[]> getDaysTimes(){ return this.daysTimes; }
//	public int getInt(){ return asInt; }
//	public String getDayTime(){ return asDayTime; }
	
	/* setters */
//	public void setInt(){
//		char day = asDayTime.charAt(0);
//		int base;
//		
//		switch(day){
//		case 'M': base = 0;
//		break;
//		case 'T': base = 1;
//		break;
//		case 'W': base = 2;
//		break;
//		case 'H': base = 3;
//		break;
//		case 'F': base = 4;
//		break;
//		case 'S': base = 5;
//		break;
//		case 'U': base = 6;
//		break;
//		default: base = -1;  /* if this happens, something is wrong */
//		break;
//		}
//		
//		this.asInt = (base * 10000) + Integer.parseInt(this.asDayTime.substring(1));
//	}
//	
//	public void setDayTime(){
//		int dayAsInt = this.asInt / 10000;
//		char day;
//		
//		switch(dayAsInt){
//		case 0: day = 'M';
//		break;
//		case 1: day = 'T';
//		break;
//		case 2: day = 'W';
//		break;
//		case 3: day = 'H';
//		break;
//		case 4: day = 'F';
//		break;
//		case 5: day = 'S';
//		break;
//		case 6: day = 'U';
//		break;
//		default: day = 'X'; /* if this happens, something is wrong */
//		break;
//		}
//		
//		String timeAsString = Integer.toString(this.asInt % 10000);
//		while(timeAsString.length() < 4)
//			timeAsString = '0' + timeAsString;
//		
//		this.asDayTime = (day + timeAsString);
//	}
	
//	@Override public String toString(){
//		return this.getDayTime() + ":\t" + Integer.toString(this.getInt());
//	}
}
