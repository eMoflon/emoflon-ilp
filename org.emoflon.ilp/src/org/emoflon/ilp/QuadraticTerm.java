package org.emoflon.ilp;

/**
 * This class represents quadratic terms.
 * 
 * quadratic term = weight * variable1 * variable2
 *
 */
public class QuadraticTerm extends Term {

	private Variable<?> var2 = null;

	/**
	 * The constructor for quadratic terms.
	 * 
	 * @param var1   The first variable that is part of this term.
	 * @param var2   The second variable that is part of this term (can be the same
	 *               as var1).
	 * @param weight The weight with which the variables are multiplied.
	 */
	public QuadraticTerm(Variable<?> var1, Variable<?> var2, double weight) {
		super(var1, weight);
		this.var2 = var2;
	}

	/**
	 * Returns the second variable of this term.
	 * 
	 * @return Current variable 2 of this term.
	 */
	public Variable<?> getVar2() {
		return var2;
	}

	/**
	 * Sets the second variable of this term.
	 * 
	 * @param var New variable 2 of this term.
	 */
	public void setVar2(Variable<?> var) {
		this.var2 = var;
	}

	// @Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getWeight());
		sb.append("*");
		sb.append(this.getVar1().getName());
		sb.append("*");
		sb.append(this.getVar2().getName());
		return sb.toString();
	}

}
