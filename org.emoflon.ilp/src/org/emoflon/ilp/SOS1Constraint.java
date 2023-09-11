package org.emoflon.ilp;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class SOS1Constraint implements Constraint {

	private List<Variable<?>> variables = new ArrayList<Variable<?>>();
	private double[] weights;
	private List<BinaryVariable> binary = new ArrayList<BinaryVariable>();
	private int bound = (int) 10E4;
	private double epsilon = 1.0E-4;

	public SOS1Constraint(List<Variable<?>> variables, double[] weights) {
		this.setVariables(variables);
		this.setWeights(weights);
	}

	// If no values for the weights are given, all of the weights are set to 1
	public SOS1Constraint(List<Variable<?>> variables) {
		this.setVariables(variables);
		this.weights = new double[variables.size()];
		Arrays.fill(this.weights, 1);
	}

	public SOS1Constraint() {
		this.setVariables(new ArrayList<Variable<?>>());
		this.weights = new double[variables.size()];
		Arrays.fill(this.weights, 1);
	}

	public SOS1Constraint(List<Variable<?>> variables, double[] weights, int bound) {
		this.setVariables(variables);
		this.setWeights(weights);
		this.setBound(bound);
	}

	// If no values for the weights are given, all of the weights are set to 1
	public SOS1Constraint(List<Variable<?>> variables, int bound) {
		this.setVariables(variables);
		this.weights = new double[variables.size()];
		Arrays.fill(this.weights, 1);
		this.setBound(bound);
	}

	public SOS1Constraint(int bound) {
		this.setVariables(new ArrayList<Variable<?>>());
		this.weights = new double[variables.size()];
		Arrays.fill(this.weights, 1);
		this.setBound(bound);
	}

	public List<Variable<?>> getVariables() {
		return variables;
	}

	public void setVariables(List<Variable<?>> variables) {
		this.variables.addAll(variables);
	}

	/*
	 * weights nicht als array, wenn das hier implementiert werden soll public void
	 * addVariable(Variable<?> variable, double weight) {
	 * this.variables.add(variable); this.addWeight(weight); }
	 */

	public double[] getWeights() {
		return weights;
	}

	public void setWeights(double[] weights) {
		this.weights = weights;
	}

	public int getBound() {
		return bound;
	}

	public void setBound(int bound) {
		this.bound = bound;
	}
	
	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}
	
	public double getEpsilon() {
		return this.epsilon;
	}

	// SOS1: at most one of the variables has a non-zero value
	public List<LinearConstraint> convert() {
		List<LinearConstraint> substitution = new ArrayList<LinearConstraint>();
		List<Term> binaryTerms = new ArrayList<Term>();
		// for every variable v_i in this sos constraint there is a corresponding binary
		// variable s_i
		for (Variable<?> var : this.variables) {
			BinaryVariable binVar = new BinaryVariable("sos_binary_" + var.getName());
			this.binary.add(binVar);
			binaryTerms.add(new LinearTerm(binVar, 1));
		}
		// at most one binary variable s_i is non-zero -> the sum of all binary
		// variables is <= 1
		substitution.add(new LinearConstraint(binaryTerms, Operator.LESS_OR_EQUAL, 1.0, this.epsilon));

		// match variable to take non-zero value
		int i = 0;
		for (Variable<?> var : this.variables) {
			// v_i <= c * s_i
			LinearConstraint linRight = new LinearConstraint(Operator.LESS_OR_EQUAL, 0.0, this.epsilon);
			linRight.addTerm(var, 1.0);
			linRight.addTerm(this.binary.get(i), -bound);
			substitution.add(linRight);

			// v_i >= -c * s_i
			LinearConstraint linLeft = new LinearConstraint(Operator.GREATER_OR_EQUAL, 0.0, this.epsilon);
			linLeft.addTerm(this.binary.get(i), bound);
			linLeft.addTerm(var, 1.0);
			substitution.add(linLeft);

			i++;
		}

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