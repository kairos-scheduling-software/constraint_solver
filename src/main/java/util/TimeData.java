package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import solver.constraints.extension.Tuples;

import com.google.common.primitives.Ints;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TimeData {
	private Map<Days, List<Integer>> daysTimes;
	private int duration;  // event's duration in minutes
	private int count;
	private Integer id;
	
	public TimeData(Map<String, String[]> startTimes, int duration) {
		this.duration = duration;
		initialize(startTimes);
	}
	
	public TimeData(TimeData other) {
		this.duration = other.duration;
		this.daysTimes = other.daysTimes;
		this.count = other.count;
	}
	
	public TimeData(JsonObject jsonObj) {
		duration = jsonObj.get("duration").getAsInt();
		
		if (jsonObj.has("id"))
			id = jsonObj.get("id").getAsInt();
		else id = null;
		
		JsonObject startTimesArray = jsonObj.getAsJsonObject("startTimes");
	    Map<String, String[]> startTimes = parseStartTimes(startTimesArray);
	    initialize(startTimes);
	}
	
	public static TimeData[] parseTimes(JsonArray jsonArr) {
		TimeData[] times = new TimeData[jsonArr.size()];
		for (int i = 0; i < times.length; i++) {
			JsonObject obj = jsonArr.get(i).getAsJsonObject();
			times[i] = new TimeData(obj);
		}
		
		return times;
	}
	
	private void initialize(Map<String, String[]> startTimes) {
		this.daysTimes = new HashMap<Days, List<Integer>>();
		this.count = 0;
		
		for (Entry<String, String[]> entry : startTimes.entrySet()) {
			Days days = new Days(entry.getKey());
			
			String[] times = entry.getValue();
			ArrayList<Integer> value = new ArrayList<Integer>();
			
			for(int i = 0; i < times.length; i++){
				value.add(stringToTime(times[i]));
			}
			count += value.size();
			this.daysTimes.put(days, value);
		}
	}
	
	public static TimeData getTimeData(int id, TimeData[] times) {
		for (TimeData time : times) {
			if (time.id != null && time.id == id) return time;
		}
		
		return null;
	}
	
	public Tuples getTuples() {
		Tuples tuples = new Tuples();
		int index = 0;
		for (Entry<Days, List<Integer>> entry : daysTimes.entrySet()) {
			int[] daysArr = entry.getKey().getWeekArr();
			
			for (int i = 0; i < 7; i++) {
				daysArr[i] *= (i+1);
			}
			for (int t : entry.getValue()) {
				int[] vals = Arrays.copyOf(daysArr, 9);
				vals[7] = t;
				vals[8] = index;
				index++;
				tuples.add(vals);
			}
		}
		return tuples;
	}
	
	public int[] getPossibleTimes() {
		HashSet<Integer> totalTimes = new HashSet<Integer>();
		for (List<Integer> times : this.daysTimes.values()) {
			totalTimes.addAll(times);
		}
		
		return Ints.toArray(totalTimes);
	}
	
	public int getCount() { return count; }
	public int getDuration() { return duration; }
	
	public static String getDays(int daysVal) {
		Days days = new Days(daysVal);
		return days.getDays();
	}
	
	public static int stringToTime(String tmString) {
		int hour, min;
		try {
			hour = Integer.parseInt(tmString.substring(0, 2));
			min = Integer.parseInt(tmString.substring(2));
		} catch (Exception e) {
			throw new IllegalArgumentException();
		}
		
		return hour * 60 + min;
	}
	
	public static String timeToString(int tm) {
		int hour, min;
		
		min = tm % 60;
		hour = tm / 60;
		
		return String.format("%02d%02d", hour, min);
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
	
	private static class Days {
		private String days;
		private HashSet<Integer> daySet;
		private int val;
		
		private static String week = "MTWHFSU";
		
		public Days(String dayStr) {
			val = 0;
			for (char c : dayStr.toUpperCase().toCharArray()) {
				int index = week.indexOf(c);
				if (index >= 0) val |= (1 << index);
			}
			
			initialize();
		}
		
		public Days(int val) {
			this.val = val & ((1 << 7) - 1);
			
			initialize();
		}
		
		public String getDays() { return days; }
		
		public int[] getWeekArr() {
			int[] arr = new int[7];
			Arrays.fill(arr, 0);
			for (int i = 0; i < 7; i++) {
				if (daySet.contains(i)) arr[i] = 1;
			}
			return arr;
		}
		
		public int hashCode() { return ((Integer) val).hashCode(); }
		public boolean equals(Object other) {
			if (other instanceof Days) {
				return (this.val == ((Days) other).val);
			} else return false;
		}
		
		private void initialize() {
			daySet = new HashSet<Integer>();
			for (int i = 0; i < 7; i++)
				if ((val & (1 << i)) != 0) daySet.add(i);
			
			days = "";
			int[] arr = Ints.toArray(daySet);
			Arrays.sort(arr);
			for (int index : arr) {
				days += week.charAt(index);
			}
		}
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
