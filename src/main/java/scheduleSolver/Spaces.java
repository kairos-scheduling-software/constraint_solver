package scheduleSolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import solver.constraints.Constraint;
import solver.constraints.ICF;
import solver.constraints.LCF;
import solver.variables.IntVar;
import util.SpaceData;

import com.google.common.primitives.Ints;


public class Spaces {

	public final int n;
	private ArrayList<Integer> idList;
	private ArrayList<Integer> capList;
	
	private Map<Integer, Integer> capacityMap;
	
//	public static final int DEFAULT_ID = -1;
//	public static final int DEFAULT_CAP = Integer.MAX_VALUE;
	
	public Spaces(SpaceData[] sData) {
		idList = new ArrayList<Integer>();
		capList = new ArrayList<Integer>();
		capacityMap = new HashMap<Integer, Integer>();
		for (SpaceData space : sData) {
			int id, cap;
			id = space.getId();
			if (id < 0) continue;
			cap = space.getCapacity();
			idList.add(id);
			capList.add(cap);
			capacityMap.put(id, cap);
		}
		n = capacityMap.size();
//		capacityMap.put(DEFAULT_ID, DEFAULT_CAP);
	}
	
	public Integer getCapacity(int id) {
		return capacityMap.get(id);
	}
	
	public int[] getIds() {
		return Ints.toArray(idList);
	}
	
	public int[] getCapacities() {
		return Ints.toArray(capacityMap.values());
	}
	
	public int[] getCapacities(int[] ids) {
		ArrayList<Integer> caps = new ArrayList<Integer>();
		for (int id : ids) {
			Integer cap = capacityMap.get(id);
			if (cap != null) caps.add(cap);
		}
		return Ints.toArray(caps);
	}
	
	public boolean hasId(int id) {
		return capacityMap.containsKey(id);
	}
	
	public int[] filterIds(int[] ids) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int id : ids) if (hasId(id)) list.add(id);
		return Ints.toArray(list);
	}
	
	public Constraint getConstraint(IntVar id, IntVar cap, IntVar index) {
		Constraint idCon = ICF.element(id, Ints.toArray(idList), index);
		Constraint capCon = ICF.element(cap, Ints.toArray(capList), index);
		
		Constraint con = LCF.and(idCon, capCon);
		return con;
	}

}
