package org.emoflon.ilp;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents linear constraints. <br>
 * 
 * The terms on the left-hand side are summed up. <br>
 * <br>
 * 
 * w_1 * x_1 + w_2 * x_2 + ... (>= | > | = | != | < | <=) rhs
 */
public class LinearConstraint implements NormalConstraint {

	private List<Term> lhsTerms = new ArrayList<Term>();
	private Operator op;
	private double rhs;
	private double epsilon = 1.0E-4;

	/**
	 * A constructor for a Linear Constraint.
	 * 
	 * @param lhsTerms A list of the terms (linear!) on the left-hand side of the
	 *                 constraint.
	 * @param op       The operator used for this constraint.
	 * @param rhs      The value on the right-hand side of the constraint.
	 * @see LinearTerm
	 * @see Operator
	 */
	public LinearConstraint(List<Term> lhsTerms, Operator op, double rhs) {
		this.setLhsTerms(lhsTerms);
		this.setOp(op);
		this.setRhs(rhs);
	}

	/**
	 * A constructor for a Linear Constraint.
	 * 
	 * @param op  The operator used for this constraint.
	 * @param rhs The value on the right-hand side of the constraint.
	 * @see Operator
	 */
	public LinearConstraint(Operator op, double rhs) {
		this(new ArrayList<Term>(), op, rhs);
	}

	/**
	 * A constructor for a Linear Constraint.
	 * 
	 * @param lhsTerms A list of the terms (linear!) on the left-hand side of the
	 *                 constraint.
	 * @param op       The operator used for this constraint.
	 * @param rhs      The value on the right-hand side of the constraint.
	 * @param epsilon  A small value used for conversion if necessary.
	 * @see LinearTerm
	 * @see Operator
	 */
	public LinearConstraint(List<Term> lhsTerms, Operator op, double rhs, double epsilon) {
		this(lhsTerms, op, rhs);
		this.setEpsilon(epsilon);
	}

	/**
	 * A constructor for a Linear Constraint.
	 * 
	 * @param op      The operator used for this constraint.
	 * @param rhs     The value on the right-hand side of the constraint.
	 * @param epsilon A small value used for conversion if necessary.
	 * @see Operator
	 */
	public LinearConstraint(Operator op, double rhs, double epsilon) {
		this(op, rhs);
		this.setEpsilon(epsilon);
	}

	/**
	 * A copy constructor for a Linear Constraint.
	 * 
	 * @param linConst The linear constraint to be copied.
	 */
	public LinearConstraint(LinearConstraint linConst) {
		this.setLhsTerms(linConst.getLhsTerms());
		this.setOp(linConst.getOp());
		this.setRhs(linConst.getRhs());
		this.setEpsilon(linConst.getEpsilon());
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
		this.lhsTerms.addAll(lhsTerms);
	}

	@Override
	public void addTerm(Term term) {
		this.lhsTerms.add(term);
	}

	/**
	 * Adds a new linear term to the left-hand side of the constraint (weight *
	 * variable).
	 * 
	 * @param var    Variable to be added in the term.
	 * @param weight Weight of the term.
	 */
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

	/**
	 * Returns the value of epsilon for this constraint.
	 * 
	 * @return Epsilon
	 */
	public double getEpsilon() {
		return epsilon;
	}

	/**
	 * Sets the value of epsilon for this constraint.
	 * 
	 * @param epsilon A small value used for conversion if necessary.
	 */
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

	@Override
	public List<Constraint> convertOperator() {
		List<Constraint> substitute = new ArrayList<Constraint>();
		LinearConstraint copy = new LinearConstraint(this);
		switch (this.op) {
		// a < b => a + e <= b <=> a <= b - e
		case LESS:
			copy.setRhs(this.rhs - epsilon);
			copy.setOp(Operator.LESS_OR_EQUAL);
			substitute.add(copy);
			return substitute;

		// a > b => a - e >= b <=> a >= b + e
		case GREATER:
			copy.setRhs(this.rhs + epsilon);
			copy.setOp(Operator.GREATER_OR_EQUAL);
			substitute.add(copy);
			return substitute;

		// a != b => a > b || a < b
		// f_i != k_i <=> (f_i + psi_i >= k_i) /\ (f_i - psi'_i <= k_i)
		case NOT_EQUAL:
			// 1: psi_i + psi'_i >= 2 * epsilon
			LinearConstraint one = new LinearConstraint(Operator.GREATER_OR_EQUAL, 2 * this.epsilon, this.epsilon);
			RealVariable psi = new RealVariable("psi_".concat(copy.toString()));
			RealVariable psiPrime = new RealVariable("psiPrime_".concat(copy.toString()));

			// 2: psi_i + psi'_i elementof R+
			psi.setLowerBound(0.0);
			psiPrime.setLowerBound(0.0);

			one.addTerm(psi, 1);
			one.addTerm(psiPrime, 1);

			substitute.add(one);

			// 3: not(psi_i != 0 AND psi'_i != 0) -> SOS1(psi_i, psi'_i)
			List<Variable<?>> sosVars = new ArrayList<Variable<?>>();
			sosVars.add(psi);
			sosVars.add(psiPrime);
			SOS1Constraint sos = new SOS1Constraint(sosVars);
			sos.setEpsilon(this.epsilon);
			substitute.add(sos);

			// 4: f_i + psi_i > k_i -> f_i + psi_i >= k_i + epsilon
			LinearConstraint left = new LinearConstraint(this.getLhsTerms(), Operator.GREATER_OR_EQUAL,
					this.getRhs() + this.epsilon, this.epsilon);
			left.addTerm(psi, 1.0);
			substitute.add(left);

			// 5: f_i - psi'_i < k_i -> f_i - psi'_i <= k_i - epsilon
			LinearConstraint right = new LinearConstraint(this.getLhsTerms(), Operator.LESS_OR_EQUAL,
					this.getRhs() - this.epsilon, this.epsilon);
			right.addTerm(psiPrime, -1.0);
			substitute.add(right);

			return substitute;

		// every other operator doesn't need to be converted
		default:
			// do nothing
			return substitute;
		}
	}

	/*
	 * // un-comment for debugging purposes public String toString() { StringBuilder
	 * sb = new StringBuilder(); int i = 1; for (Term term : this.lhsTerms) {
	 * sb.append(term.getWeight()); sb.append(" * ");
	 * sb.append(term.getVar1().getName()); if (i++ != this.lhsTerms.size()) {
	 * sb.append(" + "); } }
	 * 
	 * sb.append(this.op); sb.append(this.rhs);
	 * 
	 * return sb.toString(); }
	 */

}