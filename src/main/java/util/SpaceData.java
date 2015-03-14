package util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class SpaceData {
	
	/* members */
	String name="DEFAULT ROOM";      /* human readable name, e.g. WEBL101 */
	int ID;	                     	 /* used as part of the int representation for the solver */
	int capacity;                    /* max number of attendees in the room (excludes administrative people such as teachers, speakers, etc) */
	
	/* constructors */
	public SpaceData(int i, int c){
		ID = i;
		capacity = c;
	}
	
	public SpaceData(JSONObject jsonObj) throws JSONException {
		this.ID = jsonObj.getInt("id");
		this.capacity = jsonObj.getInt("capacity");
	}
	
	/* getters */
	public String getName(){ return name; }
	public int getID(){ return ID; }
	public int getCapacity(){ return capacity; }
	
	/* setters */
	public void setID(int id){ id = ID; }
	
	public static Map<Integer, SpaceData> parseSpaces(List<JSONObject> jsonObj) throws JSONException {
		// TODO: Fill up parseRooms method
		Map<Integer, SpaceData> spaces = new HashMap<Integer, SpaceData>();
		for (int i = 0; i < jsonObj.size(); i++) {
			SpaceData space = new SpaceData(jsonObj.get(i));
			spaces.put(space.ID, space);
		}
		return spaces;
	}
}
