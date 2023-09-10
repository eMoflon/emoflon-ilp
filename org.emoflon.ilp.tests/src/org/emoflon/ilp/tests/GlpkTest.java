package org.emoflon.ilp.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.emoflon.ilp.BinaryVariable;
import org.emoflon.ilp.IntegerVariable;
import org.emoflon.ilp.LinearConstraint;
import org.emoflon.ilp.QuadraticConstraint;
import org.emoflon.ilp.LinearFunction;
import org.emoflon.ilp.QuadraticFunction;
import org.emoflon.ilp.LinearTerm;
import org.emoflon.ilp.Objective;
import org.emoflon.ilp.ObjectiveType;
import org.emoflon.ilp.Operator;
import org.emoflon.ilp.OrConstraint;
import org.emoflon.ilp.OrVarsConstraint;
import org.emoflon.ilp.RealVariable;
import org.emoflon.ilp.SOS1Constraint;
import org.emoflon.ilp.Solver;
import org.emoflon.ilp.SolverConfig;
import org.emoflon.ilp.SolverConfig.SolverType;
import org.emoflon.ilp.SolverHelper;
import org.emoflon.ilp.SolverOutput;
import org.emoflon.ilp.Term;
import org.emoflon.ilp.Variable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GlpkTest {

	// Create variables
	BinaryVariable b1 = new BinaryVariable("b1");
	BinaryVariable b2 = new BinaryVariable("b2");
	BinaryVariable b3 = new BinaryVariable("b3");

	IntegerVariable i1 = new IntegerVariable("i1");
	IntegerVariable i2 = new IntegerVariable("i2");
	IntegerVariable i3 = new IntegerVariable("i3");

	RealVariable r1 = new RealVariable("r1");
	RealVariable r2 = new RealVariable("r2");
	RealVariable r3 = new RealVariable("r3");

	@BeforeEach
	public void setup() {
		// (Re-)set variables
		b1 = new BinaryVariable("b1");
		b2 = new BinaryVariable("b2");
		b3 = new BinaryVariable("b3");

		i1 = new IntegerVariable("i1");
		i2 = new IntegerVariable("i2");
		i3 = new IntegerVariable("i3");

		r1 = new RealVariable("r1");
		r2 = new RealVariable("r2");
		r3 = new RealVariable("r3");
	}

	@Test
	public void testGurobiMip1Example() {
		// Gurobi Mip1 example

		// Objective
		// maximize b1 + b2 + 2*b3
		Objective obj = new Objective();
		obj.setType(ObjectiveType.MAX);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(b1, 1.0);
		lin.addTerm(new LinearTerm(b2, 1.0));
		lin.addTerm(new LinearTerm(b3, 2.0));

		// Constraints
		// b1 + 2*b2 + 3*b3 <= 4
		List<Term> c1_terms = new ArrayList<Term>();
		c1_terms.add(new LinearTerm(b1, 1.0));
		c1_terms.add(new LinearTerm(b2, 2.0));
		c1_terms.add(new LinearTerm(b3, 3.0));
		LinearConstraint c1 = new LinearConstraint(c1_terms, Operator.LESS_OR_EQUAL, 4.0);

		// b1 + b2 >= 1
		LinearConstraint c2 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 1.0);
		c2.addTerm(b1, 1.0);
		c2.addTerm(new LinearTerm(b2, 1.0));

		// Model
		obj.setObjective(lin);
		obj.add(c1);
		obj.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, null, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(obj);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		solver.updateValuesFromSolution();

		assertEquals(1, obj.getVariables().get("b1").getValue());

		System.out.println("===================");
		System.out.println("Computation Result:");
		System.out.println("Value for b1: " + obj.getVariables().get("b1").getValue());
		System.out.println("Value for b2: " + obj.getVariables().get("b2").getValue());
		System.out.println("Value for b3: " + obj.getVariables().get("b3").getValue());
		System.out.println("===================");

		solver.terminate();
	}

	@Test
	public void testLinearConstrLinearObj() {
		// TODO: write test
	}

	@Test
	public void testQuadraticConstrLinearObj() {
		// Objective
		// maximize i1 + r1
		Objective obj = new Objective();
		obj.setType(ObjectiveType.MAX);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(i1, 1.0);
		lin.addTerm(r1, 1.0);

		// Constraints
		// i1^2 <= 3
		QuadraticConstraint c1 = new QuadraticConstraint(Operator.LESS_OR_EQUAL, 3.0);
		c1.addTerm(i1, i1, 1.0);

		// i1 + r1 >= 1
		LinearConstraint c2 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 1.0);
		c2.addTerm(i1, 1.0);
		c2.addTerm(r2, 1.0);

		// Model
		obj.setObjective(lin);
		obj.add(c1);
		obj.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, null, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		assertThrows(IllegalArgumentException.class, () -> {
			solver.buildILPProblem(obj);
		});

		solver.terminate();
	}

	@Test
	public void testLinearConstrQuadraticObj() {
		// Objective
		// maximize i1^2 + r1
		Objective obj = new Objective();
		obj.setType(ObjectiveType.MAX);

		QuadraticFunction lin = new QuadraticFunction();
		lin.addTerm(i1, i1, 1.0);
		lin.addTerm(r1, 1.0);

		// Constraints
		// i1 <= 3
		LinearConstraint c1 = new LinearConstraint(Operator.LESS_OR_EQUAL, 3.0);
		c1.addTerm(i1, 1.0);

		// i1 + r1 >= 1
		LinearConstraint c2 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 1.0);
		c2.addTerm(i1, 1.0);
		c2.addTerm(r2, 1.0);

		// Model
		obj.setObjective(lin);
		obj.add(c1);
		obj.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, null, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		assertThrows(IllegalArgumentException.class, () -> {
			solver.buildILPProblem(obj);
		});

		solver.terminate();
	}

	@Test
	public void testQuadraticConstrQuadraticObj() {
		// Objective
		// maximize i1^2 + r1
		Objective obj = new Objective();
		obj.setType(ObjectiveType.MAX);

		QuadraticFunction lin = new QuadraticFunction();
		lin.addTerm(i1, i1, 1.0);
		lin.addTerm(r1, 1.0);

		// Constraints
		// i1^2 <= 3
		QuadraticConstraint c1 = new QuadraticConstraint(Operator.LESS_OR_EQUAL, 3.0);
		c1.addTerm(i1, i1, 1.0);

		// i1 + r1 >= 1
		LinearConstraint c2 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 1.0);
		c2.addTerm(i1, 1.0);
		c2.addTerm(r2, 1.0);

		// Model
		obj.setObjective(lin);
		obj.add(c1);
		obj.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, null, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		assertThrows(IllegalArgumentException.class, () -> {
			solver.buildILPProblem(obj);
		});

		solver.terminate();
	}

	@Test
	public void testBasicSOS1Constraint() {
		// TODO: write test
		// Objective
		Objective obj = new Objective();
		obj.setType(ObjectiveType.MIN);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(b1, 1.0);
		lin.addTerm(b2, 1.0);
		lin.addTerm(b3, 1.0);

		// Constraints
		// b1 >= 1
		LinearConstraint c1 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 1.0);
		c1.addTerm(b1, 1.0);

		// 2*b2 <= 4
		LinearConstraint c2 = new LinearConstraint(Operator.LESS_OR_EQUAL, 5.0);
		c2.addTerm(b2, 2.0);

		// SOS1
		List<Variable<?>> sosVars = new ArrayList<Variable<?>>();
		sosVars.add(b1);
		sosVars.add(b2);
		SOS1Constraint sos1 = new SOS1Constraint(sosVars);

		// Model
		obj.setObjective(lin);
		obj.add(c1);
		obj.add(c2);
		obj.add(sos1);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, null, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(obj);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		solver.updateValuesFromSolution();

		assertEquals(1, obj.getVariables().get("b1").getValue());
		assertEquals(0, obj.getVariables().get("b2").getValue());
		assertEquals(0, obj.getVariables().get("b3").getValue());

		solver.terminate();
	}

	@Test
	public void testBasicOrConstraint() {
		// Objective
		Objective obj = new Objective();
		obj.setType(ObjectiveType.MAX);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(b1, 1.0);

		// Constraints
		// 5*b1 + b2 >= 1
		LinearConstraint c1 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 1.0);
		c1.addTerm(b1, 5.0);
		c1.addTerm(b2, 1.0);

		// b2 <= 3
		LinearConstraint c2 = new LinearConstraint(Operator.LESS_OR_EQUAL, 3.0);
		c2.addTerm(b2, 1.0);

		// Or
		OrConstraint or1 = new OrConstraint();
		or1.addConstraint(c1);
		or1.addConstraint(c2);

		// Model
		obj.setObjective(lin);
		obj.add(or1);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, true, -10, 10, false,
				false, null, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(obj);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		solver.updateValuesFromSolution();

		assertEquals(1, obj.getVariables().get("b1").getValue());
		assertEquals(1, obj.getVariables().get("b2").getValue());

		solver.terminate();
	}

	@Test
	public void testOrVarsConstraint() {
		// Objective
		Objective obj = new Objective();
		obj.setType(ObjectiveType.MAX);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(b1, 1.0);

		// Constraints
		// 5*b1 + b2 >= 1
		LinearConstraint c1 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 1.0);
		c1.addTerm(b1, 5.0);
		c1.addTerm(b2, 1.0);

		// b2 <= 3
		LinearConstraint c2 = new LinearConstraint(Operator.LESS_OR_EQUAL, 3.0);
		c2.addTerm(b2, 1.0);

		// Or
		OrVarsConstraint or1 = new OrVarsConstraint(b3);
		or1.addVariable(b1);
		or1.addVariable(b2);

		// Model
		obj.setObjective(lin);
		obj.add(or1);
		obj.add(c1);
		obj.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, null, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		assertThrows(IllegalArgumentException.class, () -> {
			solver.buildILPProblem(obj);
		});

		solver.terminate();
	}

	@Test
	public void testOperatorConversion() {
		// Objective
		// max b1
		Objective obj = new Objective();
		obj.setType(ObjectiveType.MAX);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(b1, 1.0);

		// Constraints
		// i1 != 1
		LinearConstraint c1 = new LinearConstraint(Operator.NOT_EQUAL, 1.0);
		c1.addTerm(i1, 1.0);

		// r1 > 1
		LinearConstraint c2 = new LinearConstraint(Operator.GREATER, 1.0);
		c2.addTerm(r1, 1.0);

		// i2 < 4
		LinearConstraint c3 = new LinearConstraint(Operator.LESS, 4);
		c3.addTerm(i2, 1.0);

		// Model
		obj.setObjective(lin);
		obj.add(c1);
		obj.add(c2);
		obj.add(c3);

		assertEquals(3, obj.getConstraintCount());

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, null, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(obj);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		solver.updateValuesFromSolution();

		assertEquals(5, obj.getConstraintCount());

		assertNotEquals(1, obj.getVariables().get("i1").getValue());
		assertTrue(obj.getVariables().get("r1").getValue().doubleValue() > 1);
		assertTrue(obj.getVariables().get("i2").getValue().intValue() < 4);

		solver.terminate();
	}
}
