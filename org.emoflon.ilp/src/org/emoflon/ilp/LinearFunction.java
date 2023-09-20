package org.emoflon.ilp;

import java.util.List;
import java.util.ArrayList;

/**
 * This class represents linear functions. A linear function can consist of
 * multiple terms (weight * variable), constants and nested weighted linear
 * functions (weight * function). <br>
 * <br>
 * 
 * function = term1 + term2 + ... + constant1 + constant2 + ... + w1 * func1 +
 * w2 * func2 + ...
 *
 */
public class LinearFunction extends Function {

	/**
	 * A constructor for a linear function.
	 * 
	 * function = term1 + term2 + ... + constant1 + constant2 + ... + w1 * func1 +
	 * w2 * func2 + ...
	 * 
	 * @param terms           A list of linear terms.
	 * @param constantTerms   A list of constants.
	 * @param nestedFunctions A list of nested weighted linear functions.
	 * @see LinearTerm
	 * @see Constant
	 * @see WeightedFunction
	 */
	public LinearFunction(List<Term> terms, List<Constant> constantTerms, List<WeightedFunction> nestedFunctions) {
		if (terms.stream().anyMatch(QuadraticTerm.class::isInstance)) {
			throw new IllegalArgumentException("A linear function is not allowed to contain any quadratic terms!");
		}
		this.terms = terms;
		this.constantTerms = constantTerms;
		if (nestedFunctions.stream().anyMatch(nested -> !(nested.function() instanceof LinearFunction))) {
			throw new IllegalArgumentException(
					"A linear function is not allowed to contain any quadratic nested functions!");
		}
		this.nestedFunctions = nestedFunctions;
	}

	/**
	 * A constructor for a completely empty linear function.
	 */
	public LinearFunction() {
		this.terms = new ArrayList<Term>();
		this.constantTerms = new ArrayList<Constant>();
		this.nestedFunctions = new ArrayList<WeightedFunction>();
	}

	/**
	 * A constructor for a linear function.
	 * 
	 * @param terms         A list of linear terms.
	 * @param constantTerms A list of constants.
	 * @see LinearTerm
	 * @see Constant
	 */
	public LinearFunction(List<Term> terms, List<Constant> constantTerms) {
		this.terms = terms;
		this.constantTerms = constantTerms;
		this.nestedFunctions = new ArrayList<WeightedFunction>();
	}

	@Override
	public void addNestedFunction(WeightedFunction func) {
		if (func.function() instanceof LinearFunction) {
			this.nestedFunctions.add(func);
		} else {
			throw new IllegalArgumentException(
					"A linear function is not allowed to contain any quadratic nested functions!");
		}
	}

	@Override
	public void addNestedFunction(Function func, double weight) {
		if (func instanceof LinearFunction) {
			this.nestedFunctions.add(new WeightedFunction(func, weight));
		} else {
			throw new IllegalArgumentException(
					"A linear function is not allowed to contain any quadratic nested functions!");
		}
	}

	@Override
	public Function expand() {
		if (this.nestedFunctions.isEmpty()) {
			// end of nesting, deepest level
			return this;
		} else {
			LinearFunction expanded = new LinearFunction(this.terms, this.constantTerms);

			for (WeightedFunction nested : this.nestedFunctions) {
				double nestedWeight = nested.weight();
				Function func = ((LinearFunction) nested.function()).expand();

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

	public String toString() {
		StringBuilder sb = new StringBuilder();
		int i = 1;
		for (Term term : this.terms) {
			sb.append(term.getWeight());
			sb.append(" * ");
			sb.append(term.getVar1().getName());
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