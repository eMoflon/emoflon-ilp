package org.emoflon.ilp;

import java.util.ArrayList;
import java.util.List;

public class QuadraticFunction extends Function {

	public QuadraticFunction(List<Term> terms, List<Constant> constantTerms, List<WeightedFunction> nestedFunctions) {
		this.terms = terms;
		this.constantTerms = constantTerms;
		this.nestedFunctions = nestedFunctions;
	}

	public QuadraticFunction() {
		this.terms = new ArrayList<Term>();
		this.constantTerms = new ArrayList<Constant>();
		this.nestedFunctions = new ArrayList<WeightedFunction>();
	}

	public QuadraticFunction(List<Term> terms, List<Constant> constantTerms) {
		this.terms = terms;
		this.constantTerms = constantTerms;
		this.nestedFunctions = new ArrayList<WeightedFunction>();
	}

	public void addTerm(Variable<?> var1, Variable<?> var2, double weight) {
		this.terms.add(new QuadraticTerm(var1, var2, weight));
	}

	@Override
	public QuadraticFunction expand() {
		if (this.nestedFunctions.isEmpty()) {
			// end of nesting, deepest level
			return this;
		} else {
			QuadraticFunction expanded = new QuadraticFunction(this.terms, this.constantTerms);
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
					if (term instanceof LinearTerm) {
						expanded.addTerm(term.getVar1(), term.getWeight() * nestedWeight);
					} else {
						expanded.addTerm(term.getVar1(), ((QuadraticTerm) term).getVar2(),
								term.getWeight() * nestedWeight);
					}
				}
			}
			return expanded;
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		int i = 1;
		for (Term term : this.terms) {
			sb.append(term.toString());
			if (i++ != this.terms.size()) {
				sb.append(" + ");
			}
		}

		for (Constant constant : this.constantTerms) {
			sb.append(" + ");
			sb.append(constant.weight());
		}

		for (WeightedFunction func : this.nestedFunctions) {
			sb.append(" + ");
			sb.append(func.toString());
		}

		return sb.toString();
	}
}
