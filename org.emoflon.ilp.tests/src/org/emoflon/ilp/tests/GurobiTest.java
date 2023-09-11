package org.emoflon.ilp.tests;

//import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

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
		// Objective
		// maximize i1 + 2* (r2 - i1)
		Objective obj = new Objective();
		obj.setType(ObjectiveType.MAX);

		LinearFunction nested = new LinearFunction();
		nested.addTerm(r2, 1.0);
		nested.addTerm(i1, -1.0);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(i1, 1.0);
		lin.addNestedFunction(nested, 2.0);

		// Constraints
		// i1 >= 5
		LinearConstraint c1 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 5.0);
		c1.addTerm(i1, 1.0);

		// r2 <= 100
		LinearConstraint c2 = new LinearConstraint(Operator.LESS_OR_EQUAL, 100.0);
		c2.addTerm(r2, 1.0);

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
		solver.updateValuesFromSolution();

		assertEquals(195.0, out.getObjVal(), 0.0001);
		assertEquals(5, obj.getVariables().get("i1").getValue());
		assertEquals(100.0, obj.getVariables().get("r2").getValue());

		solver.terminate();
	}

	@Test
	public void testQuadraticConstrLinearObj() {
		// Objective
		// maximize i1 + 2* (r2 - i1)
		Objective obj = new Objective();
		obj.setType(ObjectiveType.MAX);

		LinearFunction nested = new LinearFunction();
		nested.addTerm(r2, 1.0);
		nested.addTerm(i1, -1.0);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(i1, 1.0);
		lin.addNestedFunction(nested, 2.0);

		// Constraints
		// i1 >= 5
		LinearConstraint c1 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 5.0);
		c1.addTerm(i1, 1.0);

		// r2^2 <= 100
		QuadraticConstraint c2 = new QuadraticConstraint(Operator.LESS_OR_EQUAL, 100.0);
		c2.addTerm(r2, r2, 1.0);

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
		solver.updateValuesFromSolution();

		assertEquals(15.0, out.getObjVal(), 0.0001);
		assertEquals(5, obj.getVariables().get("i1").getValue());
		assertEquals(10.0, obj.getVariables().get("r2").getValue());

		solver.terminate();
	}

	@Test
	public void testLinearConstrQuadraticObj() {
		// Objective
		// maximize i1 + 2* (r2 - i1^2)
		Objective obj = new Objective();
		obj.setType(ObjectiveType.MAX);

		QuadraticFunction nested = new QuadraticFunction();
		nested.addTerm(r2, 1.0);
		nested.addTerm(i1, i1, -1.0);

		QuadraticFunction lin = new QuadraticFunction();
		lin.addTerm(i1, 1.0);
		lin.addNestedFunction(nested, 2.0);

		// Constraints
		// i1 >= 5
		LinearConstraint c1 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 5.0);
		c1.addTerm(i1, 1.0);

		// r2 <= 100
		LinearConstraint c2 = new LinearConstraint(Operator.LESS_OR_EQUAL, 100.0);
		c2.addTerm(r2, 1.0);

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
		solver.updateValuesFromSolution();

		assertEquals(155.0, out.getObjVal(), 0.0001);
		assertEquals(5, obj.getVariables().get("i1").getValue());
		assertEquals(100.0, obj.getVariables().get("r2").getValue());

		solver.terminate();
	}

	@Test
	public void testQuadraticConstrQuadraticObj() {
		// Objective
		// maximize i1 + 2* (r2 - i1^2)
		Objective obj = new Objective();
		obj.setType(ObjectiveType.MAX);

		QuadraticFunction nested = new QuadraticFunction();
		nested.addTerm(r2, 1.0);
		nested.addTerm(i1, i1, -1.0);

		QuadraticFunction lin = new QuadraticFunction();
		lin.addTerm(i1, 1.0);
		lin.addNestedFunction(nested, 2.0);

		// Constraints
		// i1 >= 5
		LinearConstraint c1 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 5.0);
		c1.addTerm(i1, 1.0);

		// r2^2 <= 100
		QuadraticConstraint c2 = new QuadraticConstraint(Operator.LESS_OR_EQUAL, 100.0);
		c2.addTerm(r2, r2, 1.0);

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
		solver.updateValuesFromSolution();

		assertEquals(-25.0, out.getObjVal(), 0.0001);
		assertEquals(5, obj.getVariables().get("i1").getValue());
		assertEquals(10.0, obj.getVariables().get("r2").getValue());

		solver.terminate();
	}

	@Test
	public void testLessLinearConstraint() {
		// Objective
		// maximize i1 + 2* (i2 - i1)
		Objective obj = new Objective();
		obj.setType(ObjectiveType.MAX);

		LinearFunction nested = new LinearFunction();
		nested.addTerm(i2, 1.0);
		nested.addTerm(i1, -1.0);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(i1, 1.0);
		lin.addNestedFunction(nested, 2.0);

		// Constraints
		// i1 >= 5
		LinearConstraint c1 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 5.0);
		c1.addTerm(i1, 1.0);

		// i2 < 100
		LinearConstraint c2 = new LinearConstraint(Operator.LESS, 100.0);
		c2.addTerm(i2, 1.0);

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
		solver.updateValuesFromSolution();

		assertEquals(193.0, out.getObjVal(), 0.0001);
		assertEquals(5, obj.getVariables().get("i1").getValue());
		assertEquals(99, obj.getVariables().get("i2").getValue());

		solver.terminate();
	}

	@Test
	public void testGreaterLinearConstraint() {
		// Objective
		// maximize i1 + 2* (r2 - i1)
		Objective obj = new Objective();
		obj.setType(ObjectiveType.MAX);

		LinearFunction nested = new LinearFunction();
		nested.addTerm(r2, 1.0);
		nested.addTerm(i1, -1.0);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(i1, 1.0);
		lin.addNestedFunction(nested, 2.0);

		// Constraints
		// i1 > 5
		LinearConstraint c1 = new LinearConstraint(Operator.GREATER, 5.0);
		c1.addTerm(i1, 1.0);

		// r2 <= 100
		LinearConstraint c2 = new LinearConstraint(Operator.LESS_OR_EQUAL, 100.0);
		c2.addTerm(r2, 1.0);

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
		solver.updateValuesFromSolution();

		assertEquals(194.0, out.getObjVal(), 0.0001);
		assertEquals(6, obj.getVariables().get("i1").getValue());
		assertEquals(100.0, obj.getVariables().get("r2").getValue());

		solver.terminate();
	}

	@Test
	public void testNotEqualLinearConstraint() {
		// TODO: fails, != conversion not correct
		// Objective
		// maximize i1 + 2* (r2 - i1)
		Objective obj = new Objective();
		obj.setType(ObjectiveType.MAX);

		LinearFunction nested = new LinearFunction();
		nested.addTerm(r2, 1.0);
		nested.addTerm(i1, -1.0);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(i1, 1.0);
		lin.addNestedFunction(nested, 2.0);

		// Constraints
		// i1 != 5 (lower bound)
		i1.setLowerBound(5);
		LinearConstraint c1 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 5.0);
		c1.addTerm(i1, 1.0);

		// r2 != 100 (upper bound)
		r2.setUpperBound(100.0);
		LinearConstraint c2 = new LinearConstraint(Operator.NOT_EQUAL, 100.0);
		c2.addTerm(r2, 1.0);

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
		solver.updateValuesFromSolution();

		// assertEquals(195.0, out.getObjVal(), 0.0001);
		assertEquals(4, obj.getVariables().get("i1").getValue());
		assertEquals(99.0, obj.getVariables().get("r2").getValue().doubleValue(), 1.0);

		solver.terminate();
	}

	@Test
	public void testLessQuadraticConstraint() {
		// Objective
		// maximize i1 + 2* (i2 - i1)
		Objective obj = new Objective();
		obj.setType(ObjectiveType.MAX);

		LinearFunction nested = new LinearFunction();
		nested.addTerm(i2, 1.0);
		nested.addTerm(i1, -1.0);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(i1, 1.0);
		lin.addNestedFunction(nested, 2.0);

		// Constraints
		// i1 >= 5
		LinearConstraint c1 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 5.0);
		c1.addTerm(i1, 1.0);

		// i2^2 < 100
		QuadraticConstraint c2 = new QuadraticConstraint(Operator.LESS, 100.0);
		c2.addTerm(i2, i2, 1.0);

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
		solver.updateValuesFromSolution();

		assertEquals(13, out.getObjVal(), 0.0001);
		assertEquals(5, obj.getVariables().get("i1").getValue());
		assertEquals(9, obj.getVariables().get("i2").getValue());

		solver.terminate();
	}

	@Test
	public void testGreaterQuadraticConstraint() {
		// Objective
		// maximize i1 + 2* (i2 - i1)
		Objective obj = new Objective();
		obj.setType(ObjectiveType.MAX);

		LinearFunction nested = new LinearFunction();
		nested.addTerm(i2, 1.0);
		nested.addTerm(i1, -1.0);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(i1, 1.0);
		lin.addNestedFunction(nested, 2.0);

		// Constraints
		// i1^2 > 25, i1 >= 0
		i1.setLowerBound(0);
		QuadraticConstraint c1 = new QuadraticConstraint(Operator.GREATER, 25.0);
		c1.addTerm(i1, i1, 1.0);

		// i2 <= 100
		LinearConstraint c2 = new LinearConstraint(Operator.LESS_OR_EQUAL, 100.0);
		c2.addTerm(i2, 1.0);

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
		solver.updateValuesFromSolution();

		assertEquals(194.0, out.getObjVal(), 0.0001);
		assertEquals(6, obj.getVariables().get("i1").getValue());
		assertEquals(100, obj.getVariables().get("i2").getValue());

		solver.terminate();
	}

	@Test
	public void testNotEqualQuadraticConstraint() {
		// TODO: Not Equal fails

		// Objective
		// maximize i1 + 2* (i2 - i1)
		Objective obj = new Objective();
		obj.setType(ObjectiveType.MAX);

		LinearFunction nested = new LinearFunction();
		nested.addTerm(i2, 1.0);
		nested.addTerm(i1, -1.0);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(i1, 1.0);
		lin.addNestedFunction(nested, 2.0);

		// Constraints
		// i1 >= 5
		LinearConstraint c1 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 5.0);
		c1.addTerm(i1, 1.0);

		// i2^2 != 100 (upper bound), i2 > 0
		i2.setLowerBound(0);
		i2.setUpperBound(100);
		QuadraticConstraint c2 = new QuadraticConstraint(Operator.NOT_EQUAL, 100.0);
		c2.addTerm(i2, i2, 1.0);

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
		solver.updateValuesFromSolution();

		// assertEquals(13, out.getObjVal(), 0.0001);
		assertEquals(5, obj.getVariables().get("i1").getValue());
		assertEquals(9, obj.getVariables().get("i2").getValue());

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
		SolverConfig config = new SolverConfig(SolverType.GUROBI, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, null, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(obj);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		solver.updateValuesFromSolution();

		assertEquals(1, obj.getVariables().get("b1").getValue());
		assertEquals(0, obj.getVariables().get("b2").getValue());
		assertEquals(0, obj.getVariables().get("b3").getValue());

		System.out.println("===================");
		System.out.println("Computation Result:");
		System.out.println("Value for b1: " + obj.getVariables().get("b1").getValue());
		System.out.println("Value for b2: " + obj.getVariables().get("b2").getValue());
		System.out.println("Value for b3: " + obj.getVariables().get("b3").getValue());
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
		solver.updateValuesFromSolution();

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
		solver_sub.updateValuesFromSolution();

		assertEquals(obj.getVariables().get("b1").getValue(), obj_sub.getVariables().get("b1_sub").getValue());
		assertEquals(obj.getVariables().get("b2").getValue(), obj_sub.getVariables().get("b2_sub").getValue());
		assertEquals(obj.getVariables().get("b3").getValue(), obj_sub.getVariables().get("b3_sub").getValue());

		solver.terminate();
	}

	@Test
	public void testGurobiSOS() {
		// Objective
		Objective obj = new Objective();
		obj.setType(ObjectiveType.MAX);

		// In Gurobi example without objective
		// Here objective to get max values for r2 and r3, zero for r1
		LinearFunction lin = new LinearFunction();
		lin.addTerm(r1, 1.0);
		lin.addTerm(r2, 1.0);
		lin.addTerm(r3, 1.0);

		// SOS1: x0=0 or x1=0
		// here: SOS1(r1, r2)

		List<Variable<?>> sosVars1 = new ArrayList<Variable<?>>();
		sosVars1.add(r1);
		sosVars1.add(r2);
		SOS1Constraint sos1 = new SOS1Constraint(sosVars1);
		double[] weights = { 1, 2 };
		sos1.setWeights(weights);

		// SOS1: x0=0 or x2=0
		// here: SOS1(r1, r3)

		List<Variable<?>> sosVars2 = new ArrayList<Variable<?>>();
		sosVars2.add(r1);
		sosVars2.add(r3);
		SOS1Constraint sos2 = new SOS1Constraint(sosVars2);
		sos2.setWeights(weights);

		// Model
		obj.setObjective(lin);
		obj.add(sos1);
		obj.add(sos2);
		// obj.add(l1);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.GUROBI, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, null, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(obj);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		solver.updateValuesFromSolution();

		assertEquals(20000.0, out.getObjVal(), 0.0001);
		assertEquals(0.0, obj.getVariables().get("r1").getValue().doubleValue(), 0.0001);
		assertEquals(10000.0, obj.getVariables().get("r2").getValue().doubleValue(), 0.0001);
		assertEquals(10000.0, obj.getVariables().get("r3").getValue().doubleValue(), 0.0001);

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
		SolverConfig config = new SolverConfig(SolverType.GUROBI, false, 0.0, true, 42, false, 0.0, true, -10, 10,
				false, false, null, false, null);
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
		solver.updateValuesFromSolution();

		assertEquals(1, obj.getVariables().get("b1").getValue());
		assertEquals(1, obj.getVariables().get("b2").getValue());
		assertEquals(1, obj.getVariables().get("b3").getValue());

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
		lin.addTerm(i1, 1.0);
		lin.addTerm(r1, -1.0);
		lin.addTerm(i1, 1.0);

		// Constraints
		// i1 != 10000
		LinearConstraint c1 = new LinearConstraint(Operator.NOT_EQUAL, i1.getUpperBound());
		c1.addTerm(i1, 1.0);
		c1.setEpsilon(1.0);

		// r1 > 1
		LinearConstraint c2 = new LinearConstraint(Operator.GREATER, 1.0);
		c2.addTerm(r1, 1.0);

		// i2^2 < 4
		QuadraticConstraint c3 = new QuadraticConstraint(Operator.LESS, 4);
		c3.addTerm(i2, i2, 1.0);

		// Model
		obj.setObjective(lin);
		obj.add(c1);
		obj.add(c2);
		obj.add(c3);

		assertEquals(3, obj.getConstraintCount());

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.GUROBI, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, null, true, "/Users/luise/Projektseminar/NOT_error.lp");
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(obj);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		solver.updateValuesFromSolution();

		System.out.println("===================");
		System.out.println("Computation Result:");
		System.out.println("Value for i1: " + obj.getVariables().get("i1").getValue());
		System.out.println("Value for r1: " + obj.getVariables().get("r1").getValue());
		System.out.println("Value for i2: " + obj.getVariables().get("i2").getValue());
		System.out.println(
				"Value for psi: " + obj.getVariables().get("psi_org.emoflon.ilp.LinearConstraint@76536c53").getValue());
		System.out.println("Value for psi_prime: "
				+ obj.getVariables().get("psiPrime_org.emoflon.ilp.LinearConstraint@76536c53").getValue());
		System.out.println("===================");

		assertEquals(5, obj.getConstraintCount());

		assertNotEquals(i1.getUpperBound(), obj.getVariables().get("i1").getValue());
		assertTrue(obj.getVariables().get("r1").getValue().doubleValue() > 1);
		assertTrue(obj.getVariables().get("i2").getValue().intValue() < 2
				&& obj.getVariables().get("i2").getValue().intValue() > -2);

		solver.terminate();
	}
}
