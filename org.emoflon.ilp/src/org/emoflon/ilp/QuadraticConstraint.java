package org.emoflon.ilp;

import java.util.List;

import org.emoflon.ilp.Term.TermType;

public class QuadraticConstraint implements Constraint {

	private List<Term> lhsTerms;
	private Operator op;
	private double rhs;

	public QuadraticConstraint(List<Term> lhsTerms, Operator op, double rhs) {
		this.setLhsTerms(lhsTerms);
		this.setOp(op);
		this.setRhs(rhs);
	}

	public List<Term> getLhsTerms() {
		return lhsTerms;
	}

	public void setLhsTerms(List<Term> lhsTerms) {
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