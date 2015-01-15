package tutorial;

import solver.Solver;
import solver.search.strategy.IntStrategyFactory;
import solver.trace.Chatterbox;
import solver.variables.IntVar;
import solver.variables.VF;

public class Tutorial {

	public static void main(String[] args){
		int numClasses, capacities=120, frequencies=3, durations=50, maxNumClasses=9, i;
		
		/* solve for one class, two classes, .... , nine classes */
		for(numClasses=1; numClasses<=maxNumClasses+1; ++numClasses){
			Solver solver = new Solver();
			/* we will have an array of ClassEvents, each 3 x 50 minute blocks */
			ClassEvent classes[] = new ClassEvent[numClasses];
			for(i=0; i<numClasses; ++i){
				System.out.println("i is " + Integer.toString(i));
				classes[i] = new ClassEvent(new String("class"+Integer.toString(i)), capacities, frequencies, durations, solver);
			}
			/* choose a strategy, no idea what is what here */
			//solver.set(IntStrategyFactory.lexico_LB(classes));
			/* print any solution that is found */
			Chatterbox.showSolutions(solver);
			/* find a solution (if one exists) */
			solver.findSolution();
		}
		
		/* assume all classes are one day per week, 50 minutes per class session, then for each classEvent ce, 
		 * ce.startTime is in {7:30, 8:35, 9:40, 10:45, 11:50, 12:55, 14:00, 15:05, 16:10}
		 * -> ce.startTime in {90, 103, 116, 129, 142, 155, 168, 181, 194}
		 */
		//IntVar[] classStartTimes = VF.enumeratedArray("Start Times", numClasses, new int[]{90, 103, 116, 129, 142, 155, 168, 181, 194}, solver);
		 
		/* each classEvent will have inSessionTimes = [startTime, startTime+5, startTime+10, ... , startTime+(duration-5)] */
	}
}
