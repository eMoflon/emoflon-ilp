package org.emoflon.ilp;

public interface Solver {

	public abstract void buildILPProblem(Objective objective);

	public abstract SolverOutput solve();

	public abstract Objective updateValuesFromSolution();

	public abstract void terminate();

	public abstract void reset();

}
