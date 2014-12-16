package tutorial;

import solver.Solver;
import solver.variables.IntVar;
import solver.variables.VF;

public class ClassEvent {

	private int capacity, frequency, duration, startTimes[];
	private String name, professor, sectionNumber, roomID;
	private IntVar inSessionTimes[];
	private Converter converter;
	private Solver solver;
	
	public ClassEvent(String n, int c, int f, int d){
		name = n;
		capacity = c;
		frequency = f;
		duration = d;
		converter = new Converter();
		solver = new Solver(n);
		startTimes = calcStartTimes();
		inSessionTimes = calcInSessionTimes();
	}
	
	private IntVar[] calcInSessionTimes(){
		IntVar[] times = new IntVar[duration/5];
		int i;
		
		times[0] = VF.enumerated(name, startTimes, solver); 
		for(i=1; i<duration/5; ++i){
			times[i]
		}
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
			return converter.convertDayTimeToMinutes(new String[]{"M0730","M0835","M0940","M1045","M1150","M1255","M1400","M1505","M1610"});
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
