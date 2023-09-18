package org.emoflon.ilp;


/**
 * This enum is used to describe the status of the solver.
 * 
 * UNBOUNDED: Problem was proven to be unbounded. (There still might be a feasible solution.)
 * INF_OR_UNBD: Problem was proven to be either infeasible or unbounded. 
 * INFEASIBLE: Problem was proven to be infeasible.
 * OPTIMAL: Problem was solved to optimality and an optimal solution is available.
 * TIME_OUT: Time limit specified in Solver Config was exceeded, optimization was terminated.
 * FEASIBLE: A feasible solution was found.
 *
 */
public enum SolverStatus {
	UNBOUNDED, INF_OR_UNBD, INFEASIBLE, OPTIMAL, TIME_OUT, FEASIBLE
}