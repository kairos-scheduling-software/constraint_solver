package solverTests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.Gson;

import scheduleSolver.Schedule;
import scheduleSolver.Schedule.SolutionLevel;
import static scheduleSolver.Schedule.SolutionLevel.*;
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
public class SchedulingSolverTest {
	private static SolutionLevel SOLUTION_OUTPUT_LEVEL = CONFLICTS;
	private static boolean RUN_ALL_TEST = true;
	private static boolean RUN_BIG_TEST = true;
	private static boolean INTERACTIVE_MODE = false;
	
	private static Map<String, String> outputs = new HashMap<String, String>();
	private static Map<String, Boolean> results = new HashMap<String, Boolean>();
	private static int interactiveTestCount = 0;
	
	public static boolean test0()
	{
		return runFileTest("test0", "test0.json", false);
	}
	
	public static boolean test1()
	{
		String json = "{\"EVENTS\":[{\"id\":313,\"maxParticipants\":97," +
				"\"time\":{\"duration\":80,\"startTimes\":{\"TH\":[\"0730\"]}}," +
				"\"spaceId\":[80],\"personId\":15}]," +
				"\"SPACES\":[{\"id\":80,\"capacity\":97,\"times\":\"\"}]}";
		
		return runTest("test1", json, true);
	}
	
	public static boolean test2()
	{
		String json = "{\"EVENTS\":[{\"id\":316,\"maxParticipants\":100," +
				"\"time\":{\"duration\":80,\"startTimes\":{\"TH\":[\"0730\"]}}," +
				"\"spaceId\":[82,83],\"personId\":15}," +
				"{\"id\":317,\"maxParticipants\":103," +
				"\"time\":{\"duration\":80,\"startTimes\":{\"TH\":[\"0730\"]}}," +
				"\"spaceId\":82,\"personId\":16}]," +
				"\"SPACES\":[{\"id\":82,\"capacity\":103," +
				"\"times\":\"\"},{\"id\":83,\"capacity\":102}]}";
		
		return runTest("test2", json, true);
	}

	public static boolean test3() {
		String json = "{\"EVENTS\":[],\"SPACES\":[]}";
		return runTest("test3", json, true);
	}
	
	public static boolean test4() {
		String fileName = "snippet_spr15_schedule.json";
		boolean result = runFileTest("test_snippet_spr_15", fileName, true);
		
		System.out.println(outputs.get("test_snippet_spr_15"));
		
		return result;
	}
	
	public static boolean test5() {
		String fileName = "new_spr15_schedule.json";
		boolean result = runFileTest("test_spr_15", fileName, true);
		
		System.out.println(outputs.get("test_spr_15"));
		
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
				String testName = "Test_"+paths[i];
				if (!runTest(testName, json, expected)) {
					System.out.println(testName + " output:");
					System.out.println(outputs.get(testName));
				}
				
			}
			catch(IOException ioe){
				System.out.println("caught IOException while attempting to read file " +
						paths[i]);
			}
		}
	}

	public static boolean runTest(String testName, String json, boolean expectedResult) {
		System.out.println("Running test " + testName);
		try {
			ScheduleData data = ScheduleData.parseJson(json);
			String outputStr;
			boolean result;
			if (data != null) {
				Schedule schedule = new Schedule(data);
				Map<String, Object> outputMap = schedule.getSolution(SOLUTION_OUTPUT_LEVEL);
				Gson gson = new Gson();
				outputStr = gson.toJson(outputMap);
				result = (expectedResult != (Boolean) outputMap.get("wasFailure"));
			} else {
				outputStr = "Invalid Json String.";
				result = false;
			}
			outputs.put(testName, outputStr);
			
			if (!result) System.out.println("\t" + testName + " failed");
			else System.out.println("\t" + testName + " passed");
			results.put(testName, result);
			return result;
		}
		catch(Exception e) {
			System.out.println("Caught exception in " + testName);
			e.printStackTrace();
			results.put(testName, false);
			return false;
		}
	}
	
	public static boolean runFileTest(String testName, String fileName, boolean expectedResult) {
		Scanner sc;
		try {
			sc = new Scanner(new File(fileName));
			String json = readInput(sc, true);
			sc.close();
			return runTest(testName, json, expectedResult);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static void runInteractiveTest() {
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
			
			String testName = "Interactive test " + interactiveTestCount++;
			runTest(testName, json, expected);
			
			System.out.println("Output JSON:");
			System.out.println(outputs.get(testName));
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException 
	{
		if (RUN_ALL_TEST) {
			test0();
			test1();
			test2();
			test3();
			String p = "jsonTestFiles/";
			
			// test satisfiable schedules
			String[] noConflictScheds = {p+"f2000NoLabsDiscs",p+"f2000Cleaned"};
			testSchedules(noConflictScheds, true);
			
			// printing output to see why it failed
			// output.get("f2000_no_labs_discs")
			//System.out.println(output.get(p+"f2000_no_labs_discs"));
			
			// test unsatisfiable schedules
			String[] conflictedScheds = {p+"f2000_with_labs_discs",
					p + "big_event.json"};
			testSchedules(conflictedScheds, false);
			
			if (RUN_BIG_TEST) {
				System.out.println("Test for spr15 cs schedule snippet");
				test4();
				
				test5();
			}
		}
		
		if (INTERACTIVE_MODE) runInteractiveTest();
		
		//WRAP UP
		System.out.println("tests complete");
		int pass = 0;
		int total = results.size();
		for (Boolean result : results.values()) {
			if (result) pass += 1;
		}
		System.out.println("SUMMARY");
		System.out.printf("Passed: %d, Failed: %d, Total: %d\n", pass, total - pass, total);
//		if (!allPassed) System.out.println("At least one test failed");
//		else System.out.println("All tests Passed");
	}
}
