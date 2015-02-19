package util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import scheduleSolver.Event;
import scheduleSolver.Space;

public class Json {
	
	public static Event[] parseEvents(JSONArray jsonEvents) throws JSONException 
	{
		Event[] events = new Event[jsonEvents.length()];
		for (int i = 0; i < events.length; i++) 
		{
			JSONObject obj = jsonEvents.getJSONObject(i);
			
			int id = obj.getInt("id");
//			int days_count = obj.getInt("days_count");
			int duration = obj.getInt("duration");
			
			// searchResult refers to the current element in the array "search_result"
		    JSONObject pStartTmArray = obj.getJSONObject("pStartTm");
		    Map<String, String[]> pStartTm = parseStartTimes(pStartTmArray);
			
			int space = obj.getInt("space");
			int max_participants = obj.getInt("max_participants");
			int person = obj.getInt("persons");
			
			events[i] = new Event(id, max_participants,
					pStartTm, duration, person, space);
		}
		
		return events;
	}
	
	public static Space[] parseSpaces(JSONArray jsonSpaces) throws JSONException
	{
		Space[] rooms = new Space[jsonSpaces.length()];
		
		for(int i = 0; i < rooms.length; i++)
		{
			JSONObject room = jsonSpaces.getJSONObject(i);
			int id = room.getInt("id");
			int capacity = room.getInt("capacity");
			//String times = room.getString("times");
			
			rooms[i] = new Space(id, capacity);
		}
		
		return rooms;
	}
	
	private static Map<String, String[]> parseStartTimes(JSONObject jsonObj) throws JSONException {
		HashMap<String, String[]> mapping = new HashMap<String, String[]>();
		
	    @SuppressWarnings("unchecked")
		Iterator<String> keys = (Iterator<String>) jsonObj.keys();

		while(keys.hasNext()) {
			String key = keys.next();
			
			JSONArray jsonArr = jsonObj.getJSONArray(key);
			String[] times = new String[jsonArr.length()];
			for (int i = 0; i < jsonArr.length(); i++) {
				times[i] = jsonArr.getString(i);
			}
			
			mapping.put(key, times);
		}
		
		return mapping;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
