package org.emoflon.ilp;

import java.util.List;

// Class that is currently only used for Gurobi OrConstraints
// Could be extended for other general constraints (min, max, ...)
public abstract class GeneralConstraint extends Constraint {
	/**
	 * Returns all variables that are part of the constraint.
	 * 
	 * @return List of variables.
	 */
	public abstract List<? extends Variable<?>> getVariables();

	/**
	 * Sets the variables for this constraint.
	 * 
	 * @param variables List of variables.
	 */
	public abstract void setVariables(List<Variable<?>> variables);

	/**
	 * Adds one variable to the list of variables.
	 * 
	 * @param var New variable to be added.
	 */
	public abstract void addVariable(Variable<?> var);

	/**
	 * Returns the result variable of the constraint.
	 * 
	 * @return Result variable.
	 */
	public abstract Variable<?> getResult();

	/**
	 * Sets the result variable of the constraint.
	 * 
	 * @param res New result variable.
	 */
	public abstract void setResult(Variable<?> res);

	/**
	 * Returns the type of the constraint.
	 * 
	 * @return Type of the constraint.
	 */
	public abstract ConstraintType getType();
}
