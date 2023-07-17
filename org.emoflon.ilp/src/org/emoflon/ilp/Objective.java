package org.emoflon.ilp;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class Objective {

	// TODO: Linear OR QUADRATIC function
	private Function objective;
	private ObjectiveType type = ObjectiveType.MIN;
	private List<Constraint> constraints = new ArrayList<Constraint>();
	private List<GeneralConstraint> genConstraints = new ArrayList<GeneralConstraint>();
	private List<SOS1Constraint> sosConstraints = new ArrayList<SOS1Constraint>();

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

	public void setGenConstraints(List<GeneralConstraint> constraints) {
		for (GeneralConstraint cons : constraints) {
			add(cons);
		}
	}

	public void setSOSConstraints(List<SOS1Constraint> constraints) {
		for (SOS1Constraint cons : constraints) {
			add(cons);
		}
	}

	public List<Constraint> getConstraints() {
		return constraints;
	}

	public List<GeneralConstraint> getGeneralConstraints() {
		return genConstraints;
	}

	public List<SOS1Constraint> getSOSConstraints() {
		return sosConstraints;
	}

	public Set<Variable<?>> getVariables() {
		return variables;
	}

	public Function getObjective() {
		return objective;
	}

	public void setObjective(Function objective) {
		// TODO: nur Konstanten auch okay?
		if (objective.getTerms().isEmpty()) {
			throw new IllegalArgumentException("An Objective has to contain terms.");
		}
		for (Term term : objective.getTerms()) {
			variables.add(term.getVar1());
			if (term instanceof QuadraticTerm) {
				variables.add(((QuadraticTerm) term).getVar2());
			}
		}
		this.objective = objective;
	}

	public void setObjective(Function objective, ObjectiveType type) {
		setObjective(objective);
		setType(type);
	}

	public int getConstraintCount() {
		return constraints.size();
	}

	public int getGenConstraintCount() {
		return genConstraints.size();
	}

	public int getSOSConstraintCount() {
		return sosConstraints.size();
	}

	public int getTotalConstraintCount() {
		return getConstraintCount() + getGenConstraintCount();
	}

	public int getVariableCount() {
		return variables.size();
	}

	public void add(Constraint constraint) {
		if (constraint.getLhsTerms().isEmpty()) {
			throw new IllegalArgumentException("The left-hand side of a Constraint must not be empty!");
		}
		for (Term term : constraint.getLhsTerms()) {
			variables.add(term.getVar1());
			if (term instanceof QuadraticTerm) {
				variables.add(((QuadraticTerm) term).getVar2());
			}
		}
		constraints.add(constraint);
	}

	public void add(GeneralConstraint constraint) {
		if (constraint.getVariables().isEmpty()) {
			throw new IllegalArgumentException("The variables of a General Constraint must not be empty!");
		}
		for (Variable<?> var : constraint.getVariables()) {
			variables.add(var);
		}
		genConstraints.add(constraint);
	}

	public void add(SOS1Constraint constraint) {
		if (constraint.getVariables().isEmpty()) {
			throw new IllegalArgumentException("The variables of a SOS Constraint must not be empty!");
		} else if (constraint.getVariables().size() != constraint.getWeights().length) {
			throw new IllegalArgumentException("Every variable in a SOS Constraint should be assigned with a weight!");
		}
		for (Variable<?> var : constraint.getVariables()) {
			variables.add(var);
		}
		sosConstraints.add(constraint);
	}

}