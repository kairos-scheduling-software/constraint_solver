package util;

import java.util.HashMap;
import java.util.Map;


public class Spaces {

	public final int n;
	public int[] ids;
	public int[] capacities;
	
	private Map<Integer, Integer> capacityMap;
	
	public Spaces(SpaceData[] sData) {
		n = sData.length;
		ids = new int[n];
		capacities = new int[n];
		capacityMap = new HashMap<Integer, Integer>(n);
		for (int i = 0; i < n; i++) {
			ids[i] = sData[i].getID();
			capacities[i] = sData[i].getCapacity();
			capacityMap.put(ids[i], capacities[i]);
		}
	}
	
	public Integer getCapacity(int id) {
		return capacityMap.get(id);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
