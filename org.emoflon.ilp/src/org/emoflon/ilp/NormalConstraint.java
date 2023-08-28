package org.emoflon.ilp;

import java.util.List;

// TODO: rename?
public interface NormalConstraint extends Constraint {

	public List<Term> getLhsTerms();

	public void setLhsTerms(List<Term> lhsTerms);

	public void addTerm(Term term);

	public Operator getOp();

	public void setOp(Operator op);

	public double getRhs();

	public void setRhs(double rhs);

	public ConstraintType getType();

	public List<Constraint> convertOperator();

}
