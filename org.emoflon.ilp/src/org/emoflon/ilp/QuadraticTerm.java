package org.emoflon.ilp;

public class QuadraticTerm extends Term {

	private Variable<?> var2 = null;

	public QuadraticTerm(Variable<?> var1, Variable<?> var2, double weight) {
		super(var1, weight);
		this.var2 = var2;
	}

	public Variable<?> getVar2() {
		return var2;
	}

	public void setVar2(Variable<?> var) {
		this.var2 = var;
	}

}
