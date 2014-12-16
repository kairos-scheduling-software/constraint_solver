package chocoTesting;

import solver.Solver;
import solver.constraints.ICF;
import solver.search.strategy.IntStrategyFactory;
import solver.trace.Chatterbox;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

public class TestChoco {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Solver solver = new Solver("n queens");
		int n = 16;  /* the number of queens */
		int i, j;    /* counters */
		
		/* create an array of n IntVars, each with a range of [0..n-1] */
		IntVar[] rows = VariableFactory.enumeratedArray("rows", n, 0, (n-1), solver);
		
		/* make sure that whatever value we select for each row is unique */
		solver.post(ICF.alldifferent(rows));
			
		/* make sure that diagonals are distinct */
		for(i=0; i<rows.length; ++i)
			for(j=0; j<rows.length; ++j)
				if(i != j){
					solver.post(ICF.arithm(rows[j], "!=", rows[i], "+", (i-j)));
					solver.post(ICF.arithm(rows[j], "!=", rows[i], "+", (j-i)));
				}
			
		/* need to know syntax to change variable selection strategy */
		//solver.set(IntStrategyFactory.minDomainSize_var_selector());
		
		/* choose a strategy, no idea what is what here */
		solver.set(IntStrategyFactory.lexico_LB(rows));
		
		/* print any solution that is found */
		Chatterbox.showSolutions(solver);
		
		/* find a solution (if one exists) */
		solver.findSolution();
		
		/* print solver statistics */
		Chatterbox.printStatistics(solver);
	}
}