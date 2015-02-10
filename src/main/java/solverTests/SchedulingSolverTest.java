package solverTests;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import scheduleSolver.*;
import scheduleSolver.Schedule.EventPOJO;


/**
 * 
 * A simple testing class for the Scheduling solver. Each test will return a boolean
 * 
 * true == Pass
 * false == fail
 * 
 *
 */
public class SchedulingSolverTest 
{
	public static boolean test1()
	{
		try 
		{
			String json = "{\"EVENT\":[{\"id\":313,\"days_count\":2,\"duration\":\"80\",\"pStartTm\":[\"T0730\",\"H0730\"],\"space\":80,\"max_participants\":97,\"persons\":15,\"constraint\":[]}],\"SPACE\":[{\"id\":80,\"capacity\":97,\"times\":\"\"}]}";
			ParsedData data = parseJson(json);
		
			Schedule schedule = new Schedule("", data.events, data.rooms);
		
			List<EventPOJO> solution =  schedule.getSolution();
		
			if(solution.get(0).wasFailure)
			{
				return false;
			}
		}
		catch(Exception e)
		{
			return false;
		}
		
		return true;
	}
	
	public static boolean test2()
	{
		try 
		{
			String json = "{\"EVENT\":[{\"id\":316,\"days_count\":2,\"duration\":\"80\",\"pStartTm\":[\"T0730\",\"H0730\"],\"space\":82,\"max_participants\":103,\"persons\":15,\"constraint\":[]},{\"id\":317,\"days_count\":2,\"duration\":\"80\",\"pStartTm\":[\"T0730\",\"H0730\"],\"space\":82,\"max_participants\":103,\"persons\":16,\"constraint\":[]}],\"SPACE\":[{\"id\":82,\"capacity\":103,\"times\":\"\"},{\"id\":83,\"capacity\":102,\"times\":\"\"}]}";
			ParsedData data = parseJson(json);
		
			Schedule schedule = new Schedule("", data.events, data.rooms);
		
			List<EventPOJO> solution =  schedule.getSolution();
		
			if(solution.get(0).wasFailure)
			{
				return true;
			}
		}
		catch(Exception e)
		{
			return false;
		}
		
		return false;
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

	public static void main(String[] args) 
	{
		boolean failed = false;
		
		//TEST 1
		boolean testPassed = test1();
		failed = failed && !testPassed;
			
		if(!testPassed)
		{
			System.out.println("test1 failed");
		}
		
		//TEST 2
		testPassed = test2();
		failed = failed && !testPassed;
			
		if(!testPassed)
		{
			System.out.println("test2 failed");
		}
		
		
		//WRAP UP
		if(!failed)
		{
			System.out.println("All tests Passed");
		}
	}

	private static ParsedData parseJson(String json) throws JSONException
	{
		ParsedData data = new ParsedData();
		
		JSONObject toCheck = new JSONObject(json);
		
		JSONArray jsonClasses = toCheck.getJSONArray("EVENT");
		JSONArray jsonResources = toCheck.getJSONArray("SPACE");
		
		data.events = parseClasses(jsonClasses);
		data.rooms = parseRooms(jsonResources);
		
		return data;
	}
	
	
	private static Event[] parseClasses(JSONArray jsonEvents) throws JSONException 
	{
		Event[] events = new Event[jsonEvents.length()];
		for (int i = 0; i < events.length; i++) 
		{
			JSONObject obj = jsonEvents.getJSONObject(i);
			
			int id = obj.getInt("id");
			int days_count = obj.getInt("days_count");
			int duration = obj.getInt("duration");
			
			//get the string array of possible start times
			JSONArray pStartTmArray = obj.getJSONArray("pStartTm");
			String[] pStartTm = new String[pStartTmArray.length()];
			
			for(int j = 0; j < pStartTm.length; j++)
			{
				pStartTm[j] = pStartTmArray.get(j).toString();
			}
			
			int space = obj.getInt("space");
			int max_participants = obj.getInt("max_participants");
			int person = obj.getInt("persons");
			
			events[i] = new Event(id, max_participants,  days_count, duration, pStartTm, person, space);
		}
		
		return events;
	}
	
	private static Space[] parseRooms(JSONArray jsonSpaces) throws JSONException
	{
		Space[] rooms = new Space[jsonSpaces.length()];
		
		for(int i = 0; i < rooms.length; i++)
		{
			JSONObject room = jsonSpaces.getJSONObject(i);
			int id = room.getInt("id");
			int capacity = room.getInt("capacity");
			//String times = room.getString("times");
			
			rooms[i] = new Space(id, capacity);
		}
		
		return rooms;
	}

	
	private static class ParsedData
	{
		public Event[] events;
		public Space[] rooms;
	}
}
