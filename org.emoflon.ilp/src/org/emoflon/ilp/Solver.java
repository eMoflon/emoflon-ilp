package org.emoflon.ilp;

public abstract class Solver {
	// TODO
	public abstract void buildILPProblem(Objective objective);

	public abstract SolverOutput solve();

	public abstract void updateValuesFromSolution();

	public abstract void terminate();

	public abstract void reset();
}