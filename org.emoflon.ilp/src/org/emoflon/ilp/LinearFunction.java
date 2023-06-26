package org.emoflon.ilp;

import java.util.List;
import java.util.ArrayList;


public record LinearFunction(List<Term> terms, List<Constant> constantTerms, List<WeightedLinearFunction> nestedFunctions) {

	public LinearFunction() {
		this(new ArrayList<Term>(), new ArrayList<Constant>());
	}
	
	public LinearFunction(List<Term> terms, List<Constant> constantTerms) {
		this(terms, constantTerms, new ArrayList<WeightedLinearFunction>());
	}

	public void addTerm(Term term) {
		this.terms.add(term);
	}

	public void addTerm(Variable<?> var, double weight, TermType type) {
		this.terms.add(new Term(var, weight, type));
	}
	
	public void addConstant(Constant constant) {
		this.constantTerms.add(constant);
	}
	
	public void addNestedFunction(WeightedLinearFunction func) {
		this.nestedFunctions.add(func);
	}
	
	public void addNestedFunction(LinearFunction func, double weight) {
		this.nestedFunctions.add(new WeightedLinearFunction(func, weight));
	}
	
	// TODO: hier ausmultiplizieren? "expand"
}