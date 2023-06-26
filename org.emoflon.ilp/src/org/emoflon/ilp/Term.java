package org.emoflon.ilp;

public class Term {

	private Variable<?> var;
	private double weight;
	private TermType type;

	// TODO: var^2 möglich, aber was ist mit x*y
	public Term(Variable<?> var, double weight, TermType type) {
		this.setVar(var);
		this.setWeight(weight);
		this.setType(type);
	}

	public Variable<?> getVar() {
		return var;
	}

	public void setVar(Variable<?> var) {
		this.var = var;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public TermType getType() {
		return type;
	}

	public void setType(TermType type) {
		this.type = type;
	}
}