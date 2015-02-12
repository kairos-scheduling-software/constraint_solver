package scheduleSolver;

import java.util.ArrayList;

public class Time {
	/* members */
	private int asInt;         /* in range 0 - 61439, the set of valid asInt values is {0,1,...,59,100,101,...,159,...,2359,10000,10001,...,12359,...62359} */
	private String asDayTime;  /* will be on character from {M,T,W,H,F,S,U} followed by four numeric digits representing the time, based on a 24-hour clock */
	private ArrayList<Tuple> daysTimes;
	
	/* constructors */
	public Time(int i){
		asInt = i;
		setDayTime();
	}
	
	public Time(String s){
		asDayTime = s;
		setInt();
	}
	
	public Time(){
		this.daysTimes = new ArrayList<Tuple>();
	}
	
	protected class Tuple{
		private int[] first;
		private int[] second;
		public Tuple(int[] d, int[] t){
			this.first = d;
			this.second = t;
		}
		public int[]getFirst(){return this.first;}
		public int[]getSecond(){return this.second;}
	}
	
	public void addAlternative(int[] days, int[] times){
		this.daysTimes.add(new Tuple(days, times));
	}
	
	/* getters */
	public ArrayList<Tuple> getDaysTimes(){ return this.daysTimes; }
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
