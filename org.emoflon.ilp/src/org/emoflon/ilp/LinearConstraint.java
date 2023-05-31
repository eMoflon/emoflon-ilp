package org.emoflon.ilp;

import java.util.List;

import org.emoflon.ilp.Term.TermType;

public class LinearConstraint implements Constraint {

	private List<Term> lhsTerms;
	private Operator op;
	private double rhs;

	public LinearConstraint(List<Term> lhsTerms, Operator op, double rhs) {
		this.setLhsTerms(lhsTerms);
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

}