package org.emoflon.ilp;

import java.util.List;
import java.util.ArrayList;

public class OrConstraint implements GeneralConstraint {

	private List<BinaryVariable> variables;
	private BinaryVariable result;

	public OrConstraint(List<Variable<?>> variables, BinaryVariable result) {
		this.setVariables(variables);
		this.setResult(result);
	}

	public OrConstraint(BinaryVariable result) {
		this.variables = new ArrayList<BinaryVariable>();
		this.setResult(result);
	}
	
	@Override
	public List<BinaryVariable> getVariables() {
		return variables;
	}

	@Override
	public void setVariables(List<Variable<?>> variables) {
		for(Variable<?> var : variables) {
			variables.add(new BinaryVariable(var.getName()));
		}
	}

	@Override
	public BinaryVariable getResult() {
		return result;
	}

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
		if((double) res.getValue() > 1 || (double) res.getValue() < 0) {
			throw new IllegalArgumentException ("The result of an Or Constraint has to be binary.");
		} else {
			result = (BinaryVariable) res;
		}
	}
}