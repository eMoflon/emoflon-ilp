package org.emoflon.ilp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Objective {

	// TODO: Linear OR QUADRATIC function
	private Function objective;
	private ObjectiveType type = ObjectiveType.MIN;
	private List<NormalConstraint> constraints = new ArrayList<NormalConstraint>();
	private List<GeneralConstraint> genConstraints = new ArrayList<GeneralConstraint>();
	private List<SOS1Constraint> sosConstraints = new ArrayList<SOS1Constraint>();
	private List<OrConstraint> orConstraints = new ArrayList<OrConstraint>();

	private Map<String, Variable<?>> variables = new HashMap<String, Variable<?>>();

	public Objective() {
		super();
	}

	public ObjectiveType getType() {
		return type;
	}

	public void setType(ObjectiveType type) {
		this.type = type;
	}

	public void setConstraints(List<NormalConstraint> constraints) {
		for (NormalConstraint cons : constraints) {
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

	public void setOrConstraints(List<OrConstraint> constraints) {
		for (OrConstraint cons : constraints) {
			add(cons);
		}
	}

	public List<NormalConstraint> getConstraints() {
		return constraints;
	}

	public List<GeneralConstraint> getGeneralConstraints() {
		return genConstraints;
	}

	public List<SOS1Constraint> getSOSConstraints() {
		return sosConstraints;
	}

	public List<OrConstraint> getOrConstraints() {
		return orConstraints;
	}

	public Map<String, Variable<?>> getVariables() {
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
			variables.put(term.getVar1().getName(), term.getVar1());
			if (term instanceof QuadraticTerm) {
				variables.put(((QuadraticTerm) term).getVar2().getName(), ((QuadraticTerm) term).getVar2());
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

	public int getOrConstraintCount() {
		return orConstraints.size();
	}

	public int getTotalConstraintCount() {
		return getConstraintCount() + getGenConstraintCount() + getSOSConstraintCount() + getOrConstraintCount();
	}

	public int getVariableCount() {
		return variables.size();
	}

	public void add(NormalConstraint constraint) {
		if (constraint.getLhsTerms().isEmpty()) {
			throw new IllegalArgumentException("The left-hand side of a Constraint must not be empty!");
		}
		for (Term term : constraint.getLhsTerms()) {
			variables.put(term.getVar1().getName(), term.getVar1());
			if (term instanceof QuadraticTerm) {
				variables.put(((QuadraticTerm) term).getVar2().getName(), ((QuadraticTerm) term).getVar2());
			}
		}
		constraints.add(constraint);
	}

	public void add(GeneralConstraint constraint) {
		if (constraint.getVariables().isEmpty()) {
			throw new IllegalArgumentException("The variables of a General Constraint must not be empty!");
		}
		for (Variable<?> var : constraint.getVariables()) {
			variables.put(var.getName(), var);
		}
		variables.put(constraint.getResult().getName(), constraint.getResult());
		genConstraints.add(constraint);
	}

	public void add(SOS1Constraint constraint) {
		if (constraint.getVariables().isEmpty()) {
			throw new IllegalArgumentException("The variables of a SOS Constraint must not be empty!");
		} else if (constraint.getVariables().size() != constraint.getWeights().length) {
			throw new IllegalArgumentException("Every variable in a SOS Constraint should be assigned with a weight!");
		}
		for (Variable<?> var : constraint.getVariables()) {
			variables.put(var.getName(), var);
		}
		sosConstraints.add(constraint);
	}

	public void add(OrConstraint constraint) {
		if (constraint.getConstraints().isEmpty()) {
			throw new IllegalArgumentException("The constraints of a Or Constraint must not be empty!");
		}
		for (LinearConstraint lin : constraint.getConstraints()) {
			for (Term term : lin.getLhsTerms()) {
				variables.put(term.getVar1().getName(), term.getVar1());
			}
		}
		orConstraints.add(constraint);
	}

	public boolean remove(NormalConstraint constraint) {
		return constraints.remove(constraint);
	}

	public boolean remove(GeneralConstraint constraint) {
		return genConstraints.remove(constraint);
	}

	public boolean remove(SOS1Constraint constraint) {
		return sosConstraints.remove(constraint);
	}

	public boolean remove(OrConstraint constraint) {
		return orConstraints.remove(constraint);
	}

	public void substituteOr() {
		List<NormalConstraint> normalConstraints = new ArrayList<NormalConstraint>();
		normalConstraints.addAll(this.getConstraints());
		List<SOS1Constraint> sosConstraints = new ArrayList<SOS1Constraint>();

		// Or Constraints
		// Substitute Or Constraints with Linear Constraints and SOS1 Constraints
		for (OrConstraint constraint : this.getOrConstraints()) {
			List<Constraint> converted = constraint.convert();
			normalConstraints.addAll(converted.stream().filter(NormalConstraint.class::isInstance)
					.map(LinearConstraint.class::cast).collect(Collectors.toList()));
			sosConstraints.addAll(converted.stream().filter(SOS1Constraint.class::isInstance)
					.map(SOS1Constraint.class::cast).collect(Collectors.toList()));
		}

		// Add substitutions to other linear and SOS constraints
		normalConstraints.forEach(it -> this.add(it));
		sosConstraints.forEach(it -> this.add(it));
	}

	public void substituteOperators() {
		// Substitute <, >, !=
		List<NormalConstraint> opSubstitution = new ArrayList<NormalConstraint>();
		List<SOS1Constraint> sosConstraints = new ArrayList<SOS1Constraint>();
		List<NormalConstraint> delete = new ArrayList<NormalConstraint>();

		for (NormalConstraint constraint : this.getConstraints()) {
			List<Constraint> substitution = constraint.convertOperator();
			if (substitution.size() > 0) {
				delete.add(constraint);
			}
			opSubstitution.addAll(substitution.stream().filter(LinearConstraint.class::isInstance)
					.map(LinearConstraint.class::cast).collect(Collectors.toList()));
			opSubstitution.addAll(substitution.stream().filter(QuadraticConstraint.class::isInstance)
					.map(QuadraticConstraint.class::cast).collect(Collectors.toList()));
			sosConstraints.addAll(substitution.stream().filter(SOS1Constraint.class::isInstance)
					.map(SOS1Constraint.class::cast).collect(Collectors.toList()));
		}
		// delete converted constraints
		this.constraints.removeAll(delete);
		delete.forEach(it -> this.remove(it));

		// add substitutions
		opSubstitution.forEach(it -> this.add(it));
		sosConstraints.forEach(it -> this.add(it));
	}

	public void substituteSOS1() {
		// SOS1 Constraints
		// Substitute SOS1 Constraints
		for (SOS1Constraint constraint : this.getSOSConstraints()) {
			List<LinearConstraint> substitution = constraint.convert();
			substitution.forEach(it -> this.add(it));
		}
	}

}