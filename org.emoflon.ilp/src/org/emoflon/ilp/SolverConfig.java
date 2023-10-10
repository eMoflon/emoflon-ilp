package org.emoflon.ilp;

/**
 * This class represents the configuration parameters used to configure the
 * solver.
 */
public class SolverConfig {
	private SolverType solver;
	private boolean timeoutEnabled;
	private double timeout;
	private boolean randomSeedEnabled;
	private int randomSeed;
	private boolean toleranceEnabled;
	private double tolerance;
	private boolean boundsEnabled;
	private int lowerBound;
	private int upperBound;
	private boolean presolveEnabled;
	private boolean debugOutputEnabled;
	private boolean outputEnabled;
	private String outputPath;

	/**
	 * Creates a new instance of the solver configuration.
	 * 
	 * @param type               Solver Type (GUROBI, GLPK, CPLEX)
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
	 */
	public SolverConfig(SolverType solver, boolean timeoutEnabled, double timeout, boolean randomSeedEnabled,
			int randomSeed, boolean toleranceEnabled, double tolerance, boolean boundsEnabled, int lowerBound,
			int upperBound, boolean presolveEnabled, boolean debugOutputEnabled, boolean outputEnabled,
			String outputPath) {
		this.solver = solver;

		this.timeoutEnabled = timeoutEnabled;
		this.timeout = timeout;
		this.randomSeedEnabled = randomSeedEnabled;
		this.randomSeed = randomSeed;
		this.toleranceEnabled = toleranceEnabled;
		this.tolerance = tolerance;
		this.boundsEnabled = boundsEnabled;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.presolveEnabled = presolveEnabled;
		this.debugOutputEnabled = debugOutputEnabled;
		this.outputEnabled = outputEnabled;
		this.outputPath = outputPath;
	}

	public SolverConfig() {
		this.solver = SolverType.GLPK;
		this.timeoutEnabled = false;
		this.timeout = -1;
		this.randomSeedEnabled = false;
		this.randomSeed = -1;
		this.toleranceEnabled = false;
		this.tolerance = -1;
		this.boundsEnabled = false;
		this.lowerBound = -1;
		this.upperBound = -1;
		this.presolveEnabled = true;
		this.debugOutputEnabled = true;
		this.outputEnabled = false;
		this.outputPath = "/dev/null";
	}

	/**
	 * Type of the Solver <br>
	 * 
	 * GUROBI, GLPK, or CPLEX
	 */
	public enum SolverType {
		GUROBI, CPLEX, GLPK
	}

	public SolverType getSolver() {
		return solver;
	}

	public void setSolver(final SolverType solver) {
		this.solver = solver;
	}

	public boolean isTimeoutEnabled() {
		return timeoutEnabled;
	}

	public void setTimeoutEnabled(final boolean timeoutEnabled) {
		this.timeoutEnabled = timeoutEnabled;
	}

	public double getTimeout() {
		return timeout;
	}

	public void setTimeout(final double timeout) {
		this.timeout = timeout;
	}

	public boolean isRandomSeedEnabled() {
		return randomSeedEnabled;
	}

	public void setRandomSeedEnabled(final boolean randomSeedEnabled) {
		this.randomSeedEnabled = randomSeedEnabled;
	}

	public int getRandomSeed() {
		return randomSeed;
	}

	public void setRandomSeed(final int randomSeed) {
		this.randomSeed = randomSeed;
	}

	public boolean isToleranceEnabled() {
		return toleranceEnabled;
	}

	public void setToleranceEnabled(final boolean toleranceEnabled) {
		this.toleranceEnabled = toleranceEnabled;
	}

	public double getTolerance() {
		return tolerance;
	}

	public void setTolerance(final double tolerance) {
		this.tolerance = tolerance;
	}

	public boolean isBoundsEnabled() {
		return boundsEnabled;
	}

	public void setBoundsEnabled(final boolean boundsEnabled) {
		this.boundsEnabled = boundsEnabled;
	}

	public int getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(final int lowerBound) {
		this.lowerBound = lowerBound;
	}

	public int getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(final int upperBound) {
		this.upperBound = upperBound;
	}

	public boolean isPresolveEnabled() {
		return presolveEnabled;
	}

	public void setPresolveEnabled(final boolean presolveEnabled) {
		this.presolveEnabled = presolveEnabled;
	}

	public boolean isDebugOutputEnabled() {
		return debugOutputEnabled;
	}

	public void setDebugOutputEnabled(final boolean debugOutputEnabled) {
		this.debugOutputEnabled = debugOutputEnabled;
	}

	public boolean isOutputEnabled() {
		return outputEnabled;
	}

	public void setOutputEnabled(final boolean outputEnabled) {
		this.outputEnabled = outputEnabled;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(final String outputPath) {
		this.outputPath = outputPath;
	}

}
