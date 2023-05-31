package org.emoflon.ilp;

import java.util.List;

public interface Constraint {

	public List<Term> getLhsTerms();

	public void setLhsTerms(List<Term> lhsTerms);

	public Operator getOp();

	public void setOp(Operator op);

	public double getRhs();

	public void setRhs(double rhs);

	public ConstraintType getType();

}
