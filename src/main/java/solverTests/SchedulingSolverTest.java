package solverTests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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
		
		System.out.println(output.get("test_snippet_spr_15"));
		
		return result;
	}
	
	public static boolean test5() {
		String fileName = "new_spr15_schedule.json";
		boolean result = runFileTest("test_spr_15", fileName, true);
		
		System.out.println(output.get("test_spr_15"));
		
		return result;
	}
	
	public static void testSchedules(String[] paths, boolean expected){
		String json;
		for(int i=0; i<paths.length; i++){
			json = "";
			try{
				BufferedReader br = new BufferedReader(new FileReader(paths[i]));
				String line;
				while((line = br.readLine()) != null)
					json += line;
				br.close();
				runTest(new String("Test_"+paths[i]), json, expected);
			}
			catch(IOException ioe){
				System.out.println("caught IOException while attempting to read file " +
						paths[i]);
			}
		}
	}

	public static void main(String[] args) throws FileNotFoundException 
	{
		
		boolean[] passed = new boolean[] {test0(), test1(), test2(), test3()};
		String p = "jsonTestFiles/";
		
		// test satisfiable schedules
		String[] noConflictScheds = {p+"f2000_no_labs_discs",p+"f2000Cleaned"};
		testSchedules(noConflictScheds, true);
		
		// test unsatisfiable schedules
		String[] conflictedScheds = {p+"f2000_with_labs_discs"};
		testSchedules(conflictedScheds, false);
		
		//WRAP UP
		boolean allPassed = true;
		for (boolean b : passed) {
			if(!b) {
				allPassed = false;
				break;
			}
		}
		System.out.println("tests complete");
		//if (!allPassed) System.out.println("At least one test failed");
		//else System.out.println("All tests Passed");
		
		//System.out.println("Test for spr15 cs schedule snippet");
		//test4();
		
		//test5();
		
		//runInteractiveTest();
	}

	private static boolean runTest(String testName, String json, boolean expectedResult) {
		System.out.println("Running test " + testName);
		try 
		{
			ScheduleData data = ScheduleData.parseJson(json);
			Schedule schedule = new Schedule("", data.events, data.spaces, data.constraints);
			JSONObject solution =  new JSONObject(schedule.getSolution(2));
			
			output.put(testName, solution.toString());
			
			boolean result = (expectedResult != solution.getBoolean("wasFailure"));
			
			if (!result) System.out.println(testName + " failed");
			else System.out.println(testName + " passed");
			return result;
		}
		catch(Exception e)
		{
			System.out.println("Caught exception in " + testName);
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean runFileTest(String testName, String fileName, boolean expectedResult) {
		Scanner sc;
		try {
			sc = new Scanner(new File(fileName));
			String json = readInput(sc, true);
			sc.close();
			return runTest(testName, json, expectedResult);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	private static void runInteractiveTest() {
		Scanner sc = new Scanner(System.in);
		while(true) {
			System.out.println("JSON string:");
			String json = readInput(sc, false);
			if (json.length() == 0) break;
			char reply = ' ';
			while (reply != 'Y' && reply != 'N') {
				System.out.print("Valid schedule? (Y/N) ");
				String line = sc.nextLine();
				if (line.length() == 1) reply = line.toUpperCase().charAt(0);
			}
			boolean expected = (reply == 'Y');
			
			if (!runTest("Interactive test", json, expected)) {
				System.out.println("Output JSON:");
				System.out.println(output.get("Interactive test"));
			}
		}
	}
}
