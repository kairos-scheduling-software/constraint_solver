package solverTests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import scheduleSolver.Schedule;
import scheduleSolver.Schedule.SolutionLevel;
import util.EventData;
import util.ScheduleData;
import util.SpaceData;
import util.TimeData;

public class SolverPerfTest {
	static int COUNT = 5;
	static int baseSize = 10;
	
	int evCount, sizeRange;
	double ratio;
	ArrayList<TimeData> timeList;
	
	public SolverPerfTest(int evCount, double ratio, int sizeRange, ArrayList<TimeData> timeList) {
		this.evCount = evCount;
		this.ratio = ratio;
		this.sizeRange = sizeRange;
		this.timeList = new ArrayList<>(timeList);
	}
	
	public void run() {
		long totalTime = 0;
		long startTime, endTime;
		for (int i = 0; i < COUNT; i++) {
			ScheduleData data = initialize();
			Schedule scheduleSolver = new Schedule(data);
			
			startTime = System.currentTimeMillis();
			scheduleSolver.getSolution(SolutionLevel.CONFLICTS);
			endTime = System.currentTimeMillis();
			
			totalTime += (endTime - startTime);
		}
		double avgTime = totalTime / (float) (COUNT * 1000);
		
		System.out.printf("Events: %d, Resource ratio: %.2f, Average time: %.2f seconds\n", evCount, ratio, avgTime);
	}
	
	private ScheduleData initialize() {
		int resourceCount = (int) (evCount * ratio);
		EventData[] events = new EventData[evCount];
		SpaceData[] spaces = new SpaceData[resourceCount];
		
		Random rand = new Random();
		
		for (int i = 0; i < evCount; i++) {
			events[i] = new EventData(i, baseSize + rand.nextInt(sizeRange),
					timeList.get(rand.nextInt(timeList.size())),
					rand.nextInt(resourceCount), null);
		}
		
		for (int i = 0; i < resourceCount; i++) {
			spaces[i] = new SpaceData(i, baseSize + rand.nextInt(sizeRange));
		}
		
		return new ScheduleData("test schedule", events, spaces, null);
	}
	
	private static ArrayList<TimeData> getTimeList() {
		String[] mwStimes = new String[]{"0805", "1150", "1325", "1500"};
        String[] tthStimes = new String[]{"0730", "0910", "1045", "1225", "1400", "1540"};
        String[] mwfSTimes = new String[]{"0730", "0835", "0940", "1045", "1150", "1255", "1400", "1505", "1610"};
		
        ArrayList<TimeData> timeList = new ArrayList<TimeData>();
        HashMap<String, String[]> timeMap = null;
        
        ///// 80min block
        // 1 day
        timeMap = new HashMap<String, String[]>();
        timeMap.put("M", mwStimes);
        timeMap.put("W", mwStimes);
        timeMap.put("T", tthStimes);
        timeMap.put("H", tthStimes);
        timeList.add(new TimeData(timeMap, 80));
        // 2 days
        timeMap = new HashMap<String, String[]>();
        timeMap.put("MW", mwStimes);
        timeMap.put("TH", tthStimes);
        timeList.add(new TimeData(timeMap, 80));
        
        ///// 50min block
        // 1 day
        timeMap = new HashMap<String, String[]>();
        timeMap.put("M", mwfSTimes);
        timeMap.put("W", mwfSTimes);
        timeMap.put("F", mwfSTimes);
        timeList.add(new TimeData(timeMap, 50));
        // 2 days
        timeMap = new HashMap<String, String[]>();
        timeMap.put("MW", mwfSTimes);
        timeList.add(new TimeData(timeMap, 50));
        // 3 days
        timeMap = new HashMap<String, String[]>();
        timeMap.put("MWF", mwfSTimes);
        timeList.add(new TimeData(timeMap, 50));
        
        return timeList;
	}
	
	public static void main(String[] args) {
		ArrayList<TimeData> timeList = getTimeList();
		
		for (double r = 0.2; r <= 0.5; r += 0.1) {
			for (int i = 10; i <= 100; i += 10) {
				SolverPerfTest solverTest = new SolverPerfTest(i, r, 10, timeList);
				solverTest.run();
			}
		}
	}
}
