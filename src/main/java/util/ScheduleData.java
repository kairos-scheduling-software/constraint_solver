package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import scheduleSolver.Event;
import scheduleSolver.EventConstraint;


public class ScheduleData {
	public String name;
	public Event[] events;
	public SpaceData[] spaces;
	public EventConstraint[] constraints;
//	public TimeData[] times = null;
	
	public static ScheduleData parseJson(String jsonStr) throws JSONException {
		JSONObject jsonObj = new JSONObject(jsonStr);
		
		JSONArray jsonClasses = jsonObj.getJSONArray("EVENT");
		JSONArray jsonResources = jsonObj.getJSONArray("SPACE");
		JSONObject jsonConstraint = null;
		if (jsonObj.has("CONSTRAINT"))
			jsonConstraint = jsonObj.getJSONObject("CONSTRAINT");
		
		ScheduleData data = new ScheduleData();
		if (jsonObj.has("name"))
			data.name = jsonObj.getString("name");
		else data.name = "Default schedule name";
		
		data.events = parseEvents(jsonClasses);
		data.spaces = parseSpaces(jsonResources);
		data.constraints = parseConstraints(jsonConstraint);
		
		return data;
	}
	
	private static Event[] parseEvents(JSONArray jsonEvents) throws JSONException 
	{
		Event[] events = new Event[jsonEvents.length()];
		for (int i = 0; i < events.length; i++) 
		{
			JSONObject obj = jsonEvents.getJSONObject(i);
			
			int id = obj.getInt("id");
			
			int duration = obj.getInt("duration");
			
			JSONObject pStartTmArray = obj.getJSONObject("pStartTm");
		    Map<String, String[]> pStartTm = parseStartTimes(pStartTmArray);
			
		    TimeData time = new TimeData(pStartTm, duration);
		    
		    int[] spaces = null;
		    if (obj.has("space")) spaces = new int[]{obj.getInt("space")};
			int max_participants = obj.getInt("max_participants");
			int person = obj.getInt("persons");
			
			events[i] = new Event(id, max_participants,
					time, person, spaces);
		}
		
		return events;
	}
	
	private static SpaceData[] parseSpaces(JSONArray jsonSpaces) throws JSONException
	{
		SpaceData[] rooms = new SpaceData[jsonSpaces.length()];
		
		for(int i = 0; i < rooms.length; i++)
		{
			JSONObject room = jsonSpaces.getJSONObject(i);
			int id = room.getInt("id");
			int capacity = room.getInt("capacity");
			//String times = room.getString("times");
			
			rooms[i] = new SpaceData(id, capacity);
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
	
	private static EventConstraint[] parseConstraints(JSONObject jsonObj) throws JSONException
	{
		ArrayList<EventConstraint> list = new ArrayList<EventConstraint>();
		
		if (jsonObj == null) return list.toArray(new EventConstraint[0]);
		
		@SuppressWarnings("unchecked")
		Iterator<String> keys = (Iterator<String>) jsonObj.keys();
		
		while(keys.hasNext())
		{
			String key = keys.next();
			
			JSONArray jsonArr = jsonObj.getJSONArray(key);
			for(int i = 0; i < jsonArr.length(); i++)
			{
				JSONArray classPair = jsonArr.getJSONArray(i);
				list.add(new EventConstraint(classPair.getInt(0), classPair.getInt(1), key));
			}
		}
		
		return list.toArray(new EventConstraint[0]);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
