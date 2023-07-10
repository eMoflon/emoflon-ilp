package org.emoflon.ilp;

import java.util.List;

public abstract class Function {

	protected List<Term> terms;
	protected List<Constant> constantTerms;
	protected List<WeightedFunction> nestedFunctions;

	public void addTerm(Term term) {
		this.terms.add(term);
	}

	public void addTerm(Variable<?> var, double weight) {
		this.terms.add(new LinearTerm(var, weight));
	}

	public void addConstant(Constant constant) {
		this.constantTerms.add(constant);
	}

	public void addConstant(double weight) {
		this.constantTerms.add(new Constant(weight));
	}

	public void addConstants(List<Constant> constants) {
		this.constantTerms.addAll(constants);
	}

	public List<Term> getTerms() {
		return terms;
	}

	public List<Constant> getConstants() {
		return constantTerms;
	}

	public void addNestedFunction(WeightedFunction func) {
		this.nestedFunctions.add(func);
	}

	public void addNestedFunction(Function func, double weight) {
		this.nestedFunctions.add(new WeightedFunction(func, weight));
	}

	public List<WeightedFunction> getNestedFunctions() {
		return nestedFunctions;
	}

	public abstract Function expand();

}
