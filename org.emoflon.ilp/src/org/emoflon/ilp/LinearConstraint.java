package org.emoflon.ilp;

import java.util.ArrayList;
import java.util.List;

public class LinearConstraint implements Constraint {

	private List<Term> lhsTerms;
	private Operator op;
	private double rhs;

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

	@Override
	public ConstraintType getType() {
		return ConstraintType.LINEAR;
	}

}