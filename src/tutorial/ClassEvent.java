package tutorial;

import solver.Solver;
import solver.constraints.ICF;
import solver.variables.IntVar;
import solver.variables.VF;

public class ClassEvent {

	private int capacity, frequency, duration, startTimes[];
	private String name, professor, sectionNumber, roomID;
	private IntVar inSessionTimes[];
	private Converter converter;
	private Solver solver;
	
	public ClassEvent(String n, int c, int f, int d, Solver s){
		name = n;
		capacity = c;
		frequency = f;
		duration = d;
		converter = new Converter();
		solver = s;
		startTimes = calcStartTimes();
		inSessionTimes = calcInSessionTimes();
	}
	
	private IntVar[] calcInSessionTimes(){
		IntVar[] times = new IntVar[duration/5];
		int i, j;
		
		times[0] = VF.enumerated(name, startTimes, solver); 
		for(i=1; i<duration/5; ++i){
			int[] iTimes = new int[startTimes.length];
			for(j=0; j<startTimes.length; ++j)
				iTimes[j] = startTimes[j] + i*5;
			//solver.post(ICF.arithm(times[i], "==", times[i-1], "+", 5));
			times[i] = VF.enumerated(name, iTimes, solver);
		}
		return times;
	}
	
	private int[] calcStartTimes(){
		
		if(frequency == 1){
			if(duration == 50){
				//TODO: make this correct
				return new int[]{90};
			}
			else if(duration == 80){
				//TODO: make this correct
				return new int[]{90};
			}
			
		} 
		else if(frequency == 2){
			//TODO: make this correct
			return new int[]{90};
		}
		else if(frequency == 3){
			return converter.convertDayTimeToMinutes(new String[]{"M07:30","M08:35","M09:40","M10:45","M11:50","M12:55","M14:00","M15:05","M16:10"});
		}
		else if(frequency == 4){
			//TODO: make this correct
			return new int[]{90};
		}
		else if(frequency == 5){
			//TODO: make this correct
			return new int[]{90};
		}
		return new int[]{}; /* something is wrong */
	}
}
