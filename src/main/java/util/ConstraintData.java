package util;

import java.util.ArrayList;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ConstraintData {
	public final int id1;
	public final int id2;
	public final String relation;
	
	public ConstraintData(int id1, int id2, String relation) {
		this.id1 = id1;
		this.id2 = id2;
		this.relation = relation;
	}
	
	public static ConstraintData[] parseConstraints(JsonObject jsonObj) {
		ArrayList<ConstraintData> list = new ArrayList<ConstraintData>();
		
		if (jsonObj == null) return list.toArray(new ConstraintData[0]);
		
		for (Entry<String, JsonElement> entry : jsonObj.entrySet()) {
			String key = entry.getKey();
			
			JsonArray jsonArr = entry.getValue().getAsJsonArray();
			for(int i = 0; i < jsonArr.size(); i++)
			{
				JsonArray classPair = jsonArr.get(i).getAsJsonArray();
				list.add(new ConstraintData(classPair.get(0).getAsInt(), classPair.get(1).getAsInt(), key));
			}
		}
		
		return list.toArray(new ConstraintData[0]);
	}
}
