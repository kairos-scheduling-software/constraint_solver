package solverTests;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import scheduleSolver.Schedule;
import util.ScheduleData;
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
	private static Map<String, String> output = new HashMap<String, String>();
	
	public static boolean test0()
	{
		return runFileTest("test0", "test0.json", false);
	}
	
	public static boolean test1()
	{
		String json = "{\"EVENT\":[{\"id\":313,\"days_count\":2,\"duration\":\"80\"," +
			"\"pStartTm\":{\"TH\":[\"0730\"]},\"space\":80,\"max_participants\":97," +
			"\"persons\":15,\"constraint\":[]}],\"SPACE\":[{\"id\":80," +
			"\"capacity\":97,\"times\":\"\"}]}";
		
		return runTest("test1", json, true);
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
		
		return runTest("test2", json, false);
	}

	public static boolean test3() {
		String json = "{\"EVENT\":[],\"SPACE\":[]}";
		return runTest("test3", json, true);
	}
	
	public static boolean test4() {
		String fileName = "snippet_spr15_schedule.json";
		boolean result = runFileTest("test_snippet_spr_15", fileName, true);
		
		return result;
	}
	
	public static boolean test5() {
		String fileName = "new_spr15_schedule.json";
		boolean result = runFileTest("test_spr_15", fileName, true);
		
		System.out.println(output.get("test_spr_15"));
		
		return result;
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
		String json;
		for(int i=0; i<paths.length; i++){
			json = "";
			try{
				BufferedReader br = new BufferedReader(new FileReader(paths[i]));
				String line;
				while((line = br.readLine()) != null)
					json += line;
				br.close();
				runTest(new String("goodTest_"+Integer.toString(i)), json, true);
			}
			catch(IOException ioe){
				System.out.println("caught IOException while attempting to read file " +
						paths[i]);
			}
		}
		// for path in paths
		// open file
		// parse file as json
		// create schedule
		// schedule should pass
	}

	public static void main(String[] args) throws FileNotFoundException 
	{	
		
		boolean[] passed = new boolean[] {test0(), test1(), test2()};
		
		String[] paths = {"jsonTestFiles/one_class_one_room"};
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
		
		System.out.println("Test for spr15 cs schedule snippet");
		test4();
		
		test5();
	}

	private static boolean runTest(String testName, String json, boolean expectedResult) {
		System.out.println("Running test " + testName);
		try 
		{
			ScheduleData data = ScheduleData.parseJson(json);
			Schedule schedule = new Schedule("", data.events, data.spaces);
			JSONObject solution =  new JSONObject(schedule.getSolution2());
			
			output.put(testName, solution.toString());
			
			boolean result = (expectedResult != solution.getBoolean("wasFailure"));
			
			if (!result) System.out.println(testName + " failed");
			else System.out.println(testName + " passed");
			return result;
		}
		catch(Exception e)
		{
			System.out.println("Caught exception in " + testName);
			e.printStackTrace(new PrintStream(System.out));
			return false;
		}
	}
	
	private static boolean runFileTest(String testName, String fileName, boolean expectedResult) {
		try {
			InputStream is = new FileInputStream(fileName);
			String json = readInput(is, true);
			is.close();
			return runTest(testName, json, expectedResult);
		} catch (IOException e) {
			System.out.println(testName + " IOException");
			return false;
		}
	}
}
