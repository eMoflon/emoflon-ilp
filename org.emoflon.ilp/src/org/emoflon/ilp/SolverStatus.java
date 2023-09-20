package org.emoflon.ilp;

/**
 * This enum is used to describe the status of the solver. <br>
 * <br>
 * 
 * UNBOUNDED: Problem was proven to be unbounded. (There still might be a
 * feasible solution.) <br>
 * INF_OR_UNBD: Problem was proven to be either infeasible or unbounded. <br>
 * INFEASIBLE: Problem was proven to be infeasible. <br>
 * OPTIMAL: Problem was solved to optimality and an optimal solution is
 * available. <br>
 * TIME_OUT: Time limit specified in Solver Config was exceeded, optimization
 * was terminated. <br>
 * FEASIBLE: A feasible solution was found. <br>
 *
 */
public enum SolverStatus {
	UNBOUNDED, INF_OR_UNBD, INFEASIBLE, OPTIMAL, TIME_OUT, FEASIBLE
}