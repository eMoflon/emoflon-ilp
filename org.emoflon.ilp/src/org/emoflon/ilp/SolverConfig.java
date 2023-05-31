package org.emoflon.ilp;

public record SolverConfig(boolean timeoutEnabled, double timeout, boolean randomSeedEnabled, int randomSeed,
		boolean toleranceEnabled, double tolerance, SolverType solver, boolean presolveEnabled,
		boolean debugOutputEnabled, String debugOutputPath, boolean outputEnabled, String outputPath) {

	/*
	 * TODO timeout; random seed; tolerance; ILP solver itself (e.g., Gurobi, CPLEX,
	 * ...); Presolve; debug output; "Save the LP as output file to path xy"
	 */

	public enum SolverType {
		GUROBI, CPLEX, GLPK
	}
}
