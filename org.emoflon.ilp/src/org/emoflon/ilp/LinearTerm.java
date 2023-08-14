package org.emoflon.ilp;

public class LinearTerm extends Term {

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
