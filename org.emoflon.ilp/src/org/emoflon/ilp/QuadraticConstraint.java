package org.emoflon.ilp;

import java.util.List;
import java.util.ArrayList;

public class QuadraticConstraint implements NormalConstraint {

	private List<Term> lhsTerms;
	private Operator op;
	private double rhs;
	private double epsilon = 1.0E-4;

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

	public QuadraticConstraint(List<Term> lhsTerms, Operator op, double rhs, double epsilon) {
		this.setLhsTerms(lhsTerms);
		this.setOp(op);
		this.setRhs(rhs);
		this.setEpsilon(epsilon);
	}

	public QuadraticConstraint(Operator op, double rhs, double epsilon) {
		this.setLhsTerms(new ArrayList<Term>());
		this.setOp(op);
		this.setRhs(rhs);
		this.setEpsilon(epsilon);
	}

	public QuadraticConstraint(QuadraticConstraint quadConst) {
		this.setLhsTerms(quadConst.lhsTerms);
		this.setOp(quadConst.op);
		this.setRhs(quadConst.rhs);
		this.setEpsilon(quadConst.epsilon);
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

	public double getEpsilon() {
		return epsilon;
	}

	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	// Use for converting the operator from < to <=, from > to >= and from != to ???
	// returns the new LinearConstraint for < and >
	// returns the new Constraint for !=
	@Override
	public List<Constraint> convertOperator() {
		List<Constraint> substitute = new ArrayList<Constraint>();
		QuadraticConstraint copy = new QuadraticConstraint(this);
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
			// 1: psi_i + psi'_i >= epsilon
			LinearConstraint one = new LinearConstraint(Operator.GREATER_OR_EQUAL, this.epsilon);
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
			substitute.add(sos);

			// 4: f_i + psi_i >= k_i
			QuadraticConstraint left = new QuadraticConstraint(this.getLhsTerms(), Operator.GREATER_OR_EQUAL,
					this.getRhs());
			left.addTerm(psi, 1.0);
			substitute.add(left);

			// 5: f_i - psi'_i <= k_i
			QuadraticConstraint right = new QuadraticConstraint(this.getLhsTerms(), Operator.LESS_OR_EQUAL,
					this.getRhs());
			right.addTerm(psiPrime, -1.0);
			substitute.add(right);

			return substitute;

		// every other operator doesn't need to be converted
		default:
			// do nothing
			return substitute;
		}
	}

}