package org.emoflon.ilp;

import java.util.List;

public abstract class NormalConstraint extends Constraint {

	/**
	 * Returns a list of the terms on the left-hand side of the constraint.
	 * 
	 * @return Current list of terms on the left-hand side of the constraint.
	 * @see Term
	 */
	public abstract List<Term> getLhsTerms();

	/**
	 * Sets the terms of the constraint.
	 * 
	 * @param lhsTerms New list of terms to be set for the constraint.
	 * @see Term
	 */
	public abstract void setLhsTerms(final List<Term> lhsTerms);

	/**
	 * Adds a term to the existing terms on the left-hand side of the constraint.
	 * 
	 * @param term New term to be added to the constraint.
	 * @see Term
	 */
	public abstract void addTerm(final Term term);

	/**
	 * Returns the operator of the constraint.
	 * 
	 * @return Current operator of the constraint.
	 * @see Operator
	 */
	public abstract Operator getOp();

	/**
	 * Sets the operator of the constraint.
	 * 
	 * @param op Operator to be set for the constraint.
	 * @see Operator
	 */
	public abstract void setOp(final Operator op);

	/**
	 * Returns the value on the right-hand side of the constraint.
	 * 
	 * @return Current Value on the right-hand side.
	 */
	public abstract double getRhs();

	/**
	 * Sets the value on the right-hand side of the constraint.
	 * 
	 * @param rhs New value on the right-hand side.
	 */
	public abstract void setRhs(final double rhs);

	/**
	 * Returns the type of the constraint.
	 * 
	 * @return Type of the constraint.
	 * @see ConstraintType
	 */
	public abstract ConstraintType getType();

	/**
	 * Converts the constraint to a list of new constraints, if needed. Only
	 * converts if the current operator is either LESS, GREATER or NOT_EQUAL.
	 * 
	 * @return List of substitution constraints or empty list.
	 */
	public abstract List<Constraint> convertOperator();

}
