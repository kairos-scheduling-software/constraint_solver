package util;

import java.util.ArrayList;

import com.google.common.primitives.Ints;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class EventData {
	public final int id;
	
	public final TimeData time;
	public final int[] spaceIds;
	public final int personId;
	
	public final int maxParticipants;
	
	public EventData(int id, int maxParticipants, TimeData time,
			int personId, int[] spaceIds) {
		this.id = id;
		this.maxParticipants = maxParticipants;
		this.personId = personId;
		
		this.time = time;
		
		this.spaceIds = spaceIds;
	}
	
	public static EventData[] parseEvents(JsonArray jsonEvents, TimeData[] times) {
		EventData[] events = new EventData[jsonEvents.size()];
		for (int i = 0; i < events.length; i++) {
			JsonObject obj = jsonEvents.get(i).getAsJsonObject();
			
			int id = obj.get("id").getAsInt();
			
//			int duration = obj.get("duration").getAsInt();
//			
//			JsonObject pStartTmArray = obj.getAsJsonObject("pStartTm");
//		    Map<String, String[]> pStartTm = parseStartTimes(pStartTmArray);
//			
//		    TimeData time = new TimeData(pStartTm, duration);
			TimeData time;
			JsonElement jsonTime = obj.get("time");
			if (jsonTime.isJsonObject())
				time = new TimeData(jsonTime.getAsJsonObject());
			else {
				int timeId = jsonTime.getAsInt();
				time = TimeData.getTimeData(timeId, times);
				if (time == null) throw new IllegalArgumentException();
			}
		    
		    int[] spaces = parseIntArray(obj.get("spaceId"));
			int max_participants = obj.get("maxParticipants").getAsInt();
			int person = obj.get("personId").getAsInt();
			
			events[i] = new EventData(id, max_participants,
					time, person, spaces);
		}
		
		return events;
	}
	
	private static int[] parseIntArray(JsonElement obj) {
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
	
//	private static Map<String, String[]> parseStartTimes(JsonObject jsonObj) {
//		HashMap<String, String[]> mapping = new HashMap<String, String[]>();
//		
//	    for (Entry<String, JsonElement> entry : jsonObj.entrySet()) {
//	    	String key = entry.getKey();
//	    	JsonArray value = entry.getValue().getAsJsonArray();
//	    	String[] times = new String[value.size()];
//			for (int i = 0; i < value.size(); i++) {
//				times[i] = value.get(i).getAsString();
//			}
//			
//			mapping.put(key, times);
//	    }
//		
//		return mapping;
//	}
}
