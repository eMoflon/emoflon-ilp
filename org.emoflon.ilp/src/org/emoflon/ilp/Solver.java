package org.emoflon.ilp;

public interface Solver {

	/**
	 * Translates the problem formulation to be comprehensible for the solver
	 * configured in SolverConfig.
	 * 
	 * @param problem Problem formulation to be optimized.
	 */
	public abstract void buildILPProblem(Problem problem);

	/**
	 * Solves the problem.
	 * 
	 * @return Result of the solving process.
	 */
	public abstract SolverOutput solve();

	/**
	 * Sets the values of the variables to the resulting values after solving the
	 * problem.
	 */
	public abstract void updateValuesFromSolution();

	/**
	 * Terminates the solver after solving the problem.
	 */
	public abstract void terminate();

	/**
	 * Resets the solver.
	 */
	public abstract void reset();

}
