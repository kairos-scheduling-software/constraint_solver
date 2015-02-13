package scheduleSolver;

import java.util.HashMap;
import java.util.Map;

import solver.Solver;
import solver.constraints.Constraint;

public class Time {
	/* members */
	private int asInt;         /* in range 0 - 61439, the set of valid asInt values is {0,1,...,59,100,101,...,159,...,2359,10000,10001,...,12359,...62359} */
	private String asDayTime;  /* will be on character from {M,T,W,H,F,S,U} followed by four numeric digits representing the time, based on a 24-hour clock */
	private Map<Integer,int[]> daysTimes;
	
	/* constructors */
	public Time(int i){
		asInt = i;
		setDayTime();
	}
	
	public Time(String s){
		asDayTime = s;
		setInt();
	}
	
	// Skeleton methods/functions
	public Time(Map<String, String[]> startTimes, int duration) {
		// Main constructor method
		//TODO: Fill this up
	}
	public void initialize(Solver solver) {}
	public char[] getDays(){ return null; }
	public String getStartTime(){ return null; }
	public Constraint notOverlap(Time other) { return null; }
	
	
	public Time(Map<String, String[]> m){
		this.daysTimes = new HashMap<Integer, int[]>();
		for(String key : m.keySet()){
			for(int i=0; i<key.length(); i++){
				int newKey = 0;
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
				int[] newVal = new int[m.get(key).length];
				for(int j=0; j<m.get(key).length; j++){
					newVal[i] = Integer.parseInt(m.get(key)[j]);
				}
				this.daysTimes.put(newKey,newVal);
			}
		}
	}
	
	/* getters */
	//public Map<String[],String[]> getDaysTimes(){ return this.daysTimes; }
	public int getInt(){ return asInt; }
	public String getDayTime(){ return asDayTime; }
	
	/* setters */
	public void setInt(){
		char day = asDayTime.charAt(0);
		int base;
		
		switch(day){
		case 'M': base = 0;
		break;
		case 'T': base = 1;
		break;
		case 'W': base = 2;
		break;
		case 'H': base = 3;
		break;
		case 'F': base = 4;
		break;
		case 'S': base = 5;
		break;
		case 'U': base = 6;
		break;
		default: base = -1;  /* if this happens, something is wrong */
		break;
		}
		
		this.asInt = (base * 10000) + Integer.parseInt(this.asDayTime.substring(1));
	}
	
	public void setDayTime(){
		int dayAsInt = this.asInt / 10000;
		char day;
		
		switch(dayAsInt){
		case 0: day = 'M';
		break;
		case 1: day = 'T';
		break;
		case 2: day = 'W';
		break;
		case 3: day = 'H';
		break;
		case 4: day = 'F';
		break;
		case 5: day = 'S';
		break;
		case 6: day = 'U';
		break;
		default: day = 'X'; /* if this happens, something is wrong */
		break;
		}
		
		String timeAsString = Integer.toString(this.asInt % 10000);
		while(timeAsString.length() < 4)
			timeAsString = '0' + timeAsString;
		
		this.asDayTime = (day + timeAsString);
	}
	
	@Override public String toString(){
		return this.getDayTime() + ":\t" + Integer.toString(this.getInt());
	}
}
