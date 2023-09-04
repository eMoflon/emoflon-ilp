package org.emoflon.ilp.tests;

//import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.emoflon.ilp.*;
import org.emoflon.ilp.SolverConfig.SolverType;

public class GurobiTest {

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
	public void testGurobiExample() {
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
		SolverConfig config = new SolverConfig(SolverType.GUROBI, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, null, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(obj);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		Objective result = solver.updateValuesFromSolution();

		assertEquals(1, result.getVariables().get("b1").getValue());

		System.out.println("===================");
		System.out.println("Computation Result:");
		System.out.println("Value for b1: " + result.getVariables().get("b1").getValue());
		System.out.println("Value for b2: " + result.getVariables().get("b2").getValue());
		System.out.println("Value for b3: " + result.getVariables().get("b3").getValue());
		System.out.println("===================");

		solver.terminate();
	}

	@Test
	public void testLinearConstrLinearObj() {
		// TODO: write test
	}

	@Test
	public void testQuadraticConstrLinearObj() {
		// TODO: write test
	}

	@Test
	public void testLinearConstrQuadraticObj() {
		// TODO: write test
	}

	@Test
	public void testQuadraticConstrQuadraticObj() {
		// TODO: write test
	}

	@Test
	public void testLessLinearConstraint() {
		// TODO: write test
	}

	@Test
	public void testGreaterLinearConstraint() {
		// TODO: write test
	}

	@Test
	public void testNotEqualLinearConstraint() {
		// TODO: write test
	}

	@Test
	public void testLessQuadraticConstraint() {
		// TODO: write test
	}

	@Test
	public void testGreaterQuadraticConstraint() {
		// TODO: write test
	}

	@Test
	public void testNotEqualQuadraticConstraint() {
		// TODO: write test
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
		SolverConfig config = new SolverConfig(SolverType.GUROBI, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, null, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(obj);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		Objective result = solver.updateValuesFromSolution();

		assertEquals(1, result.getVariables().get("b1").getValue());
		assertEquals(0, result.getVariables().get("b2").getValue());
		assertEquals(0, result.getVariables().get("b3").getValue());

		System.out.println("===================");
		System.out.println("Computation Result:");
		System.out.println("Value for b1: " + result.getVariables().get("b1").getValue());
		System.out.println("Value for b2: " + result.getVariables().get("b2").getValue());
		System.out.println("Value for b3: " + result.getVariables().get("b3").getValue());
		System.out.println("===================");

		solver.terminate();
	}

	@Test
	public void testCompareSOS1toSubstitution() {
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
		SolverConfig config = new SolverConfig(SolverType.GUROBI, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, null, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(obj);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		Objective result = solver.updateValuesFromSolution();

		solver.terminate();

		// Now with substitution instead of Gurobi SOS1 Constraints

		BinaryVariable b1_sub = new BinaryVariable("b1_sub");
		BinaryVariable b2_sub = new BinaryVariable("b2_sub");
		BinaryVariable b3_sub = new BinaryVariable("b3_sub");

		// Objective
		Objective obj_sub = new Objective();
		obj_sub.setType(ObjectiveType.MIN);

		LinearFunction lin_sub = new LinearFunction();
		lin_sub.addTerm(b1_sub, 1.0);
		lin_sub.addTerm(b2_sub, 1.0);
		lin_sub.addTerm(b3_sub, 1.0);

		// Constraints
		// b1 >= 1
		LinearConstraint c1_sub = new LinearConstraint(Operator.GREATER_OR_EQUAL, 1.0);
		c1_sub.addTerm(b1_sub, 1.0);

		// 2*b2 <= 4
		LinearConstraint c2_sub = new LinearConstraint(Operator.LESS_OR_EQUAL, 5.0);
		c2_sub.addTerm(b2_sub, 2.0);

		// SOS1
		List<Variable<?>> sosVars_sub = new ArrayList<Variable<?>>();
		sosVars_sub.add(b1_sub);
		sosVars_sub.add(b2_sub);
		SOS1Constraint sos1_sub = new SOS1Constraint(sosVars_sub);

		List<LinearConstraint> substitution = sos1_sub.convert();

		// Model
		obj_sub.setObjective(lin_sub);
		obj_sub.add(c1_sub);
		obj_sub.add(c2_sub);
		substitution.forEach(it -> obj_sub.add(it));

		// Optimize
		SolverConfig config_sub = new SolverConfig(SolverType.GUROBI, false, 0.0, true, 42, false, 0.0, false, 0, 0,
				false, false, null, false, null);
		Solver solver_sub = (new SolverHelper(config_sub)).getSolver();
		solver_sub.buildILPProblem(obj_sub);
		SolverOutput out_sub = solver_sub.solve();
		System.out.println(out_sub.toString());
		Objective result_sub = solver_sub.updateValuesFromSolution();

		assertEquals(result.getVariables().get("b1").getValue(), result_sub.getVariables().get("b1_sub").getValue());
		assertEquals(result.getVariables().get("b2").getValue(), result_sub.getVariables().get("b2_sub").getValue());
		assertEquals(result.getVariables().get("b3").getValue(), result_sub.getVariables().get("b3_sub").getValue());

		solver.terminate();
	}

	@Test
	public void testBasicOrConstraint() {
		// TODO: minimal Or Constraint example

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
		SolverConfig config = new SolverConfig(SolverType.GUROBI, false, 0.0, true, 42, false, 0.0, true, -10, 10, false,
				false, null, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(obj);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		Objective result = solver.updateValuesFromSolution();

		assertEquals(1, result.getVariables().get("b1").getValue());
		assertEquals(1, result.getVariables().get("b2").getValue());

		solver.terminate();
	}

	@Test
	public void testBasicOrVarsConstraint() {
		// TODO: minimal Or Constraint example

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
		SolverConfig config = new SolverConfig(SolverType.GUROBI, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, null, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(obj);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		Objective result = solver.updateValuesFromSolution();

		assertEquals(1, result.getVariables().get("b1").getValue());
		assertEquals(1, result.getVariables().get("b2").getValue());
		assertEquals(1, result.getVariables().get("b3").getValue());

		solver.terminate();
	}
}
