package org.emoflon.ilp;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;


public class Objective {
	
	// TODO: Linear OR QUADRATIC function
	private LinearFunction objective;
	private ObjectiveType type = ObjectiveType.MIN;
	private List<Constraint> constraints = new ArrayList<Constraint>();
	
	private Set<Variable<?>> variables = new HashSet<Variable<?>>();
	
	
	public ObjectiveType getType() {
		return type;
	}

	public void setType(ObjectiveType type) {
		this.type = type;
	}

	public List<Constraint> getConstraints() {
		return constraints;
	}

	public Set<Variable<?>> getVariables() {
		return variables;
	}

	public LinearFunction getObjective() {
		return objective;
	}

	public void setObjective(LinearFunction objective) {
		for (Term term : objective.terms()) {
			variables.add(term.getVar());
		}
		this.objective = objective;
	}
	
	public void setObjective(LinearFunction objective, ObjectiveType type) {
		setObjective(objective);
		setType(type);
	}
	
	public int getConstraintCount() {
		return constraints.size();
	}
	
	public int getVariableCount() {
		return variables.size();
	}

	
}