package scheduleSolver;

import static scheduleSolver.Utils.readInput;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.primitives.Ints;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ICF;
import solver.constraints.IntConstraintFactory;
import solver.constraints.LogicalConstraintFactory;
import solver.explanations.ExplanationFactory;
import solver.explanations.RecorderExplanationEngine;
import solver.explanations.strategies.ConflictBasedBackjumping;
import solver.explanations.strategies.DynamicBacktracking;
import solver.trace.Chatterbox;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.VF;
import solver.variables.Variable;
import solver.variables.VariableFactory;
import solver.variables.impl.BitsetIntVarImpl;

public class Schedule {
	public final String name;
	
	public final Map<Integer, Event> events;
	public final Map<Integer, Space> spaces;
//	public final Map<Integer, Person> persons;
	
	private Solver solver;
	private DynamicBacktracking exp;
	private boolean modelBuilt = false;
	private boolean solved = false;
	
	public Schedule(String name, Event[] events,
			Space[] spaces /*, Map<Integer, Person> persons*/) {
		this.name = name;
		this.solver = new Solver(this.name);
		
		ExplanationFactory.DBT.plugin(solver, true);
		solver.set(new RecorderExplanationEngine(solver));
//		exp = new ConflictBasedBackjumping(solver.getExplainer());
		//exp = new DynamicBacktracking(solver.getExplainer());
		// Then active end-user explanation
		//exp.activeUserExplanation(true);
		
		this.spaces = new HashMap<Integer, Space>(spaces.length);
		for (Space space : spaces) {
			this.spaces.put(space.ID, space);
		}
		
		this.events = new HashMap<Integer, Event>(events.length);
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
	
//	public Schedule(JSONObject jsonObj) throws JSONException {
//		JSONArray jsonClasses, jsonResources;
//		ArrayList<JSONObject> jsonRooms = new ArrayList<JSONObject>();
//		// ArrayList<JSONObject> jsonProfs;
//		
//		jsonClasses = jsonObj.getJSONArray("events");
//		jsonResources = jsonObj.getJSONArray("resources");
//		
//		//jsonProfs = new ArrayList<JSONObject>();
//		for (int i = 0; i < jsonResources.length(); i++) {
//			JSONObject obj = jsonResources.getJSONObject(i);
//			if (obj.getString("type").equals("room")) {
//				jsonRooms.add(obj);
//			}
//		}
//		
//		this.name = jsonObj.getString("name");
//		this.solver = new Solver(this.name);
//		this.spaces = Space.parseSpaces(jsonRooms);
//		this.events = Event.parseClasses(this.solver, spaces, /*persons,*/ jsonClasses);
//	}
	
	public ArrayList<EventPOJO> getSolution(){
		ArrayList<EventPOJO> eps = new ArrayList<EventPOJO>();
		
		if(findSolution()){
			for(Map.Entry<Integer, Event> event : events.entrySet()){
				EventPOJO ep = new EventPOJO();
				Event e = event.getValue();
				ep.ID = e.getID();                        
				ep.days = e.getDays();                  
				ep.roomID = e.getSpaceID();                   
				ep.startTime = e.getStartTime();
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
		
		Chatterbox.showDecisions(solver);
		solved = solver.findSolution();
		Chatterbox.printStatistics(solver);
		if (!solved) {
			
			//if (exp.getUserExplanation() == null)
				//System.out.println("Error: No explanation.");
			//else
				//System.out.println("Explain: " + exp.getUserExplanation());
			System.out.println(solver.getEngine().getContradictionException());
			//solver.getEngine().getContradictionException().printStackTrace();
			for (Variable variable : solver.getVars()) {
//				System.out.println(var);
				String name = variable.getName();
                String value;
                if (variable instanceof IntVar) {
                    IntVar intVar = (IntVar) variable;
                    int val = intVar.getValue();
                    value = new String(val + "");

                } else if (variable instanceof SetVar) {
                    SetVar intVar = (SetVar) variable;
                    int[] val = intVar.getValues();
                    value = Arrays.toString(val);

                } else if (variable instanceof BitsetIntVarImpl) {
                    BitsetIntVarImpl intVar = (BitsetIntVarImpl) variable;
                    int val = intVar.getValue();
                    value = new String(val + "");
                } else
                    throw new RuntimeException();
                System.out.println(String.format("%-20s%-20s%-20s", name,
                        value, variable.getClass().getSimpleName()));
			}
			for (Constraint c : solver.getCstrs()) {
				System.out.println(c);
			}
		}
		
		return solved;
	}
	
//	public static void testing2(String inputFile)
//			throws JSONException, IOException {
//		InputStream is = new FileInputStream(inputFile);
//		String rawData = readInput(is);
//		is.close();
//		JSONObject jsonInput = new JSONObject(rawData);
//		
//		Schedule scheduler = new Schedule(jsonInput);
//		
//		if (scheduler.findSolution())
//			//scheduler.printSolution();
//			System.out.println("Finished.");
//	}
	
//	public static void aTest() {
//
//        Solver s = new Solver();
//        s.set(new RecorderExplanationEngine(s));
//        ConflictBasedBackjumping cbj = new ConflictBasedBackjumping(s.getExplainer());
//        // Then active end-user explanation
//        cbj.activeUserExplanation(true);
//
//        IntVar one = VF.fixed(1, s);
//        IntVar three = VF.fixed(3, s);
//        IntVar four = VF.fixed(4, s);
//        IntVar six = VF.fixed(6, s);
//        IntVar seven = VF.fixed(7, s);
//
//        IntVar x = VF.integer("x", 1, 10, s);
//        IntVar y = VF.integer("y", 1, 10, s);
//
//        IntVar[] variables = new IntVar[]{x, y};
//
//        Constraint xGE3 = ICF.arithm(x, ">=", three);
//        Constraint xLE4 = ICF.arithm(x, "<=", four);
//
//        Constraint yGE6 = ICF.arithm(y, ">=", six);
//        Constraint yLE7 = ICF.arithm(y, "<=", seven);
//
//        s.post(xGE3);
//        s.post(xLE4);
//        s.post(yGE6);
//        s.post(yLE7);
//
//        Constraint xE1 = ICF.arithm(x, "=", one);
//        s.post(xE1);
//
//        Chatterbox.showDecisions(s);
//        Boolean solve = s.findSolution();
//        if (solve) {
//            while (solve) {
//                System.out.println("---");
//
//                for (Variable variable : variables) {
//                    String name = variable.getName();
//                    String value;
//                    if (variable instanceof IntVar) {
//                        IntVar intVar = (IntVar) variable;
//                        int val = intVar.getValue();
//                        value = new String(val + "");
//
//                    } else if (variable instanceof SetVar) {
//                        SetVar intVar = (SetVar) variable;
//                        int[] val = intVar.getValues();
//                        value = Arrays.toString(val);
//
//                    } else if (variable instanceof BitsetIntVarImpl) {
//                        BitsetIntVarImpl intVar = (BitsetIntVarImpl) variable;
//                        int val = intVar.getValue();
//                        value = new String(val + "");
//                    } else
//                        throw new RuntimeException();
//                    System.out.println(String.format("%-20s%-20s%-20s", name,
//                            value, variable.getClass().getSimpleName()));
//                }
//
//                solve = s.nextSolution();
//            }
//        } else {
//            // If the problem has no solution, the end-user explanation can be retrieved
//            System.out.println(cbj.getUserExplanation());
//            System.out.println("No solution found.");
//            System.out.println(s.getEngine().getContradictionException());
//        }
//
//        System.out.println("Done.");
//    }
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//testing2("/home/dttvinh/snippet_spr15_schedule.json");
		Event e0 = new Event(0 /*id*/, 50 /*maxParticipants*/,
				2 /*daysCount*/, 50 /*duration*/,
				new String[]{"M0930"} /*startTimes*/,
				0 /*personId*/, 1 /*spaceId*/);
		
		Event e1 = new Event(1 /*id*/, 70 /*maxParticipants*/,
				2 /*daysCount*/, 70 /*duration*/,
				new String[]{"M0930"} /*startTimes*/,
				1 /*personId*/, 1 /*spaceId*/);
		
		Event e2 = new Event(2 /*id*/, 50 /*maxParticipants*/,
				2 /*daysCount*/, 50 /*duration*/,
				new String[]{"T0930"} /*startTimes*/,
				1 /*personId*//*, 1 /*spaceId*/);
		
		Space s0 = new Space(0, 60);
		Space s1 = new Space(1, 70);
		
		Schedule sc = new Schedule("test schedule", new Event[]{e0, e1, e2},
				new Space[] {s0, s1});
		
		boolean result = sc.findSolution();
		System.out.println("has_solution = " + result);
		
//		System.out.println("\nSolver Test");
//		aTest();
	}
	
	public class EventPOJO {
		public int ID;
		public char[] days;
		public int roomID;
		public String startTime;
		public boolean wasFailure;
	}
}
