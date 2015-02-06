package scheduleSolver;

import static scheduleSolver.Utils.readInput;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

public class Schedule {
	public final String name;
	
	public final Map<Integer, Event> events;
	public final Map<Integer, Space> spaces;
//	public final Map<Integer, Person> persons;
	
	private Solver solver;
	private boolean modelBuilt = false;
	private boolean solved = false;
	
	public Schedule(String name, Event[] events,
			Space[] spaces /*, Map<Integer, Person> persons*/) {
		this.name = name;
		this.solver = new Solver(this.name);
		
		this.spaces = new HashMap<>(spaces.length);
		for (Space space : spaces) {
			this.spaces.put(space.ID, space);
		}
		
		this.events = new HashMap<>(events.length);
		for (Event event : events) {
			event.initialize(this.solver, this.spaces);
			this.events.put(event.ID, event);
		}
		
//		this.persons = persons;
	}
	
//	public Schedule(String name, Map<Integer, Event> events,
//			Map<Integer, Space> spaces /*, Map<Integer, Person> persons*/) {
//		this.name = name;
//		this.events = events;
//		this.spaces = spaces;
////		this.persons = persons;
//		this.solver = new Solver(this.name);
//	}
	
	public Schedule(JSONObject jsonObj) throws JSONException {
		JSONArray jsonClasses, jsonResources;
		ArrayList<JSONObject> jsonRooms = new ArrayList<JSONObject>();
		// ArrayList<JSONObject> jsonProfs;
		
		jsonClasses = jsonObj.getJSONArray("events");
		jsonResources = jsonObj.getJSONArray("resources");
		
		//jsonProfs = new ArrayList<JSONObject>();
		for (int i = 0; i < jsonResources.length(); i++) {
			JSONObject obj = jsonResources.getJSONObject(i);
			if (obj.getString("type").equals("room")) {
				jsonRooms.add(obj);
			}
		}
		
		this.name = jsonObj.getString("name");
		this.solver = new Solver(this.name);
		this.spaces = Space.parseSpaces(jsonRooms);
		this.events = Event.parseClasses(this.solver, spaces, /*persons,*/ jsonClasses);
	}
	
	public ArrayList<EventPOJO> getSolution(){
		ArrayList<EventPOJO> eps = new ArrayList<EventPOJO>();
		
		if(findSolution()){
			for(Map.Entry<Integer, Event> event : events.entrySet()){
				EventPOJO ep = new EventPOJO();
				Event e = event.getValue();
				ep.ID = e.getID();                           // getID needs to be created
				ep.days = e.getDays();                       // getDays needs to be written
				ep.roomID = e.getRoomID();                   // getRoomID needs to be written
				ep.startTime = e.getDayTime().substring(1);  // getDayTime needs to be written
				ep.wasFailure = false;
				eps.add(ep);
			}
		}
		else{
			EventPOJO ep = new EventPOJO();
			ep.wasFailure = true;
			eps.add(ep);
		}
		return eps;
	}
	
//	public Constraint getConstraintsEventsSpaces() {
//		ArrayList<IntVar> timeBlocks = new ArrayList<IntVar>();
//		for (Event event : this.events.values()) {
//			// 1. Extract domain from event.possibleStartTimes
//			//		Things to consider:
//			//		- Convert from human-friendly strings to integers
//			//		- Space's capacity
//			//		- Space's availability (can just ignore for now...)
//			//    For now, we will assume that possibleStartTimes are in
//			//      correct format already (010910 instead of T:09:10 for instance)
//			// 2. time0 = IntVar.enumerated(domain)
//			// 3. populate array of time_offset (c1_s1_t0, c1_s1_t1, c1_s1_t2...)
//			// 4. Do the same for all the events
//			// 5. create alldifferent(array) constraint
//			
//			// 1.
//			ArrayList<Integer> domain = new ArrayList<Integer>();
//			for (Time t : event.possibleStartTimes) {
//				Integer tmp = t.getInt() * 100;
//				for (Space space : this.spaces.values()) {
//					if (space.capacity >= event.maxParticipants) {
//						domain.add(tmp + space.ID);
//					}
//				}
//			}
//			
//			// 2.
//			IntVar time0 = VariableFactory.enumerated("start time",
//					Ints.toArray(domain), this.solver);
//			
//			// 3.
//			for (int i = 0; i < event.duration; i += 5) {
//				IntVar timeVar = VariableFactory.offset(time0, i * 100);
//				timeBlocks.add(timeVar);
//			}
//			
//			// 4. -- Continue the loop
//		}
//		
//		// 5.
//		return IntConstraintFactory.alldifferent((IntVar[]) timeBlocks.toArray());
//	}
	
	private void buildModel() {
		Constraint constraint = IntConstraintFactory.TRUE(this.solver);
		Event[] events_arr = this.events.values().toArray(new Event[0]);
		int n = events_arr.length;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				constraint = LogicalConstraintFactory.and(constraint,
						events_arr[i].notOverlap(events_arr[j]));
			}
//			for (Space space : this.spaces) {
//				constraint = LogicalConstraintFactory.and(constraint,
//						this.events[i].roomConstraint(space));
//			}
//			constraint = LogicalConstraintFactory.and(constraint, this.events[i].eventConstraint);
		}
		
		this.solver.post(constraint);
		
		modelBuilt = true;
	}
	
	private boolean findSolution() {
		if (!modelBuilt) buildModel();
		solved = solver.findSolution();
		return solved;
	}
	
	public void testJson(String fileName) {
		
	}
	
	public static void testing2(String inputFile)
			throws JSONException, IOException {
		InputStream is = new FileInputStream(inputFile);
		String rawData = readInput(is);
		is.close();
		JSONObject jsonInput = new JSONObject(rawData);
		
		Schedule scheduler = new Schedule(jsonInput);
		
		if (scheduler.findSolution())
			//scheduler.printSolution();
			System.out.println("Finished.");
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws JSONException 
	 */
	public static void main(String[] args) throws JSONException, IOException {
		// TODO Auto-generated method stub
		testing2("/home/dttvinh/snippet_spr15_schedule.json");
		
	}
}