package org.emoflon.ilp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.AssumptionViolatedException;

/**
 * This class represents the problem to be solved.
 *
 */
public class Problem {

	private Function objective;
	private ObjectiveType type = ObjectiveType.MIN;
	private Map<String, NormalConstraint> constraints = new HashMap<String, NormalConstraint>();
	private Map<String, GeneralConstraint> genConstraints = new HashMap<String, GeneralConstraint>();
	private Map<String, SOS1Constraint> sosConstraints = new HashMap<String, SOS1Constraint>();
	private Map<String, OrConstraint> orConstraints = new HashMap<String, OrConstraint>();

	private Map<String, Variable<?>> variables = new HashMap<String, Variable<?>>();

	private int constraintNameGenCounter = 0;

	/**
	 * The constructor for a problem.
	 */
	public Problem() {
		super();
	}

	/**
	 * Returns the type (minimize or maximize) of the objective.
	 * 
	 * @return Objective type of this objective.
	 * @see ObjectiveType
	 */
	public ObjectiveType getType() {
		return type;
	}

	/**
	 * Sets the type of this objective.
	 * 
	 * @param type New type of this objective (MIN or MAX).
	 * @see ObjectiveType
	 */
	public void setType(ObjectiveType type) {
		this.type = type;
	}

	/**
	 * Adds a list of normal constraints (linear or quadratic) to the current
	 * constraints.
	 * 
	 * @param constraints List of normal constraints to be added to this problem.
	 * @see LinearConstraint
	 * @see QuadraticConstraint
	 */
	public void setConstraints(List<NormalConstraint> constraints) {
		for (NormalConstraint cons : constraints) {
			add(cons);
		}
	}

	/**
	 * Adds a list of general constraints to the current constraints.
	 * 
	 * @param constraints List of general constraints to be added to this problem.
	 */
	public void setGenConstraints(List<GeneralConstraint> constraints) {
		for (GeneralConstraint cons : constraints) {
			add(cons);
		}
	}

	/**
	 * Adds a list of SOS constraints to the current constraints.
	 * 
	 * @param constraints List of SOS1 constraints to be added to this problem.
	 * @see SOS1Constraint
	 */
	public void setSOSConstraints(List<SOS1Constraint> constraints) {
		for (SOS1Constraint cons : constraints) {
			add(cons);
		}
	}

	/**
	 * Adds a list of Or constraints to the current constraints.
	 * 
	 * @param constraints List of Or constraints to be added to this problem.
	 * @seer OrConstraint
	 */
	public void setOrConstraints(List<OrConstraint> constraints) {
		for (OrConstraint cons : constraints) {
			add(cons);
		}
	}

	/**
	 * Returns all normal constraints currently contained in this problem.
	 * 
	 * @return List of all normal constraints.
	 * @see LinearConstraint
	 * @see QuadraticConstraint
	 */
	public List<NormalConstraint> getConstraints() {
		return new ArrayList<NormalConstraint>(this.constraints.values());
	}

	/**
	 * Returns all general constraints currently contained in this problem.
	 * 
	 * @return List of all general constraints.
	 */
	public List<GeneralConstraint> getGeneralConstraints() {
		return new ArrayList<GeneralConstraint>(this.genConstraints.values());
	}

	/**
	 * Returns all SOS constraints currently contained in this problem.
	 * 
	 * @return List of all SOS constraints.
	 * @see SOS1Constraint
	 */
	public List<SOS1Constraint> getSOSConstraints() {
		return new ArrayList<SOS1Constraint>(this.sosConstraints.values());
	}

	/**
	 * Returns all Or constraints currently contained in this problem.
	 * 
	 * @return List of all Or constraints.
	 * @see OrConstraint
	 */
	public List<OrConstraint> getOrConstraints() {
		return new ArrayList<OrConstraint>(this.orConstraints.values());
	}

	/**
	 * Returns all variables that are part of the problem formulation.
	 * 
	 * @return Map of all variables, the keys are the names of the variables.
	 */
	public Map<String, Variable<?>> getVariables() {
		return variables;
	}

	/**
	 * Returns the objective function to be optimized in this problem.
	 * 
	 * @return Objective function of this problem.
	 * @see Function
	 */
	public Function getObjective() {
		return objective;
	}

	/**
	 * Sets the objective function of this problem.
	 * 
	 * @param objective Objective function to be optimized.
	 * @see Function
	 */
	public void setObjective(Function objective) {
		if (objective == null) {
			System.out.println("WARNING: The objective function of this problem is empty.");
			objective = new LinearFunction();
		} else if (objective.getTerms().isEmpty()) {
			System.out.println("WARNING: The objective function of this problem does not contain any variables.");
		}
		for (Term term : objective.getTerms()) {
			variables.put(term.getVar1().getName(), term.getVar1());
			if (term instanceof QuadraticTerm) {
				variables.put(((QuadraticTerm) term).getVar2().getName(), ((QuadraticTerm) term).getVar2());
			}
		}
		this.objective = objective;
	}

	/**
	 * Sets the objective function of this problem and the objective type (MIN or
	 * MAX).
	 * 
	 * @param objective Objective function to be optimized.
	 * @param type      Objective type of this problem (MIN or MAX).
	 * @see function
	 * @see ObjectiveType
	 */
	public void setObjective(Function objective, ObjectiveType type) {
		setObjective(objective);
		setType(type);
	}

	/**
	 * Sets the objective function of this problem to an empty linear function.
	 */
	public void setObjective() {
		if (objective != null && objective.getTerms().isEmpty()) {
			System.out.println("WARNING: The objective function of this problem is empty.");
		}
		this.objective = new LinearFunction();
	}

	/**
	 * Returns the amount of normal constraints currently in this problem.
	 * 
	 * @return Number of normal constraints.
	 */
	public int getConstraintCount() {
		return constraints.size();
	}

	/**
	 * Returns the amount of general constraints currently in this problem.
	 * 
	 * @return Number of general constraints.
	 */
	public int getGenConstraintCount() {
		return genConstraints.size();
	}

	/**
	 * Returns the amount of SOS constraints currently in this problem.
	 * 
	 * @return Number of SOS constraints.
	 */
	public int getSOSConstraintCount() {
		return sosConstraints.size();
	}

	/**
	 * Returns the amount of Or constraints currently in this problem.
	 * 
	 * @return Number of Or constraints.
	 */
	public int getOrConstraintCount() {
		return orConstraints.size();
	}

	/**
	 * Returns the total amount of constraints currently in this problem. This
	 * contains all normal, general, SOS and Or constraints.
	 * 
	 * @return Total number of constraints.
	 */
	public int getTotalConstraintCount() {
		return getConstraintCount() + getGenConstraintCount() + getSOSConstraintCount() + getOrConstraintCount();
	}

	/**
	 * Returns the amount of variables that are currently part of this problem.
	 * 
	 * @return Number of variables.
	 */
	public int getVariableCount() {
		return variables.size();
	}

	/**
	 * If the given constraint does not have a name, yet, this method sets a generic
	 * (unique) name.
	 * 
	 * @param constraint Constraint to set a generic (unique) name for.
	 */
	private void genConstraintNameIfAbsent(final Constraint constraint) {
		// If constraint has no name, generate one
		if (constraint.getName() == null || constraint.getName().isBlank()) {
			constraint.setName("cnstr_" + constraintNameGenCounter++);
		}
	}

	/**
	 * Adds a normal constraint to the current constraints.
	 * 
	 * @param constraint Normal constraint to be added to this problem.
	 * @see LinearConstraint
	 * @see QuadraticConstraint
	 */
	public void add(NormalConstraint constraint) {
		genConstraintNameIfAbsent(constraint);
		for (Term term : constraint.getLhsTerms()) {
			variables.put(term.getVar1().getName(), term.getVar1());
			if (term instanceof QuadraticTerm) {
				variables.put(((QuadraticTerm) term).getVar2().getName(), ((QuadraticTerm) term).getVar2());
			}
		}
		constraints.put(constraint.getName(), constraint);
	}

	/**
	 * Adds a general constraint to the current constraints.
	 * 
	 * @param constraint General constraint to be added to this problem.
	 */
	public void add(GeneralConstraint constraint) {
		genConstraintNameIfAbsent(constraint);
		for (Variable<?> var : constraint.getVariables()) {
			variables.put(var.getName(), var);
		}
		variables.put(constraint.getResult().getName(), constraint.getResult());
		genConstraints.put(constraint.getName(), constraint);
	}

	/**
	 * Adds a SOS constraint to the current constraints.
	 * 
	 * @param constraint SOS constraint to be added to this problem.
	 * @see SOS1Constraint
	 */
	public void add(SOS1Constraint constraint) {
		genConstraintNameIfAbsent(constraint);
		for (Variable<?> var : constraint.getVariables()) {
			variables.put(var.getName(), var);
		}
		sosConstraints.put(constraint.getName(), constraint);
	}

	/**
	 * Adds a Or constraint to the current constraints.
	 * 
	 * @param constraint Or constraint to be added to this problem.
	 * @see OrConstraint
	 */
	public void add(OrConstraint constraint) {
		genConstraintNameIfAbsent(constraint);
		for (LinearConstraint lin : constraint.getConstraints()) {
			for (Term term : lin.getLhsTerms()) {
				variables.put(term.getVar1().getName(), term.getVar1());
			}
		}
		orConstraints.put(constraint.getName(), constraint);
	}

	/**
	 * Removes this normal constraint from the current constraints.
	 * 
	 * @param constraint Normal constraint to be removed.
	 * @return True, if the constraint was part of the problem formulation and was
	 *         successfully removed. False, if the constraint was not part of the
	 *         problem formulation.
	 */
	public boolean remove(NormalConstraint constraint) {
		return constraints.remove(constraint.getName()) != null;
	}

	/**
	 * Removes this general constraint from the current constraints.
	 * 
	 * @param constraint General constraint to be removed.
	 * @return True, if the constraint was part of the problem formulation and was
	 *         successfully removed. False, if the constraint was not part of the
	 *         problem formulation.
	 */
	public boolean remove(GeneralConstraint constraint) {
		return genConstraints.remove(constraint.getName()) != null;
	}

	/**
	 * Removes this SOS constraint from the current constraints.
	 * 
	 * @param constraint SOS constraint to be removed.
	 * @return True, if the constraint was part of the problem formulation and was
	 *         successfully removed. False, if the constraint was not part of the
	 *         problem formulation.
	 */
	public boolean remove(SOS1Constraint constraint) {
		return sosConstraints.remove(constraint.getName()) != null;
	}

	/**
	 * Removes this Or constraint from the current constraints.
	 * 
	 * @param constraint Or constraint to be removed.
	 * @return True, if the constraint was part of the problem formulation and was
	 *         successfully removed. False, if the constraint was not part of the
	 *         problem formulation.
	 */
	public boolean remove(OrConstraint constraint) {
		return orConstraints.remove(constraint.getName()) != null;
	}

	/**
	 * Substitutes all Or constraints with the result of their conversion. After
	 * calling this method, there are no more Or constraints in the problem
	 * formulation. The Or constraints are substituted with linear constraints and
	 * SOS1 constraints. <br>
	 * <br>
	 * 
	 * Please call substituteSOS1() after calling this method (if needed)!
	 */
	public void substituteOr() {
		List<NormalConstraint> normalConstraints = new ArrayList<NormalConstraint>();
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

		// Remove all OrConstraints
		orConstraints.clear();
	}

	/**
	 * Substitutes all normal constraints containing LESS, GREATER or NOT_EQUAL
	 * operators. After calling this method, all normal constraints have the
	 * operators LESS_OR_EQUAL, GREATER_OR_EQUAL or EQUAL. The substitution
	 * constraints are normal constraints and SOS1 constraints (for operator
	 * NOT_EQUAL). <br>
	 * <br>
	 * 
	 * Please call substituteSOS1() after calling this method (if needed)!
	 */
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
		for (final NormalConstraint toDelete : delete) {
			this.constraints.remove(toDelete.getName());
		}
		delete.forEach(it -> this.remove(it));

		// add substitutions
		opSubstitution.forEach(it -> this.add(it));
		sosConstraints.forEach(it -> this.add(it));
	}

	/**
	 * Substitutes all SOS1 constraints with the result of their conversion. After
	 * calling this method, there are no more SOS1 constraints in the problem
	 * formulation. The SO1 constraints are substituted with linear constraints.
	 * <br>
	 * <br>
	 * 
	 * Please call substituteOr() and substituteOperators() before calling this
	 * method (if needed)!
	 */
	public void substituteSOS1() {
		// SOS1 Constraints
		// Substitute SOS1 Constraints
		for (SOS1Constraint constraint : this.getSOSConstraints()) {
			List<LinearConstraint> substitution = constraint.convert();
			substitution.forEach(it -> this.add(it));
		}

		// remove all SOS Constraints
		sosConstraints.clear();
	}

	/**
	 * Returns the constraint for the given name or throws an error if there is no
	 * constraint with a matching name.
	 * 
	 * @param name Constraint name to search for.
	 * @return Constraint for the given name.
	 */
	public Constraint getConstraintByName(final String name) {
		if (this.constraints.containsKey(name)) {
			return this.constraints.get(name);
		} else if (this.genConstraints.containsKey(name)) {
			return this.genConstraints.get(name);
		} else if (this.sosConstraints.containsKey(name)) {
			return this.sosConstraints.get(name);
		} else if (this.orConstraints.containsKey(name)) {
			return this.orConstraints.get(name);
		} else {
			throw new Error("Constraint with name " + name + " not found.");
		}
	}

	/**
	 * Returns true if the problem contains a constraint with the given name.
	 * 
	 * @param name Name to check the constraint existence for.
	 * @return True if the problem contains a constraint with the given name.
	 */
	public boolean hasConstraintWithName(final String name) {
		return this.constraints.containsKey(name) || this.genConstraints.containsKey(name)
				|| this.sosConstraints.containsKey(name) || this.orConstraints.containsKey(name);
	}

	/**
	 * Validates all constraints that are part of this problem instance.
	 */
	public void validateConstraints() {
		this.constraints.values().forEach(v -> {
			if (v.getLhsTerms().isEmpty()) {
				throw new AssumptionViolatedException("The constraint " + v.getName() + " has no LHS terms.");
			}
		});

		this.genConstraints.values().forEach(v -> {
			if (v.getVariables().isEmpty()) {
				throw new AssumptionViolatedException("The generic constraint " + v.getName() + " has no variables.");
			}
		});

		this.sosConstraints.values().forEach(v -> {
			if (v.getVariables().isEmpty()) {
				throw new AssumptionViolatedException("The SOS1 constraint " + v.getName() + " has no variables.");
			}
			if (v.getVariables().size() != v.getWeights().length) {
				throw new AssumptionViolatedException("The SOS1 constraint " + v.getName()
						+ " does not contain the correct amount of weights for the number of variables.");
			}
		});

		this.orConstraints.values().forEach(v -> {
			if (v.getConstraints().isEmpty()) {
				throw new AssumptionViolatedException("The Or constraint " + v.getName() + " has no sub constraints.");
			}
		});
	}

}
