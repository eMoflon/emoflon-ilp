package org.emoflon.ilp;

/**
 * Interface for all implemented Solvers.
 * 
 * @see GurobiSolver
 * @see GlpkSolver
 * @see CplexSolver
 */
public interface Solver {

	/**
	 * Translates the problem formulation to be comprehensible for the solver
	 * configured in SolverConfig.
	 * 
	 * @param problem Problem formulation to be optimized.
	 * @see Problem
	 */
	public abstract void buildILPProblem(Problem problem);

	/**
	 * Solves the problem.
	 * 
	 * @return Result of the solving process.
	 * @see SolverOutput
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
