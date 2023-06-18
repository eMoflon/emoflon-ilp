package org.emoflon.ilp;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

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

	public List<Term> getLhsTerms() {
		return lhsTerms;
	}

	public void setLhsTerms(List<Term> lhsTerms) {
		if (lhsTerms.stream().filter(t -> t.getType().equals(TermType.QUADRATIC)).toArray().length != 0) {
			throw new IllegalArgumentException("A linear constraint is not allowed to contain any quadratic terms.");
		}
		this.lhsTerms = lhsTerms;
	}

	public void addTerm(Term term) {
		if (lhsTerms.isEmpty()) {
			setLhsTerms(new ArrayList<Term>(Arrays.asList(term)));
		} else {
			this.lhsTerms.add(term);
		}
	}

	public Operator getOp() {
		return op;
	}

	public void setOp(Operator op) {
		this.op = op;
	}

	public double getRhs() {
		return rhs;
	}

	public void setRhs(double rhs) {
		this.rhs = rhs;
	}

	@Override
	public ConstraintType getType() {
		return ConstraintType.LINEAR;
	}

}