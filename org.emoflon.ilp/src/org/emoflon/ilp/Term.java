package org.emoflon.ilp;

public abstract class Term {

	private Variable<?> var1;
	private double weight;

	public Term(Variable<?> var, double weight) {
		this.var1 = var;
		this.weight = weight;
	}

	/**
	 * Returns the first variable of this term.
	 * 
	 * @return Current variable 1 of this term.
	 */
	public Variable<?> getVar1() {
		return var1;
	}

	/**
	 * Sets the first variable of this term.
	 * 
	 * @param var New variable 1 of this term.
	 */
	public void setVar1(Variable<?> var) {
		this.var1 = var;
	}

	/**
	 * Returns the weight with which the variable is multiplied in this term.
	 * 
	 * @return Current value of the weight.
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * Sets the weight with which the variable is multiplied in this term.
	 * 
	 * @param weight New value of the weight.
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}
}