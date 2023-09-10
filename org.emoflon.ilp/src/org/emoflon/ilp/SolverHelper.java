package org.emoflon.ilp;

public class SolverHelper {
	// TODO
	private Solver solver;

	public SolverHelper(SolverConfig config) {
		switch (config.solver()) {
		case GUROBI:
			this.solver = new GurobiSolver(config);
			break;
		case CPLEX:
			throw new Error("Not yet implemented!");
		// break;
		case GLPK:
			this.solver = new GlpkSolver(config);
			break;
		default:
			throw new IllegalArgumentException("This solver is not implemented in the tool.");
		}
	}

	public Solver getSolver() {
		return this.solver;
	}

}