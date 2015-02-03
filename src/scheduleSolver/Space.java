package scheduleSolver;

import java.util.ArrayList;

public class Space {
	
	/* members */
	int ID;	                     	 /* used as part of the int representation for the solver */
	int capacity;                    /* max number of attendees in the room (excludes administrative people such as teachers, speakers, etc) */
	ArrayList<Time> timesAvailable;  /* the times at which the Space is available, by default it is from Monday at midnight to Sunday at 23:59 */
	ArrayList<Event> events;		 /* the events that are actually scheduled in this Space */
	
	/* constructors */
	public Space(int c){
		capacity = c;
	}
	
	/* getters */
	public int getID(){ return ID; }
	public int getCapacity(){ return capacity; }
	
	/* setters */
	public void setID(int id){ id = ID; }
}
