package scheduleSolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class Space {
	
	/* members */
	String name="DEFAULT ROOM";      /* human readable name, e.g. WEBL101 */
	int ID;	                     	 /* used as part of the int representation for the solver */
	int capacity;                    /* max number of attendees in the room (excludes administrative people such as teachers, speakers, etc) */
	ArrayList<Time> timesAvailable;  /* the times at which the Space is available, by default it is from Monday at midnight to Sunday at 23:59 */
	ArrayList<Event> events;		 /* the events that are actually scheduled in this Space */
	
	/* constructors */
	public Space(int i, int c){
		ID = i;
		capacity = c;
	}
	
	public Space(JSONObject jsonObj) throws JSONException {
		this.ID = jsonObj.getInt("id");
		this.capacity = jsonObj.getInt("capacity");
	}
	
	/* getters */
	public String getName(){ return name; }
	public int getID(){ return ID; }
	public int getCapacity(){ return capacity; }
	
	/* setters */
	public void setID(int id){ id = ID; }
	
	public static Map<Integer, Space> parseSpaces(List<JSONObject> jsonObj) throws JSONException {
		// TODO: Fill up parseRooms method
		Map<Integer, Space> spaces = new HashMap<Integer, Space>();
		for (int i = 0; i < jsonObj.size(); i++) {
			Space space = new Space(jsonObj.get(i));
			spaces.put(space.ID, space);
		}
		return spaces;
	}
}
