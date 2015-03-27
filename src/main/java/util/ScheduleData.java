package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.primitives.Ints;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import scheduleSolver.Event;
import scheduleSolver.EventConstraint;


public class ScheduleData {
	public String name;
	public Event[] events;
	public SpaceData[] spaces;
	public EventConstraint[] constraints;
//	public TimeData[] times = null;
	
	
	/// EXPERIMENTAL AREA ///
	
	public static ScheduleData parseJson(String jsonStr) {
		ScheduleData data;
		
		try {
			JsonParser parser = new JsonParser();
			JsonObject jsonObj = parser.parse(jsonStr).getAsJsonObject();
					
			JsonArray jsonClasses = jsonObj.getAsJsonArray("EVENT");
			JsonArray jsonResources = jsonObj.getAsJsonArray("SPACE");
			JsonObject jsonConstraint = null;
			if (jsonObj.has("CONSTRAINT"))
				jsonConstraint = jsonObj.getAsJsonObject("CONSTRAINT");
			
			data = new ScheduleData();
			
			if (jsonObj.has("name"))
				data.name = jsonObj.get("name").getAsString();
			else data.name = "Default schedule name";
			
			data.events = parseEvents(jsonClasses);
			data.spaces = parseSpaces(jsonResources);
			data.constraints = parseConstraints(jsonConstraint);
		} catch (Exception e) {
			data = null;
		}
		
		return data;
	}
	
	private static Event[] parseEvents(JsonArray jsonEvents) {
		Event[] events = new Event[jsonEvents.size()];
		for (int i = 0; i < events.length; i++) {
			JsonObject obj = jsonEvents.get(i).getAsJsonObject();
			
			int id = obj.get("id").getAsInt();
			
			int duration = obj.get("duration").getAsInt();
			
			JsonObject pStartTmArray = obj.getAsJsonObject("pStartTm");
		    Map<String, String[]> pStartTm = parseStartTimes(pStartTmArray);
			
		    TimeData time = new TimeData(pStartTm, duration);
		    
		    int[] spaces = parseInts(obj.get("space"));
			int max_participants = obj.get("max_participants").getAsInt();
			int person = obj.get("persons").getAsInt();
			
			events[i] = new Event(id, max_participants,
					time, person, spaces);
		}
		
		return events;
	}
	
	private static int[] parseInts(JsonElement obj) {
		ArrayList<Integer> list = new ArrayList<Integer>();
	    if (obj != null) {
	    	if (obj.isJsonArray()) {
	    		JsonArray arr = obj.getAsJsonArray();
	    		for (JsonElement el : arr) {
	    			list.add(el.getAsInt());
	    		}
	    	} else list.add(obj.getAsInt());
	    } else return null;
	    
	    return Ints.toArray(list);
	}

	private static SpaceData[] parseSpaces(JsonArray jsonSpaces) {
		SpaceData[] rooms = new SpaceData[jsonSpaces.size()];
		
		for(int i = 0; i < rooms.length; i++)
		{
			JsonObject room = jsonSpaces.get(i).getAsJsonObject();
			int id = room.get("id").getAsInt();
			int capacity = room.get("capacity").getAsInt();
			//String times = room.getString("times");
			
			rooms[i] = new SpaceData(id, capacity);
		}
		
		return rooms;
	}
	
	private static Map<String, String[]> parseStartTimes(JsonObject jsonObj) {
		HashMap<String, String[]> mapping = new HashMap<String, String[]>();
		
	    for (Entry<String, JsonElement> entry : jsonObj.entrySet()) {
	    	String key = entry.getKey();
	    	JsonArray value = entry.getValue().getAsJsonArray();
	    	String[] times = new String[value.size()];
			for (int i = 0; i < value.size(); i++) {
				times[i] = value.get(i).getAsString();
			}
			
			mapping.put(key, times);
	    }
		
		return mapping;
	}
	
	private static EventConstraint[] parseConstraints(JsonObject jsonObj) {
		ArrayList<EventConstraint> list = new ArrayList<EventConstraint>();
		
		if (jsonObj == null) return list.toArray(new EventConstraint[0]);
		
		for (Entry<String, JsonElement> entry : jsonObj.entrySet()) {
			String key = entry.getKey();
			
			JsonArray jsonArr = entry.getValue().getAsJsonArray();
			for(int i = 0; i < jsonArr.size(); i++)
			{
				JsonArray classPair = jsonArr.get(i).getAsJsonArray();
				list.add(new EventConstraint(classPair.get(0).getAsInt(), classPair.get(1).getAsInt(), key));
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
