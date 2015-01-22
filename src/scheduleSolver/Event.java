package scheduleSolver;

import java.util.ArrayList;

public class Event {
	String name;
	String ID;
	int daysCount;
	int duration;
	ArrayList<String> possibleStartTimes; /* for now these are Strings, maybe a pair such as <String:int> or <String:Time> */
	char[] days;
	
}
