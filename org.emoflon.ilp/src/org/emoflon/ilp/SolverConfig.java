package org.emoflon.ilp;

public record SolverConfig(SolverType solver, boolean timeoutEnabled, double timeout, boolean randomSeedEnabled,
		int randomSeed, boolean toleranceEnabled, double tolerance, boolean boundsEnabled, int lowerBound,
		int upperBound, boolean presolveEnabled, boolean debugOutputEnabled, String debugOutputPath,
		boolean outputEnabled, String outputPath) {

	/*
	 * TODO timeout; random seed; tolerance; ILP solver itself (e.g., Gurobi, CPLEX,
	 * ...); Presolve; debug output; "Save the LP as output file to path xy"
	 */

	public enum SolverType {
		GUROBI, CPLEX, GLPK
	}
}
