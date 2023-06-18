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

	public Objective() {
		super();
	}

	public ObjectiveType getType() {
		return type;
	}

	public void setType(ObjectiveType type) {
		this.type = type;
	}

	public void setConstraints(List<Constraint> constraints) {
		for (Constraint cons : constraints) {
			add(cons);
		}
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

	public void add(Constraint constraint) {
		for (Term term : constraint.getLhsTerms()) {
			variables.add(term.getVar());
		}
		constraints.add(constraint);
	}

}