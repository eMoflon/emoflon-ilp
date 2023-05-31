package org.emoflon.ilp;

public abstract class Solver {
	// TODO
	public abstract void buildILPProbelm();
	
	public abstract SolverOutput solve();
	
	public abstract void updateValuesFromSolution();
	
	public abstract void terminate();
	
	public abstract void reset();
}