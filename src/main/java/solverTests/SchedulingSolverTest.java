package solverTests;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
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
	public static boolean test0()
	{
		try 
		{
			String inputFile = "test0.json";
			InputStream is = new FileInputStream(inputFile);
			String json = readInput(is);
			ParsedData data = parseJson(json);
		
			Schedule schedule = new Schedule("", data.events, data.rooms);
		
			List<EventPOJO> solution =  schedule.getSolution();
		
			if(! solution.get(0).wasFailure)
			{
				return true;
			}
		}
		catch(Exception e)
		{
			System.out.println("caught exception in test0");
			e.printStackTrace(new PrintStream(System.out));
			return false;
		}
		
		return false;
	}
	
	public static boolean test1()
	{
		try 
		{
			String json = "{\"EVENT\":[{\"id\":313,\"days_count\":2,\"duration\":\"80\"," +
					"\"pStartTm\":{\"TH\":[\"0730\"]},\"space\":80,\"max_participants\":97," +
					"\"persons\":15,\"constraint\":[]}],\"SPACE\":[{\"id\":80," +
					"\"capacity\":97,\"times\":\"\"}]}";
			ParsedData data = parseJson(json);
		
			Schedule schedule = new Schedule("", data.events, data.rooms);
		
			List<EventPOJO> solution =  schedule.getSolution();
		
			if(solution.get(0).wasFailure)
			{
				System.out.println("solution.get(0) was failure");
				return false;
			}
		}
		catch(Exception e)
		{
			System.out.println("caught exception in test1");
			System.out.println(e.getMessage());
			e.printStackTrace(new PrintStream(System.out));
			return false;
		}
		
		return true;
	}
	
	public static boolean test2()
	{
		try 
		{
			String json = "{\"EVENT\":[{\"id\":316,\"days_count\":2,\"duration\":80," +
					"\"pStartTm\":{\"TH\":[\"0730\"]},\"space\":82," +
					"\"max_participants\":103,\"persons\":15,\"constraint\":[]}," +
					"{\"id\":317,\"days_count\":2,\"duration\":80," +
					"\"pStartTm\":{\"TH\":[\"0730\"]}," +
					"\"space\":82,\"max_participants\":103,\"persons\":16," +
					"\"constraint\":[]}],\"SPACE\":[{\"id\":82,\"capacity\":103," +
					"\"times\":\"\"},{\"id\":83,\"capacity\":102,\"times\":\"\"}]}";
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
			System.out.println("caught exception in test2");
			e.printStackTrace(new PrintStream(System.out));
			return false;
		}
		
		return false;
	}

	public static boolean test3(){
		try{
			String json = "{\"EVENT\":[],\"SPACE\":[]}";
			ParsedData data = parseJson(json);
			Schedule schedule = new Schedule("", data.events, data.rooms);
			List<EventPOJO> solution = schedule.getSolution();
			if(solution.get(0).wasFailure)
				return false;
		}
		catch(Exception e){
			System.out.println("caught exception in test3");
			e.printStackTrace(new PrintStream(System.out));
			return false;
		}
		return true;
	}

	public static void main(String[] args) 
	{	
		boolean passed = test0();
			
		if(!passed)
		{
			System.out.println("test0 failed");
		}
		
		//TEST 1
		boolean testPassed = test1();
		passed = passed && testPassed;
			
		if(!testPassed)
		{
			System.out.println("test1 failed");
		}
				
		//TEST 2
		testPassed = test2();
		passed = passed && testPassed;
			
		if(!testPassed)
		{
			System.out.println("test2 failed");
		}
		
		//WRAP UP
		if(passed) System.out.println("All tests Passed");
		else System.out.println("At least one test failed");
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
