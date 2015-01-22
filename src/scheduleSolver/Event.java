package scheduleSolver;

import java.util.ArrayList;

public class Event {
	String name;						  /* name of the event, e.g. "Computer Systems" */
	String ID;							  /* event ID, e.g. cs4400 */
	int daysCount;						  /* the number of days per week that the event should be scheduled */
	int duration;						  /* the length, in minutes, of a single session of the event */
	ArrayList<String> possibleStartTimes; /* for now these are Strings, maybe a pair such as <String:int> or <String:Time> */
	char[] days;						  /* the actual days that the event gets scheduled */
	Time startTime;						  /* the time at which the event will start for each day in days */
	Space space;						  /* the space where the event will be held, e.g in a room */
	int maxParticipants;				  /* the maximum number of participants that can be included in the Event */
	ArrayList<Person> persons;			  /* any administrative-type people associated with the event, e.g teachers, speakers, etc */
}
