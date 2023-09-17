package org.emoflon.ilp;

import java.util.List;
import java.util.ArrayList;

// Currently only used for Gurobi OrConstraints
// b_result = or(var1, var2, ..., var_n)
public class OrVarsConstraint implements GeneralConstraint {

	private List<BinaryVariable> variables;
	private BinaryVariable result;

	/**
	 * A constructor for Gurobi Or Constraints of the form binary_result = or(var_1,
	 * var_2, ..., var_n)
	 * 
	 * @param variables The variables to be compared in this constraint.
	 * @param result    The variable depicting the result.
	 */
	public OrVarsConstraint(List<Variable<?>> variables, BinaryVariable result) {
		this.setVariables(variables);
		this.setResult(result);
	}

	/**
	 * A constructor for Gurobi Or Constraints of the form binary_result = or(var_1,
	 * var_2, ..., var_n)
	 * 
	 * @param result The variable depicting the result.
	 */
	public OrVarsConstraint(BinaryVariable result) {
		this.variables = new ArrayList<BinaryVariable>();
		this.setResult(result);
	}

	@Override
	public List<BinaryVariable> getVariables() {
		return variables;
	}

	@Override
	public void setVariables(List<Variable<?>> variables) {
		for (Variable<?> var : variables) {
			variables.add(new BinaryVariable(var.getName()));
		}
	}

	@Override
	public BinaryVariable getResult() {
		return result;
	}

	/**
	 * Sets the result variable of the constraint.
	 * 
	 * @param res New result variable.
	 */
	public void setResult(BinaryVariable res) {
		this.result = res;
	}

	@Override
	public ConstraintType getType() {
		return ConstraintType.OR;
	}

	@Override
	public void addVariable(Variable<?> var) {
		variables.add(new BinaryVariable(var.getName()));
	}

	@Override
	public void setResult(Variable<?> res) {
		if (res.getValue().doubleValue() != 1 || res.getValue().doubleValue() != 0) {
			throw new IllegalArgumentException("The result of an Or Constraint has to be binary.");
		} else {
			result = (BinaryVariable) res;
		}
	}

	// TODO: Translate to normal constraint
	public List<LinearConstraint> convert() {
		throw new Error("Not yet implemented!");
	}
}