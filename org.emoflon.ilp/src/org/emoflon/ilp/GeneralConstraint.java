package org.emoflon.ilp;

import java.util.List;

// Class that is currently only used for Gurobi OrConstraints
// Could be extended for other general constraints (min, max, ...)
public interface GeneralConstraint extends Constraint {
	/**
	 * Returns all variables that are part of the constraint.
	 * 
	 * @return List of variables.
	 */
	public List<? extends Variable<?>> getVariables();

	/**
	 * Sets the variables for this constraint.
	 * 
	 * @param variables List of variables.
	 */
	public void setVariables(List<Variable<?>> variables);

	/**
	 * Adds one variable to the list of variables.
	 * 
	 * @param var New variable to be added.
	 */
	public void addVariable(Variable<?> var);

	/**
	 * Returns the result variable of the constraint.
	 * 
	 * @return Result variable.
	 */
	public Variable<?> getResult();

	/**
	 * Sets the result variable of the constraint.
	 * 
	 * @param res New result variable.
	 */
	public void setResult(Variable<?> res);

	/**
	 * Returns the type of the constraint.
	 * 
	 * @return Type of the constraint.
	 */
	public ConstraintType getType();
}
