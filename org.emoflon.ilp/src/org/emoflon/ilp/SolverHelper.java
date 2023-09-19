package org.emoflon.ilp;

/**
 * This is a helper class for keeping the solver easily parameterized.
 *
 */
public class SolverHelper {

	private Solver solver;

	/**
	 * The constructor for the solver helper.
	 * 
	 * @param config The solver config used for configuring the parameters of the
	 *               solver.
	 */
	public SolverHelper(SolverConfig config) {
		switch (config.solver()) {
		case GUROBI:
			this.solver = new GurobiSolver(config);
			break;
		case CPLEX:
			this.solver = new CplexSolver(config);
			break;
		case GLPK:
			this.solver = new GlpkSolver(config);
			break;
		default:
			throw new IllegalArgumentException("This solver is not implemented in the tool.");
		}
	}

	/**
	 * Returns the solver set by this helper.
	 * 
	 * @return The current solver set by the configuration.
	 */
	public Solver getSolver() {
		return this.solver;
	}

}