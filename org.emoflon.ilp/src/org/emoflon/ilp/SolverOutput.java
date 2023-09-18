package org.emoflon.ilp;

/**
 * This class represents the solver output after the optimization of the
 * problem.
 *
 */
public class SolverOutput {
	// TODO

	private SolverStatus status = null;
	private double objVal = -1;
	private int solCount = -1;

	/**
	 * The constructor for the solver output.
	 * 
	 * @param status   The status of the solver.
	 * @param objVal   The value of the objective.
	 * @param solCount The number of solutions found by the solver.
	 */
	public SolverOutput(SolverStatus status, double objVal, int solCount) {
		this.setStatus(status);
		this.setObjVal(objVal);
		this.setSolCount(solCount);
	}

	/**
	 * Returns the status of the solver.
	 * 
	 * @return The current status of the solver.
	 */
	public SolverStatus getStatus() {
		return status;
	}

	/**
	 * Sets the status of the solver.
	 * 
	 * @param status The new status of the solver.
	 */
	public void setStatus(SolverStatus status) {
		this.status = status;
	}

	/**
	 * Returns the objective value of the problem.
	 * 
	 * @return The current value of the objective.
	 */
	public double getObjVal() {
		return objVal;
	}

	/**
	 * Sets the objective value of the problem.
	 * 
	 * @param objVal The new value of the objective.
	 */
	public void setObjVal(double objVal) {
		this.objVal = objVal;
	}

	/**
	 * Returns the number of solutions found by the solver.
	 * 
	 * @return Current number of solutions found.
	 */
	public int getSolCount() {
		return solCount;
	}

	/**
	 * Sets the number of solutions found by the solver.
	 * 
	 * @param solCount New number of solutions found.
	 */
	public void setSolCount(int solCount) {
		this.solCount = solCount;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ILP problem status <");
		sb.append(status.name());
		sb.append(">\n Objective function value:  ");
		sb.append(objVal);
		return sb.toString();
	}
}