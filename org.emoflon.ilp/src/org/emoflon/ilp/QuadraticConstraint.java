package org.emoflon.ilp;

import java.util.List;
import java.util.ArrayList;

public class QuadraticConstraint implements NormalConstraint {

	private List<Term> lhsTerms;
	private Operator op;
	private double rhs;

	public QuadraticConstraint(List<Term> lhsTerms, Operator op, double rhs) {
		this.setLhsTerms(lhsTerms);
		this.setOp(op);
		this.setRhs(rhs);
	}

	public QuadraticConstraint(Operator op, double rhs) {
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
		this.lhsTerms = lhsTerms;
	}

	@Override
	public void addTerm(Term term) {
		this.lhsTerms.add(term);
	}
	
	public void addTerm(Variable<?> var, double weight) {
		this.lhsTerms.add(new LinearTerm(var, weight));
	}
	
	public void addTerm(Variable<?> var1, Variable<?> var2, double weight) {
		this.lhsTerms.add(new QuadraticTerm(var1, var2, weight));
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
		return ConstraintType.QUADRATIC;
	}

}