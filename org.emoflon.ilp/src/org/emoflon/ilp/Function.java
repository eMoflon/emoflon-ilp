package org.emoflon.ilp;

import java.util.List;

/**
 * This abstract class represents functions that can be used in the objective of
 * the optimization problem. <br>
 * <br>
 * 
 * Sum(terms) + Sum(constants) + Sum(nestedFunctions)
 *
 */
public abstract class Function {

	protected List<Term> terms;
	protected List<Constant> constantTerms;
	protected List<WeightedFunction> nestedFunctions;

	/**
	 * Adds a term to the function.
	 * 
	 * @param term Term to be added to this function.
	 */
	public void addTerm(Term term) {
		this.terms.add(term);
	}

	/**
	 * Adds a linear term to the function.
	 * 
	 * @param var    Variable to be added in the term.
	 * @param weight Weight of the term.
	 */
	public void addTerm(Variable<?> var, double weight) {
		this.terms.add(new LinearTerm(var, weight));
	}

	/**
	 * Adds a constant to the function.
	 * 
	 * @param constant Constant to be added to this function.
	 */
	public void addConstant(Constant constant) {
		this.constantTerms.add(constant);
	}

	/**
	 * Adds a constant value to the function.
	 * 
	 * @param weight Value of the constant to be added to this function.
	 */
	public void addConstant(double weight) {
		this.constantTerms.add(new Constant(weight));
	}

	/**
	 * Adds a list of constants to the constant terms of this function.
	 * 
	 * @param constants List of constants to be added to this function.
	 */
	public void addConstants(List<Constant> constants) {
		this.constantTerms.addAll(constants);
	}

	/**
	 * Returns all current terms of the function.
	 * 
	 * @return List of terms of this function.
	 */
	public List<Term> getTerms() {
		return terms;
	}

	/**
	 * Returns all constants of the function.
	 * 
	 * @return List of constants of this function.
	 */
	public List<Constant> getConstants() {
		return constantTerms;
	}

	/**
	 * Adds a weighted function as a nested function to this function.
	 * 
	 * @param func Weighted function to be added to this function.
	 * @see WeightedFunction
	 */
	public void addNestedFunction(WeightedFunction func) {
		this.nestedFunctions.add(func);
	}

	/**
	 * Adds a function and a weight as a nested function to this function.
	 * 
	 * @param func   Function to be added.
	 * @param weight Weight of the new weighted function.
	 * @see Function
	 */
	public void addNestedFunction(Function func, double weight) {
		this.nestedFunctions.add(new WeightedFunction(func, weight));
	}

	/**
	 * Returns all nested functions of this function.
	 * 
	 * @return List of nested weighted functions.
	 */
	public List<WeightedFunction> getNestedFunctions() {
		return nestedFunctions;
	}

	/**
	 * Expands (i.e. multiplies out) all nested functions currently contained in
	 * this function.
	 * 
	 * @return Expanded function, without any nested functions left.
	 */
	public abstract Function expand();

}
