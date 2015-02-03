package scheduleSolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
	int daysCount;						  /* the number of days per week that the event should be scheduled */
	int duration;						  /* the length, in minutes, of a single session of the event */
	ArrayList<Time> possibleStartTimes; /* for now these are Strings, maybe a pair such as <String:int> or <String:Time> */
	char[] days;						  /* the actual days that the event gets scheduled */
	//Time startTime;						  /* the time at which the event will start for each day in days */
	Space space;						  /* the space where the event will be held, e.g in a room */
	int maxParticipants;				  /* the maximum number of participants that can be included in the Event */
	int[] spaceIds;
	Person person;			  /* any administrative-type people associated with the event, e.g teachers, speakers, etc */
	int personId;
	
	Solver solver;
	IntVar startTime;
	ArrayList<IntVar> blocks;
	IntVar spaceId;
	
	public Event(int id, int capacity, int daysCount, int duration, 
			String[] possibleStartTimes, int personId) {
		this.ID = id;
		this.daysCount = daysCount;
		this.duration = duration;
		this.maxParticipants = capacity;
		this.personId = personId;
		this.possibleStartTimes = new ArrayList<Time>();
		for (String t : possibleStartTimes) {
			this.possibleStartTimes.add(new Time(t));
		}
	}
	
	public Event(/*Map<Integer, Person> persons,*/ JSONObject jsonClass)
					throws JSONException {
		
		this.ID = jsonClass.getInt("id");
		//this.persons = new ArrayList<Person>();
		//this.persons.add(persons.get(jsonClass.getInt("professor_id")));
		this.personId = jsonClass.getInt("persons");
//		this.person = persons.get(this.personId);
		
		this.daysCount = jsonClass.getInt("days_count");
		this.duration = jsonClass.getInt("duration");
		this.possibleStartTimes = getPossibleStartTimes(jsonClass);
		
		this.maxParticipants = jsonClass.getInt("capacity");
	}
	
	public void initialize(Solver solver, Map<Integer, Space> spaces) {
		this.solver = solver;
		int[] spaceIds = Ints.toArray(getSpaceIds(spaces));
		this.spaceId = VariableFactory.enumerated("room id", spaceIds, this.solver);
		
		this.startTime = getStartTime();
		this.blocks = getBlocks();
	}
	
	public Constraint notOverlap(Event other) {
		Constraint _space = IntConstraintFactory.arithm(this.spaceId, "!=", other.spaceId);
		ArrayList<IntVar> combinedTimes = new ArrayList<IntVar>(this.blocks);
		combinedTimes.addAll(other.blocks);
		Constraint _time = IntConstraintFactory.alldifferent(combinedTimes.toArray(new IntVar[0]));
		if (this.personId == other.personId)
			return _time;
		else
			return LogicalConstraintFactory.or(_space, _time);
	}
	
	public static Map<Integer, Event> parseClasses(Solver _solver,
			Map<Integer, Space> spaces, /*Map<Integer, Person> persons,*/
			JSONArray jsonClasses) throws JSONException {
		Map<Integer, Event> events = new HashMap<Integer, Event>();
		for (int i = 0; i < jsonClasses.length(); i++) {
			Event event = new Event(jsonClasses.getJSONObject(i));
			event.initialize(_solver, spaces);
			events.put(event.ID, event);
		}
		
		return events;
	}
	
	private ArrayList<Time> getPossibleStartTimes(JSONObject jsonClass) throws JSONException {
		JSONArray jsonTimes = jsonClass.getJSONArray("possible_start_times");
		ArrayList<Time> result = new ArrayList<Time>();
		for (int i = 0; i < jsonTimes.length(); i++) {
			result.add(new Time(jsonTimes.getString(i)));
		}
		return result;
	}
	
	private ArrayList<Integer> getSpaceIds(Map<Integer, Space> spaces) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for (Map.Entry<Integer, Space> entry : spaces.entrySet()) {
			if (entry.getValue().capacity >= this.maxParticipants) {
				ids.add(entry.getKey());
			}
		}
		return ids;
	}
	
	private IntVar getStartTime() {
		IntVar result = null;
		// ~~ IntVar.enumerate(str_to_int(possibleStartTime))
		return result;
	}
	
	private ArrayList<IntVar> getBlocks() {
		int n = this.duration / 5;
		ArrayList<IntVar> _blocks = new ArrayList<IntVar>();
		for (int i = 0; i < this.daysCount; i++) {
			for (int j = 0; j < n; j++) {
				_blocks.add(VariableFactory.offset(this.startTime, i * 60 * 48 / 5 + j));
			}
		}
		
		return _blocks;
	}
}
