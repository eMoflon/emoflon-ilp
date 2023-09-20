package org.emoflon.ilp;

/**
 * This class represents linear terms. <br>
 * <br>
 * 
 * linear term = weight * variable
 *
 */
public class LinearTerm extends Term {

	/**
	 * The constructor for linear terms.
	 * 
	 * @param var    The variable that is part of this term.
	 * @param weight The weight with which the variable is multiplied.
	 */
	public LinearTerm(Variable<?> var, double weight) {
		super(var, weight);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getWeight());
		sb.append("*");
		sb.append(getVar1().getName());
		return sb.toString();
	}

}
