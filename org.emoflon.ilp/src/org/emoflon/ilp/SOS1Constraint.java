package org.emoflon.ilp;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class SOS1Constraint {

	private List<Variable<?>> variables;
	private double[] weights;
	private List<BinaryVariable> binary;
	private int bound = 10_000;

	public SOS1Constraint(List<Variable<?>> variables, double[] weights) {
		this.setVariables(variables);
		this.setWeights(weights);
	}

	// If no values for the weights are given, all of the weights are set to 1
	public SOS1Constraint(List<Variable<?>> variables) {
		this.setVariables(variables);
		Arrays.fill(this.weights, 1);
	}

	public SOS1Constraint() {
		this.setVariables(new ArrayList<Variable<?>>());
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
		Arrays.fill(this.weights, 1);
		this.setBound(bound);
	}

	public SOS1Constraint(int bound) {
		this.setVariables(new ArrayList<Variable<?>>());
		Arrays.fill(this.weights, 1);
		this.setBound(bound);
	}

	public List<Variable<?>> getVariables() {
		return variables;
	}

	public void setVariables(List<Variable<?>> variables) {
		this.variables = variables;
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

	// SOS1: at most one of the variables has a non-zero value
	public List<LinearConstraint> convert() {
		List<LinearConstraint> substitution = new ArrayList<LinearConstraint>();
		List<Term> binaryTerms = new ArrayList<Term>();
		for (Variable<?> var : this.variables) {
			BinaryVariable binVar = new BinaryVariable("sos_binary_" + var.getName());
			this.binary.add(binVar);
			binaryTerms.add(new LinearTerm(binVar, 1));
		}
		// at most one binary variable is non-zero
		substitution.add(new LinearConstraint(binaryTerms, Operator.LESS_OR_EQUAL, 1.0));

		// match variable to take non-zero value
		// TODO
		int i = 0;
		for (Variable<?> var : this.variables) {
			// -b_i * K <= a_i
			LinearConstraint linLeft = new LinearConstraint(Operator.LESS_OR_EQUAL, 0.0);
			linLeft.addTerm(this.binary.get(i), -bound);
			linLeft.addTerm(var, -1);
			substitution.add(linLeft);

			// a_i <= b_i * K
			LinearConstraint linRight = new LinearConstraint(Operator.LESS_OR_EQUAL, 0.0);
			linRight.addTerm(var, 1);
			linRight.addTerm(this.binary.get(i), -bound);
			substitution.add(linRight);
		}

		return substitution;
	}
}