package org.emoflon.ilp;

import java.util.List;
import java.util.ArrayList;

public class LinearFunction extends Function {

	public LinearFunction(List<Term> terms, List<Constant> constantTerms, List<WeightedFunction> nestedFunctions) {
		if (terms.stream().anyMatch(QuadraticTerm.class::isInstance)) {
			throw new IllegalArgumentException("A linear function is not allowed to contain quadratic terms!");
		}
		this.terms = terms;
		this.constantTerms = constantTerms;
		this.nestedFunctions = nestedFunctions;
	}

	public LinearFunction() {
		this.terms = new ArrayList<Term>();
		this.constantTerms = new ArrayList<Constant>();
		this.nestedFunctions = new ArrayList<WeightedFunction>();
	}

	public LinearFunction(List<Term> terms, List<Constant> constantTerms) {
		this.terms = terms;
		this.constantTerms = constantTerms;
		this.nestedFunctions = new ArrayList<WeightedFunction>();
	}

	@Override
	public Function expand() {
		if (this.nestedFunctions.isEmpty()) {
			// end of nesting, deepest level
			return this;
		} else {

			Function expanded;
			if (this.nestedFunctions.stream().anyMatch(nested -> nested.function() instanceof QuadraticFunction)) {
				expanded = new QuadraticFunction();
			} else {
				expanded = new LinearFunction();
			}

			for (WeightedFunction nested : this.nestedFunctions) {
				double nestedWeight = nested.weight();

				Function func;
				if (nested.function() instanceof LinearFunction) {
					func = ((LinearFunction) nested.function()).expand();
				} else {
					func = ((QuadraticFunction) nested.function()).expand();
				}

				// add constants multiplied with weight
				for (Constant cons : func.constantTerms) {
					expanded.addConstant(cons.weight() * nestedWeight);
				}
				// add terms multiplied with weight
				for (Term term : func.terms) {
					expanded.addTerm(term.getVar1(), term.getWeight() * nestedWeight);
				}
			}
			return expanded;
		}
	}
}