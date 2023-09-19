package org.emoflon.ilp;

/**
 * This record class represents the configuration parameters used to configure
 * the solver.
 * 
 * @param type               Solver Type (GUROBI, GLPK)
 * @param timeoutEnabled     Set to true, if timeout should be set.
 * @param timeout            time until timeout
 * @param randomSeedEnabled  Set to true, if a seed should be set (if possible).
 * @param randomSeed         Value for the random seed.
 * @param toleranceEnabled   Set to true, if a value for the tolerance should be
 *                           set.
 * @param tolerance          Value for the tolerance.
 * @param boundsEnabled      Set to true, if custom default bounds for integer
 *                           and real variables should be set.
 * @param lowerBound         Value for the custom upper bound for integer and
 *                           real variables.
 * @param upperBound         Value for the custom upper bound for integer and
 *                           real variables.
 * @param presolveEnabled    Set to true, if the solver should carry out a
 *                           presolve phase (for GPLK you might need to set this
 *                           to true!).
 * @param debugOutputEnabled Set to true, if debug output should be written into
 *                           a file.
 * @param outputEnabled      Set to true, if the problem should be written into
 *                           a file (e.g. with the file ending ".lp")
 * @param outputPath         Path to the output file.
 *
 */
public record SolverConfig(SolverType solver, boolean timeoutEnabled, double timeout, boolean randomSeedEnabled,
		int randomSeed, boolean toleranceEnabled, double tolerance, boolean boundsEnabled, int lowerBound,
		int upperBound, boolean presolveEnabled, boolean debugOutputEnabled, boolean outputEnabled, String outputPath) {

	public enum SolverType {
		GUROBI, CPLEX, GLPK
	}
}
