package util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ScheduleData {
	public String name;
	public EventData[] events;
	public SpaceData[] spaces;
	public ConstraintData[] constraints;
	public TimeData[] times;
	
	private static String DEFAULT_NAME = "Default schedule name";
	
	public ScheduleData() {}
	
	public ScheduleData(String name, EventData[] events, SpaceData[] spaces, ConstraintData[] constraints) {
		this.name = name;
		this.events = events;
		this.spaces = spaces;
		this.constraints = constraints;
	}
	
	public static ScheduleData parseJson(String jsonStr) {
		ScheduleData data;
		
		try {
			JsonParser parser = new JsonParser();
			JsonObject jsonObj = parser.parse(jsonStr).getAsJsonObject();
			
			JsonElement ev, sp, con, tm;
			ev = jsonObj.get("EVENTS");
			sp = jsonObj.get("SPACES");
			con = jsonObj.get("CONSTRAINTS");
			tm = jsonObj.get("TIMES");
			
			if (ev == null || sp == null || !ev.isJsonArray()
					|| !sp.isJsonArray() ||
					(con != null && !con.isJsonObject())) return null;
			
			JsonArray jsonEvents = ev.getAsJsonArray();
			JsonArray jsonSpaces = sp.getAsJsonArray();
			JsonObject jsonConstraints = null;
			if (con != null) jsonConstraints = con.getAsJsonObject();
			
			data = new ScheduleData();
			
			if (jsonObj.has("name"))
				data.name = jsonObj.get("name").getAsString();
			else data.name = DEFAULT_NAME;
			
			data.spaces = SpaceData.parseSpaces(jsonSpaces);
			
			if (tm != null && tm.isJsonArray()) {
				JsonArray jsonTimes = tm.getAsJsonArray();
				data.times = TimeData.parseTimes(jsonTimes);
			} else data.times = new TimeData[0];
			
			data.events = EventData.parseEvents(jsonEvents, data.times);
			
			data.constraints = ConstraintData.parseConstraints(jsonConstraints);
		} catch (Exception e) {
			data = null;
		}
		
		return data;
	}
}
