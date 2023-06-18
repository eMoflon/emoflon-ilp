package org.emoflon.ilp;

public class SolverOutput {
	// TODO

	private SolverStatus status = null;
	private double objVal = -1;
	private int solCount = -1;

	public SolverOutput(SolverStatus status, double objVal, int solCount) {
		this.setStatus(status);
		this.setObjVal(objVal);
		this.setSolCount(solCount);
	}

	public SolverStatus getStatus() {
		return status;
	}

	public void setStatus(SolverStatus status) {
		this.status = status;
	}

	public double getObjVal() {
		return objVal;
	}

	public void setObjVal(double objVal) {
		this.objVal = objVal;
	}

	public int getSolCount() {
		return solCount;
	}

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