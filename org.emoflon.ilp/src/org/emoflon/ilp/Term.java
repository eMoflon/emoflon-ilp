package org.emoflon.ilp;

public abstract class Term {

	private Variable<?> var1;
	private double weight;

	public Term(Variable<?> var, double weight) {
		this.var1 = var;
		this.weight = weight;
	}

	public Term(Variable<?> var1, Variable<?> var2, double weight) {
		this.var1 = var1;
		this.weight = weight;
	}

	public Variable<?> getVar1() {
		return var1;
	}

	public void setVar1(Variable<?> var) {
		this.var1 = var;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
}