package scheduleSolver;

import java.util.ArrayList;
import java.util.Map;

import com.google.common.primitives.Ints;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.LogicalConstraintFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

public class Event {
	String name;						  /* name of the event, e.g. "Computer Systems" */
	int ID;							  /* event ID, e.g. cs4400 */
	Time time; /* for now these are Strings, maybe a pair such as <String:int> or <String:Time> */
	Space space;						  /* the space where the event will be held, e.g in a room */
	int maxParticipants;				  /* the maximum number of participants that can be included in the Event */
	int[] spaceIds;
	Person person;			  /* any administrative-type people associated with the event, e.g teachers, speakers, etc */
	int personId;
	
	Solver solver;
	IntVar spaceId = null;
	
	public Event(int id, int maxParticipants,
			Map<String, String[]> startTimes, int duration,
			 int personId) {
		this(id, maxParticipants, startTimes, duration, personId, -1);
		
	}
	
	public Event(int id, int maxParticipants,
			Map<String, String[]> startTimes, int duration,
			int personId, int spaceId) {
		this.ID = id;
		this.maxParticipants = maxParticipants;
		this.personId = personId;
		
		this.time = new Time(startTimes, duration);
		
//		this.possibleStartTimes = new ArrayList<Time>();
//		for (String t : possibleStartTimes) {
//			this.possibleStartTimes.add(new Time(t));
//		}
		if (spaceId >= 0) {
			this.spaceIds = new int[]{spaceId};
		}
	}
	
	public void initialize(Solver solver, Map<Integer, Space> spaces) {
		this.solver = solver;
		
		// Initialize Space Id
		ArrayList<Integer> spaceIds = new ArrayList<Integer>();
		if (this.spaceIds == null) {
			for (Map.Entry<Integer, Space> entry : spaces.entrySet()) {
				if (entry.getValue().capacity >= this.maxParticipants) {
					spaceIds.add(entry.getKey());
				}
			}
		} else {
			for (int i : this.spaceIds) {
				if (spaces.get(i).capacity >= this.maxParticipants) {
					spaceIds.add(i);
				}
			}
		}
		this.spaceIds = Ints.toArray(spaceIds);
		this.spaceId = VariableFactory.enumerated("room id", this.spaceIds, this.solver);
		
		// Initialize Start Time
		this.time.initialize(this.solver);
//		initializeStartTime();
//		initializeBlocks();
	}
	
	public Constraint notOverlap(Event other) {
		Constraint _space = IntConstraintFactory.arithm(this.spaceId, "!=", other.spaceId);
//		ArrayList<IntVar> combinedTimes = new ArrayList<IntVar>(this.blocks);
//		combinedTimes.addAll(other.blocks);
//		Constraint _time = IntConstraintFactory.alldifferent(combinedTimes.toArray(new IntVar[0]));
		Constraint _time = this.time.notOverlap(other.time);
		if (this.personId == other.personId)
			return _time;
		else
			return LogicalConstraintFactory.or(_space, _time);
	}
	
	public int getID() { return this.ID; }
	public int getSpaceID() { return this.spaceId.getValue(); }
	public char[] getDays() {
		
		return this.time.getDays();
		
//		String daysStr = "MTWHFSU";
////		char[] dayArr = {'M', 'T', 'W', 'H', 'F', 'S', 'U'};
//		char[] days = new char[this.daysCount];
//		Time startTime = new Time(this.startTime.getValue());
//		String strStartTime = startTime.getDayTime();
//		int dayIndex = daysStr.indexOf(strStartTime.charAt(0));
//		
//		// May get IndexOutOfBound due to malfunction-input
//		for (int i = 0; i < days.length; i++) {
//			days[i] = daysStr.charAt(dayIndex + i * 2);
//		}
//		
//		return days;
	}
	public String getStartTime() {
		return this.time.getStartTime();
	}
	
//	private ArrayList<Integer> getSpaceIds(Map<Integer, Space> spaces) {
//		ArrayList<Integer> ids = new ArrayList<Integer>();
//		for (Map.Entry<Integer, Space> entry : spaces.entrySet()) {
//			if (entry.getValue().capacity >= this.maxParticipants) {
//				ids.add(entry.getKey());
//			}
//		}
//		return ids;
//	}
	
//	private void initializeStartTime() {
//		int[] tmp = new int[this.startTime.size()];
//		for (int i = 0; i < tmp.length; i++) {
//			tmp[i] = this.startTime.get(i).getInt();
//		}
//		this.startTime = VariableFactory.enumerated("possible start times", tmp, this.solver);
//	}
//	
//	private void initializeBlocks() {
//		int n = this.duration;
//		ArrayList<IntVar> _blocks = new ArrayList<IntVar>();
//		for (int i = 0; i < this.daysCount; i++) {
//			for (int j = 0; j < n; j+=5) {
//				_blocks.add(VariableFactory.offset(this.startTime, i * 20000 + j));
//			}
//		}
//		
//		this.blocks = _blocks;
//	}
}
