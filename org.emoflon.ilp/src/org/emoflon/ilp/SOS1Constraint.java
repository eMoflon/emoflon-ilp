package org.emoflon.ilp;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * This class represents SOS1 Constraints. At most one variable contained in
 * this constraint is allowed to be non-zero. <br>
 * <br>
 * 
 * SOS1(var_1, var_2, ..., var_n) with weights w = [w_1, w_2, ..., w_n]
 *
 */
public class SOS1Constraint implements Constraint {

	private List<Variable<?>> variables = new ArrayList<Variable<?>>();
	private List<Double> weights = new ArrayList<Double>();
	private List<BinaryVariable> binary = new ArrayList<BinaryVariable>();
	private int bound = (int) 10E4;
	private double epsilon = 1.0E-4;

	/**
	 * A constructor for a SOS1 constraint.
	 * 
	 * @param variables List of variables that are subject to this constraint.
	 * @param weights   List of weights with which to regard the variables.
	 */
	public SOS1Constraint(List<Variable<?>> variables, double[] weights) {
		this.addVariables(variables, weights);
	}

	/**
	 * A constructor for a SOS1 constraint with equal weights of 1.
	 * 
	 * Some solver (e.g. CPLEX) do not allow two variables to have the same weight.
	 * 
	 * @param variables List of variables that are subject to this constraint.
	 */
	public SOS1Constraint(List<Variable<?>> variables) {
		double[] arr_weights = new double[variables.size()];
		Arrays.fill(arr_weights, 1);
		this.addVariables(variables, arr_weights);
	}

	/**
	 * A constructor for a SOS1 constraint.
	 */
	public SOS1Constraint() {
		double[] arr_weights = new double[0];
		this.addVariables(new ArrayList<Variable<?>>(), arr_weights);
	}

	/**
	 * A constructor for a SOS1 constraint.
	 * 
	 * @param variables List of variables that are subject to this constraint.
	 * @param weights   List of weights with which to regard the variables.
	 * @param bound     Value for the bound that is used for conversion into normal
	 *                  constraints.
	 */
	public SOS1Constraint(List<Variable<?>> variables, double[] weights, int bound) {
		double[] arr_weights = new double[variables.size()];
		Arrays.fill(arr_weights, 1);
		this.addVariables(variables, arr_weights);
		this.setBound(bound);
	}

	// If no values for the weights are given, all of the weights are set to 1
	/**
	 * A constructor for a SOS1 constraint with equal weights of 1.
	 * 
	 * Some solver (e.g. CPLEX) do not allow two variables to have the same weight.
	 * 
	 * @param variables List of variables that are subject to this constraint.
	 * @param bound     Value for the bound that is used for conversion into normal
	 *                  constraints.
	 */
	public SOS1Constraint(List<Variable<?>> variables, int bound) {
		double[] arr_weights = new double[variables.size()];
		Arrays.fill(arr_weights, 1);
		this.addVariables(variables, arr_weights);
		this.setBound(bound);
	}

	/**
	 * A constructor for a SOS1 constraint.
	 * 
	 * @param bound Value for the bound that is used for conversion into normal
	 *              constraints.
	 */
	public SOS1Constraint(int bound) {
		double[] arr_weights = new double[0];
		this.addVariables(new ArrayList<Variable<?>>(), arr_weights);
		this.setBound(bound);
	}

	/**
	 * Returns a list of the variables that are subject to this constraint.
	 * 
	 * @return List of variables.
	 */
	public List<Variable<?>> getVariables() {
		return variables;
	}

	/**
	 * Adds new variables that are subject to this constraint and their respective
	 * weights. The list of variables and the Array of weights have to be the same
	 * size.
	 * 
	 * @param variables List of the new variables.
	 * @param weights   Array of weights to be set for the new variables.
	 */
	public void addVariables(List<Variable<?>> variables, double[] weights) {
		if (variables.size() != weights.length) {
			throw new IllegalArgumentException("Every variable has to have a weight!");
		}
		this.variables.addAll(variables);
		this.weights.addAll(Arrays.stream(weights).mapToObj(Double::valueOf).toList());
	}

	/**
	 * Adds new variables that are subject to this constraint. <br>
	 * Important: All variables are assigned a weight of 1.0. The weight cannot be
	 * changed afterwards!
	 * 
	 * @param variables List of the new variables.
	 */
	public void addVariables(List<Variable<?>> variables) {
		this.variables.addAll(variables);
		variables.forEach(it -> this.weights.add(1.0));
	}

	/**
	 * Adds a new variable and its weight to this constraint.
	 * 
	 * @param variable The new variable to be added.
	 * @param weight   The weights to be set for the variable.
	 */
	public void addVariable(Variable<?> variable, double weight) {
		this.variables.add(variable);
		this.weights.add(weight);
	}

	/**
	 * Adds a new variable and its weight to this constraint. <br>
	 * Important: The variable is assigned a weight of 1.0. This weight cannot be
	 * changed afterwards!
	 * 
	 * @param variable The new variable to be added.
	 */
	public void addVariable(Variable<?> variable) {
		this.variables.add(variable);
		this.weights.add(1.0);
	}

	/**
	 * Returns the weights of the current variables of this constraint.
	 * 
	 * @return Array of weights.
	 */
	public double[] getWeights() {
		return weights.stream().mapToDouble(Double::doubleValue).toArray();
	}

	/**
	 * Returns the bound used for converting the SOS1 constraint into linear
	 * constraints.
	 * 
	 * @return Bound used for conversion.
	 */
	public int getBound() {
		return bound;
	}

	/**
	 * Sets the bound used for converting the SOS1 constraint into linear
	 * constraints. This should be a large number.
	 * 
	 * @param bound Large integer bound used for conversion.
	 */
	public void setBound(int bound) {
		this.bound = bound;
	}

	/**
	 * Sets the epsilon value used for converting the SOS1 constraint into linear
	 * constraints. This should be a small number.
	 * 
	 * @param epsilon Small double value used for conversion.
	 */
	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	/**
	 * Returns the current epsilon value.
	 * 
	 * @return Current epsilon (small double value) used for conversion.
	 */
	public double getEpsilon() {
		return this.epsilon;
	}

	/**
	 * Converts the SOS1 constraint into multiple linear constraints.
	 * 
	 * @return List of constraints to substitute this SOS1 constraint.
	 * @see LinearConstraint
	 */
	public List<LinearConstraint> convert() {
		List<LinearConstraint> substitution = new ArrayList<LinearConstraint>();
		List<Term> binaryTerms = new ArrayList<Term>();
		// for every variable v_i in this sos constraint there is a corresponding binary
		// variable s_i
		for (Variable<?> var : this.variables) {
			BinaryVariable binVar = new BinaryVariable("sos_binary_" + var.getName());
			this.binary.add(binVar);
			binaryTerms.add(new LinearTerm(binVar, 1));

			// match variable to take non-zero value
			// v_i <= c * s_i -> v_i - c * s_i <= 0
			LinearConstraint linRight = new LinearConstraint(Operator.LESS_OR_EQUAL, 0.0);
			linRight.addTerm(var, 1.0);
			linRight.addTerm(binVar, -bound);
			substitution.add(linRight);

			// v_i >= -c * s_i -> v_i + c * s_i >= 0
			LinearConstraint linLeft = new LinearConstraint(Operator.GREATER_OR_EQUAL, 0.0);
			linLeft.addTerm(binVar, bound);
			linLeft.addTerm(var, 1);
			substitution.add(linLeft);
		}
		// at most one binary variable s_i is non-zero -> the sum of all binary
		// variables is <= 1
		substitution.add(new LinearConstraint(binaryTerms, Operator.LESS_OR_EQUAL, 1.0));

		return substitution;
	}

	/*
	 * // un-comment for debugging purposes public String toString() { StringBuilder
	 * sb = new StringBuilder(); sb.append("SOS1("); int i = 1; for (Variable<?> var
	 * : variables) { sb.append(var.getName()); if (i++ != this.variables.size()) {
	 * sb.append(", "); } }
	 * 
	 * sb.append(")"); return sb.toString(); }
	 */
}