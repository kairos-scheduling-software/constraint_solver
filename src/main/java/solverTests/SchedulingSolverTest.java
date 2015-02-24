package solverTests;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import scheduleSolver.*;
import scheduleSolver.Schedule.EventPOJO;
import static util.Json.*;
import static util.IO.*;

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
	public static boolean test0() throws FileNotFoundException
	{
		String inputFile = "test0.json";
		InputStream is = new FileInputStream(inputFile);
		String json = readInput(is);
		
		return checkSchedule("test0", json, false);
	}
	
	public static boolean test1()
	{
		String json = "{\"EVENT\":[{\"id\":313,\"days_count\":2,\"duration\":\"80\"," +
			"\"pStartTm\":{\"TH\":[\"0730\"]},\"space\":80,\"max_participants\":97," +
			"\"persons\":15,\"constraint\":[]}],\"SPACE\":[{\"id\":80," +
			"\"capacity\":97,\"times\":\"\"}]}";
		
		return checkSchedule("test1", json, false);
	}
	
	public static boolean test2()
	{
		String json = "{\"EVENT\":[{\"id\":316,\"days_count\":2,\"duration\":80," +
			"\"pStartTm\":{\"TH\":[\"0730\"]},\"space\":82," +
			"\"max_participants\":103,\"persons\":15,\"constraint\":[]}," +
			"{\"id\":317,\"days_count\":2,\"duration\":80," +
			"\"pStartTm\":{\"TH\":[\"0730\"]}," +
			"\"space\":82,\"max_participants\":103,\"persons\":16," +
			"\"constraint\":[]}],\"SPACE\":[{\"id\":82,\"capacity\":103," +
			"\"times\":\"\"},{\"id\":83,\"capacity\":102,\"times\":\"\"}]}";
		
		return checkSchedule("test2", json, true);
	}

	public static boolean test3() {
		String json = "{\"EVENT\":[],\"SPACE\":[]}";
		return checkSchedule("test3", json, true);
	}
	
	public static boolean testBadSchedules(String[] paths){
		// for path in paths
		// open file	
		// parse file as json
		// create schedule
		// schedule should pass
		return true;
	}
	
	public static void testGoodSchedules(String[] paths){
		
		// for path in paths
		// open file
		// parse file as json
		// create schedule
		// schedule should pass
	}

	public static void main(String[] args) throws FileNotFoundException 
	{	
		
		boolean[] passed = new boolean[] {test0(), test1(), test2()};
		
		String[] paths = {"../../../../jsonTestFiles/one_event_one_space"};
		testGoodSchedules(paths);
		
		//WRAP UP
		boolean allPassed = true;
		for (boolean b : passed) {
			if(!b) {
				allPassed = false;
				break;
			}
		}
		if (!allPassed) System.out.println("At least one test failed");
		else System.out.println("All tests Passed");
	}

	private static boolean checkSchedule(String testName, String json, boolean expectedResult) {
		try 
		{
			ParsedData data = parseJson(json);
		
			Schedule schedule = new Schedule("", data.events, data.rooms);
			
			JSONObject solution =  new JSONObject(schedule.getSolution2());
			
			return (expectedResult != solution.getBoolean("wasFailure"));
		}
		catch(Exception e)
		{
			System.out.println("Caught exception in " + testName);
			e.printStackTrace(new PrintStream(System.out));
			return false;
		}
	}
	
	private static ParsedData parseJson(String json) throws JSONException
	{
		ParsedData data = new ParsedData();
		
		JSONObject toCheck = new JSONObject(json);
		
		JSONArray jsonClasses = toCheck.getJSONArray("EVENT");
		JSONArray jsonResources = toCheck.getJSONArray("SPACE");
		
		data.events = parseEvents(jsonClasses);
		data.rooms = parseSpaces(jsonResources);
		
		return data;
	}
	
	private static class ParsedData
	{
		public Event[] events;
		public Space[] rooms;
	}
}
