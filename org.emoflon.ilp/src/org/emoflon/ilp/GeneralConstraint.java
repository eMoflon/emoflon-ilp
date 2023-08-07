package org.emoflon.ilp;

import java.util.List;

public interface GeneralConstraint extends Constraint {
	public List<? extends Variable<?>> getVariables();

	public void setVariables(List<Variable<?>> variables);

	public void addVariable(Variable<?> var);

	public Variable<?> getResult();

	public void setResult(Variable<?> res);

	public ConstraintType getType();
}
