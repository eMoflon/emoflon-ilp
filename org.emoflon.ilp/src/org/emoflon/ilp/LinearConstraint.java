package org.emoflon.ilp;

import java.util.ArrayList;
import java.util.List;

public class LinearConstraint implements Constraint {

	private List<Term> lhsTerms;
	private Operator op;
	private double rhs;
	private double epsilon = 1.0E-4;

	public LinearConstraint(List<Term> lhsTerms, Operator op, double rhs) {
		this.setLhsTerms(lhsTerms);
		this.setOp(op);
		this.setRhs(rhs);
	}

	public LinearConstraint(Operator op, double rhs) {
		this.setLhsTerms(new ArrayList<Term>());
		this.setOp(op);
		this.setRhs(rhs);
	}

	public LinearConstraint(List<Term> lhsTerms, Operator op, double rhs, double epsilon) {
		this.setLhsTerms(lhsTerms);
		this.setOp(op);
		this.setRhs(rhs);
		this.setEpsilon(epsilon);
	}

	public LinearConstraint(Operator op, double rhs, double epsilon) {
		this.setLhsTerms(new ArrayList<Term>());
		this.setOp(op);
		this.setRhs(rhs);
		this.setEpsilon(epsilon);
	}

	@Override
	public List<Term> getLhsTerms() {
		return lhsTerms;
	}

	@Override
	public void setLhsTerms(List<Term> lhsTerms) {
		if (lhsTerms.stream().filter(t -> t instanceof QuadraticTerm).count() != 0) {
			throw new IllegalArgumentException("A linear constraint is not allowed to contain any quadratic terms.");
		}
		this.lhsTerms = lhsTerms;
	}

	@Override
	public void addTerm(Term term) {
		this.lhsTerms.add(term);
	}

	public void addTerm(Variable<?> var, double weight) {
		this.addTerm(new LinearTerm(var, weight));
	}

	@Override
	public Operator getOp() {
		return op;
	}

	@Override
	public void setOp(Operator op) {
		this.op = op;
	}

	@Override
	public double getRhs() {
		return rhs;
	}

	@Override
	public void setRhs(double rhs) {
		this.rhs = rhs;
	}

	public double getEpsilon() {
		return epsilon;
	}

	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	@Override
	public ConstraintType getType() {
		return ConstraintType.LINEAR;
	}

	// Use for converting the operator from < to <=, from > to >= and from != to ???
	// returns the new LinearConstraint for < and >
	// returns the new Constraint for !=
	public Constraint convertOperator() {
		switch (this.op) {
		// a < b => a - e <= b <=> a <= b + e
		case LESS:
			this.setRhs(this.rhs + epsilon);
			this.setOp(Operator.LESS_OR_EQUAL);
			return this;

		// a > b => a + e >= b <=> a >= b - e
		case GREATER:
			this.setRhs(this.rhs - epsilon);
			this.setOp(Operator.GREATER_OR_EQUAL);
			return this;

		// a != b => a > b || a < b
		case NOT_EQUAL:
			// TODO: Implement this!
			throw new Error("Not yet implemented!");

		// every other operator doesn't need to be converted
		default:
			// do nothing
			return this;
		}
	}

}