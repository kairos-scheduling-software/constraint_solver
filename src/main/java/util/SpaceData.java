package util;

public class SpaceData {
	
	/* members */
	String name="DEFAULT ROOM";      /* human readable name, e.g. WEBL101 */
	int id;	                     	 /* used as part of the int representation for the solver */
	int capacity;                    /* max number of attendees in the room (excludes administrative people such as teachers, speakers, etc) */
	
	/* constructors */
	public SpaceData(int i, int c) {
		id = i;
		capacity = c;
	}
	
	/* getters */
	public String getName() { return name; }
	public int getId() { return id; }
	public int getCapacity() { return capacity; }
}
