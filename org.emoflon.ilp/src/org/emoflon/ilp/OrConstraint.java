package org.emoflon.ilp;

import java.util.ArrayList;
import java.util.List;

public class OrConstraint {

	private List<LinearConstraint> constraints;
	private BinaryVariable result;
	private double epsilon = 1.0E-4;

	public OrConstraint(List<LinearConstraint> constraints, BinaryVariable result) {
		this.setConstraints(constraints);
		this.setResult(result);
	}

	public OrConstraint(BinaryVariable result) {
		this.constraints = new ArrayList<LinearConstraint>();
		this.setResult(result);
	}

	public OrConstraint(List<LinearConstraint> constraints, BinaryVariable result, double epsilon) {
		this.setConstraints(constraints);
		this.setResult(result);
		this.setEpsilon(epsilon);
	}

	public OrConstraint(BinaryVariable result, double epsilon) {
		this.constraints = new ArrayList<LinearConstraint>();
		this.setResult(result);
		this.setEpsilon(epsilon);
	}

	public List<LinearConstraint> getConstraints() {
		return this.constraints;
	}

	public void setConstraints(List<LinearConstraint> constraints) {
		this.constraints = constraints;
	}

	public BinaryVariable getResult() {
		return result;
	}

	public void setResult(BinaryVariable res) {
		this.result = res;
	}

	public ConstraintType getType() {
		return ConstraintType.OR;
	}

	public void addConstraint(LinearConstraint constr) {
		constraints.add(constr);
	}

	public void setResult(Variable<?> res) {
		if (res.getValue().doubleValue() != 1 || res.getValue().doubleValue() != 0) {
			throw new IllegalArgumentException("The result of an Or Constraint has to be binary.");
		} else {
			result = (BinaryVariable) res;
		}
	}

	public double getEpsilon() {
		return epsilon;
	}

	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	// Translate to normal (incl. SOS1) constraints
	// TODO: negation of constraints?
	public List<Constraint> convert() {
		List<Constraint> substitute = new ArrayList<Constraint>();
		LinearConstraint binary_sub = new LinearConstraint(Operator.GREATER_OR_EQUAL, 0.0);

		for (LinearConstraint lin : this.constraints) {
			switch (lin.getOp()) {
			case GREATER_OR_EQUAL:
				// (1) s_i, s'_i element {0,1}
				BinaryVariable s_geq = new BinaryVariable("s_".concat(this.toString()).concat(lin.toString()));
				BinaryVariable s_prime_geq = new BinaryVariable(
						"s_prime_".concat(this.toString()).concat(lin.toString()));
				binary_sub.addTerm(s_geq, 1.0);

				// (1) s_i + s'_i = 1
				LinearConstraint geq1 = new LinearConstraint(Operator.EQUAL, 1.0);
				geq1.addTerm(s_geq, 1.0);
				geq1.addTerm(s_prime_geq, 1.0);
				substitute.add(geq1);

				// (2) phi_i element R, f_i + phi_i >= k_i
				RealVariable phi_geq = new RealVariable("phi_".concat(this.toString().concat(lin.toString())));
				LinearConstraint geq2 = new LinearConstraint(lin.getLhsTerms(), Operator.GREATER_OR_EQUAL,
						lin.getRhs());
				geq2.addTerm(phi_geq, 1.0);
				substitute.add(geq2);

				// (3) phi'_i element R, f_i - phi'_i < k_i
				RealVariable phi_prime_geq = new RealVariable(
						"phi_prime_".concat(this.toString()).concat(lin.toString()));
				LinearConstraint geq3 = new LinearConstraint(lin.getLhsTerms(), Operator.LESS, lin.getRhs());
				geq3.addTerm(phi_prime_geq, -1.0);
				substitute.add(geq3);

				// (4) phi_i + s_i >= 2*e und e element R+\{0}
				LinearConstraint geq4 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 2 * this.epsilon);
				geq4.addTerm(phi_geq, 1.0);
				geq4.addTerm(s_geq, 1.0);
				substitute.add(geq4);

				// (5) phi'_i + s'_i >= 2*e
				LinearConstraint geq5 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 2 * this.epsilon);
				geq5.addTerm(phi_prime_geq, 1.0);
				geq5.addTerm(s_prime_geq, 1.0);
				substitute.add(geq5);

				// (6) SOS1(phi_i, s_i)
				List<Variable<?>> sosVars_geq6 = new ArrayList<Variable<?>>();
				sosVars_geq6.add(phi_geq);
				sosVars_geq6.add(s_geq);
				SOS1Constraint geq6 = new SOS1Constraint(sosVars_geq6);
				substitute.add(geq6);

				// (7) SOS1(phi'_i, s'_i)
				List<Variable<?>> sosVars_geq7 = new ArrayList<Variable<?>>();
				sosVars_geq7.add(phi_prime_geq);
				sosVars_geq7.add(s_prime_geq);
				SOS1Constraint geq7 = new SOS1Constraint(sosVars_geq7);
				substitute.add(geq7);

				break;
			case GREATER:
				// (1) s_i, s'_i element {0,1}
				BinaryVariable s_gr = new BinaryVariable("s_".concat(this.toString()).concat(lin.toString()));
				BinaryVariable s_prime_gr = new BinaryVariable(
						"s_prime_".concat(this.toString()).concat(lin.toString()));
				binary_sub.addTerm(s_gr, 1.0);

				// (1) s_i + s'_i = 1
				LinearConstraint gr1 = new LinearConstraint(Operator.EQUAL, 1.0);
				gr1.addTerm(s_gr, 1.0);
				gr1.addTerm(s_prime_gr, 1.0);
				substitute.add(gr1);

				// (2) phi_i element R, f_i + phi_i > k_i
				RealVariable phi_gr = new RealVariable("phi_".concat(this.toString().concat(lin.toString())));
				LinearConstraint gr2 = new LinearConstraint(lin.getLhsTerms(), Operator.GREATER, lin.getRhs());
				gr2.addTerm(phi_gr, 1.0);
				substitute.add(gr2);

				// (3) phi'_i element R, f_i - phi'_i <= k_i
				RealVariable phi_prime_gr = new RealVariable(
						"phi_prime_".concat(this.toString()).concat(lin.toString()));
				LinearConstraint gr3 = new LinearConstraint(lin.getLhsTerms(), Operator.LESS_OR_EQUAL, lin.getRhs());
				gr3.addTerm(phi_prime_gr, -1.0);
				substitute.add(gr3);

				// (4) phi_i + s_i >= 2*e
				LinearConstraint gr4 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 2 * this.epsilon);
				gr4.addTerm(phi_gr, 1.0);
				gr4.addTerm(s_gr, 1.0);
				substitute.add(gr4);

				// (5) phi'_i + s'_i >= 2*e
				LinearConstraint gr5 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 2 * this.epsilon);
				gr5.addTerm(phi_prime_gr, 1.0);
				gr5.addTerm(s_prime_gr, 1.0);
				substitute.add(gr5);

				// (6) SOS1(phi_i, s_i)
				List<Variable<?>> sosVars_gr6 = new ArrayList<Variable<?>>();
				sosVars_gr6.add(phi_gr);
				sosVars_gr6.add(s_gr);
				SOS1Constraint gr6 = new SOS1Constraint(sosVars_gr6);
				substitute.add(gr6);

				// (7) SOS1(phi'_i, s'_i)
				List<Variable<?>> sosVars_gr7 = new ArrayList<Variable<?>>();
				sosVars_gr7.add(phi_prime_gr);
				sosVars_gr7.add(s_prime_gr);
				SOS1Constraint gr7 = new SOS1Constraint(sosVars_gr7);
				substitute.add(gr7);

				break;
			case EQUAL:
				// (1) s_i element {0,1}
				BinaryVariable s_eq = new BinaryVariable("s_".concat(this.toString()).concat(lin.toString()));
				binary_sub.addTerm(s_eq, 1.0);

				// (2) phi_i element R+, f_i + phi_i >= k_i
				RealVariable phi_eq = new RealVariable("phi_".concat(this.toString().concat(lin.toString())));
				phi_eq.setLowerBound(0.0);
				LinearConstraint eq1 = new LinearConstraint(lin.getLhsTerms(), Operator.GREATER_OR_EQUAL, lin.getRhs());
				eq1.addTerm(phi_eq, 1.0);
				substitute.add(eq1);

				// (3) phi'_i element R+, f_i - phi'_i <= k_i
				RealVariable phi_prime_eq = new RealVariable(
						"phi_prime_".concat(this.toString().concat(lin.toString())));
				phi_prime_eq.setLowerBound(0.0);
				LinearConstraint eq2 = new LinearConstraint(lin.getLhsTerms(), Operator.LESS_OR_EQUAL, lin.getRhs());
				eq2.addTerm(phi_prime_eq, -1.0);
				substitute.add(eq2);

				// (4) SOS1(phi_i, phi'_i, s_i)
				List<Variable<?>> sosVars_eq3 = new ArrayList<Variable<?>>();
				sosVars_eq3.add(phi_eq);
				sosVars_eq3.add(phi_prime_eq);
				sosVars_eq3.add(s_eq);
				SOS1Constraint eq3 = new SOS1Constraint(sosVars_eq3);
				substitute.add(eq3);

				// (5) phi_i + phi'_i + s_i > e, e element R+\{0}
				LinearConstraint eq4 = new LinearConstraint(Operator.GREATER, this.epsilon);
				eq4.addTerm(phi_eq, 1.0);
				eq4.addTerm(phi_prime_eq, 1.0);
				eq4.addTerm(s_eq, 1.0);
				substitute.add(eq4);

				break;
			case NOT_EQUAL:
				// (1) s_i, s'_i element {0,1}
				BinaryVariable s_neq = new BinaryVariable("s_".concat(this.toString()).concat(lin.toString()));
				BinaryVariable s_prime_neq = new BinaryVariable(
						"s_prime_".concat(this.toString()).concat(lin.toString()));
				binary_sub.addTerm(s_neq, 1.0);

				// (2) s_i = 1 - s'_i <=> s_i + s'_i = 1
				LinearConstraint neq1 = new LinearConstraint(Operator.EQUAL, 1.0);
				neq1.addTerm(s_neq, 1.0);
				neq1.addTerm(s_prime_neq, 1.0);
				substitute.add(neq1);

				// (3) phi_i element R+, f_i + phi_i >= k_i
				RealVariable phi_neq = new RealVariable("phi_".concat(this.toString().concat(lin.toString())));
				phi_neq.setLowerBound(0.0);
				LinearConstraint neq2 = new LinearConstraint(lin.getLhsTerms(), Operator.GREATER_OR_EQUAL,
						lin.getRhs());
				neq2.addTerm(phi_neq, 1.0);
				substitute.add(neq2);

				// (3) phi'_i element R+, f_i - phi'_i <= k_i
				RealVariable phi_prime_neq = new RealVariable(
						"phi_prime_".concat(this.toString().concat(lin.toString())));
				phi_prime_neq.setLowerBound(0.0);
				LinearConstraint neq3 = new LinearConstraint(lin.getLhsTerms(), Operator.LESS_OR_EQUAL, lin.getRhs());
				neq3.addTerm(phi_prime_neq, -1.0);
				substitute.add(neq3);

				// (4) SOS1(phi_i, phi'_i, s'_i)
				List<Variable<?>> sosVars_neq4 = new ArrayList<Variable<?>>();
				sosVars_neq4.add(phi_neq);
				sosVars_neq4.add(phi_prime_neq);
				sosVars_neq4.add(s_prime_neq);
				SOS1Constraint neq4 = new SOS1Constraint(sosVars_neq4);
				substitute.add(neq4);

				// (5) phi_i + phi'_i + s'_i > e, e element R+\{0}
				LinearConstraint neq5 = new LinearConstraint(Operator.GREATER, this.epsilon);
				neq5.addTerm(phi_neq, 1.0);
				neq5.addTerm(phi_prime_neq, 1.0);
				neq5.addTerm(s_prime_neq, 1.0);
				substitute.add(neq5);

				break;
			case LESS:
				// (1) s_i, s'_i element {0,1}
				BinaryVariable s_le = new BinaryVariable("s_".concat(this.toString()).concat(lin.toString()));
				BinaryVariable s_prime_le = new BinaryVariable(
						"s_prime_".concat(this.toString()).concat(lin.toString()));
				binary_sub.addTerm(s_le, 1.0);

				// (1) s_i + s'_i = 1
				LinearConstraint le1 = new LinearConstraint(Operator.EQUAL, 1.0);
				le1.addTerm(s_le, 1.0);
				le1.addTerm(s_prime_le, 1.0);
				substitute.add(le1);

				// (2) phi_i element R, f_i - phi_i < k_i
				RealVariable phi_le = new RealVariable("phi_".concat(this.toString().concat(lin.toString())));
				LinearConstraint le2 = new LinearConstraint(lin.getLhsTerms(), Operator.LESS, lin.getRhs());
				le2.addTerm(phi_le, -1.0);
				substitute.add(le2);

				// (3) phi'_i element R, f_i + phi'_i >= k_i
				RealVariable phi_prime_le = new RealVariable(
						"phi_prime_".concat(this.toString()).concat(lin.toString()));
				LinearConstraint le3 = new LinearConstraint(lin.getLhsTerms(), Operator.GREATER_OR_EQUAL, lin.getRhs());
				le3.addTerm(phi_prime_le, 1.0);
				substitute.add(le3);

				// (4) phi_i + s_i >= 2*e
				LinearConstraint le4 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 2 * this.epsilon);
				le4.addTerm(phi_le, 1.0);
				le4.addTerm(s_le, 1.0);
				substitute.add(le4);

				// (5) phi'_i + s'_i >= 2*e
				LinearConstraint le5 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 2 * this.epsilon);
				le5.addTerm(phi_prime_le, 1.0);
				le5.addTerm(s_prime_le, 1.0);
				substitute.add(le5);

				// (6) SOS1(phi_i, s_i)
				List<Variable<?>> sosVars_le6 = new ArrayList<Variable<?>>();
				sosVars_le6.add(phi_le);
				sosVars_le6.add(s_le);
				SOS1Constraint le6 = new SOS1Constraint(sosVars_le6);
				substitute.add(le6);

				// (7) SOS1(phi'_i, s'_i)
				List<Variable<?>> sosVars_le7 = new ArrayList<Variable<?>>();
				sosVars_le7.add(phi_prime_le);
				sosVars_le7.add(s_prime_le);
				SOS1Constraint le7 = new SOS1Constraint(sosVars_le7);
				substitute.add(le7);

				break;
			case LESS_OR_EQUAL:
				// (1) s_i, s'_i element {0,1}
				BinaryVariable s_leq = new BinaryVariable("s_".concat(this.toString()).concat(lin.toString()));
				BinaryVariable s_prime_leq = new BinaryVariable(
						"s_prime_".concat(this.toString()).concat(lin.toString()));
				binary_sub.addTerm(s_leq, 1.0);

				// (1) s_i + s'_i = 1
				LinearConstraint leq1 = new LinearConstraint(Operator.EQUAL, 1.0);
				leq1.addTerm(s_leq, 1.0);
				leq1.addTerm(s_prime_leq, 1.0);
				substitute.add(leq1);

				// (2) phi_i element R, f_i - phi_i <= k_i
				RealVariable phi_leq = new RealVariable("phi_".concat(this.toString().concat(lin.toString())));
				LinearConstraint leq2 = new LinearConstraint(lin.getLhsTerms(), Operator.LESS_OR_EQUAL, lin.getRhs());
				leq2.addTerm(phi_leq, -1.0);
				substitute.add(leq2);

				// (3) phi'_i element R, f_i + phi'_i > k_i
				RealVariable phi_prime_leq = new RealVariable(
						"phi_prime_".concat(this.toString()).concat(lin.toString()));
				LinearConstraint leq3 = new LinearConstraint(lin.getLhsTerms(), Operator.GREATER, lin.getRhs());
				leq3.addTerm(phi_prime_leq, 1.0);
				substitute.add(leq3);

				// (4) phi_i + s_i >= 2*e und e element R+\{0}
				LinearConstraint leq4 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 2 * this.epsilon);
				leq4.addTerm(phi_leq, 1.0);
				leq4.addTerm(s_leq, 1.0);
				substitute.add(leq4);

				// (5) phi'_i + s'_i >= 2*e
				LinearConstraint leq5 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 2 * this.epsilon);
				leq5.addTerm(phi_prime_leq, 1.0);
				leq5.addTerm(s_prime_leq, 1.0);
				substitute.add(leq5);

				// (6) SOS1(phi_i, s_i)
				List<Variable<?>> sosVars_leq6 = new ArrayList<Variable<?>>();
				sosVars_leq6.add(phi_leq);
				sosVars_leq6.add(s_leq);
				SOS1Constraint leq6 = new SOS1Constraint(sosVars_leq6);
				substitute.add(leq6);

				// (7) SOS1(phi'_i, s'_i)
				List<Variable<?>> sosVars_leq7 = new ArrayList<Variable<?>>();
				sosVars_leq7.add(phi_prime_leq);
				sosVars_leq7.add(s_prime_leq);
				SOS1Constraint leq7 = new SOS1Constraint(sosVars_leq7);
				substitute.add(leq7);

				break;
			default:
				throw new IllegalArgumentException(
						"The or substitution for the operator of the following constraint is not implemented: "
								.concat(lin.toString()));
			}
		}
		substitute.add(binary_sub);
		return substitute;
	}
}
