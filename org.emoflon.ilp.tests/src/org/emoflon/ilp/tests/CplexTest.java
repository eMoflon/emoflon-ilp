package org.emoflon.ilp.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.emoflon.ilp.*;
import org.emoflon.ilp.SolverConfig.SolverType;

public class CplexTest {

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
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

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
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.CPLEX, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		solver.updateValuesFromSolution();

		assertEquals(1, problem.getVariables().get("b1").getValue());
		assertEquals(3.0, out.getObjVal(), 0.1);

		/*
		 * System.out.println("===================");
		 * System.out.println(out.toString());
		 * System.out.println("Computation Result:"); for (String varName :
		 * problem.getVariables().keySet()) { System.out.println("Value for " + varName
		 * + ": " + problem.getVariables().get(varName).getValue()); }
		 * System.out.println("===================");
		 */

		solver.terminate();
	}

	@Test
	public void testLinearConstrLinearObj() {
		// Objective
		// maximize i1 + 2* (r2 - i1)
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

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
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.CPLEX, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		solver.updateValuesFromSolution();

		assertEquals(195.0, out.getObjVal(), 0.0001);
		assertEquals(5, problem.getVariables().get("i1").getValue());
		assertEquals(100.0, problem.getVariables().get("r2").getValue());

		solver.terminate();
	}

	@Test
	public void testQuadraticConstrLinearObj() {
		// Objective
		// maximize i1 + 2* (r2 - i1)
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

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
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.CPLEX, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		solver.updateValuesFromSolution();

		assertEquals(15.0, out.getObjVal(), 0.0001);
		assertEquals(5, problem.getVariables().get("i1").getValue());
		assertEquals(10.0, problem.getVariables().get("r2").getValue());

		solver.terminate();
	}

	@Test
	public void testDuplicateTermQuadraticConstr() {
		// Objective
		// maximize i1 + 2* (r2 - i1)
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

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

		// r2^2 + r^2 <= 200
		QuadraticConstraint c2 = new QuadraticConstraint(Operator.LESS_OR_EQUAL, 200.0);
		c2.addTerm(r2, r2, 1.0);
		c2.addTerm(r2, r2, 1.0);

		// Model
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.CPLEX, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		solver.updateValuesFromSolution();

		assertEquals(15.0, out.getObjVal(), 0.0001);
		assertEquals(5, problem.getVariables().get("i1").getValue());
		assertEquals(10.0, problem.getVariables().get("r2").getValue());

		solver.terminate();
	}

	@Test
	public void testLinearConstrQuadraticObj() {
		// Objective
		// maximize i1 + 2* (r2 - i1^2)
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

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
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.CPLEX, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		solver.updateValuesFromSolution();

		assertEquals(155.0, out.getObjVal(), 0.0001);
		assertEquals(5, problem.getVariables().get("i1").getValue());
		assertEquals(100.0, problem.getVariables().get("r2").getValue());

		solver.terminate();
	}

	@Test
	public void testQuadraticConstrQuadraticObj() {
		// Objective
		// maximize i1 + 2* (r2 - i1^2)
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

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
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.CPLEX, false, 0.0, true, 42, false, 1.0E-6, false, 0, 0,
				false, false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		solver.updateValuesFromSolution();

		assertEquals(-25.0, out.getObjVal(), 0.0001);
		assertEquals(5, problem.getVariables().get("i1").getValue());
		assertEquals(10.0, problem.getVariables().get("r2").getValue().doubleValue(), 0.0001);

		solver.terminate();
	}

	@Test
	public void testLessLinearConstraint() {
		// Objective
		// maximize i1 + 2* (i2 - i1)
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

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
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.CPLEX, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		solver.updateValuesFromSolution();

		assertEquals(193.0, out.getObjVal(), 0.0001);
		assertEquals(5, problem.getVariables().get("i1").getValue());
		assertEquals(99, problem.getVariables().get("i2").getValue());

		solver.terminate();
	}

	@Test
	public void testGreaterLinearConstraint() {
		// Objective
		// maximize i1 + 2* (r2 - i1)
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

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
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.CPLEX, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		solver.updateValuesFromSolution();

		assertEquals(194.0, out.getObjVal(), 0.0001);
		assertEquals(6, problem.getVariables().get("i1").getValue());
		assertEquals(100.0, problem.getVariables().get("r2").getValue());

		solver.terminate();
	}

	@Test
	public void testNotEqualLinearConstraint() {
		// Objective
		// maximize i1 + 2* (r2 - i1)
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

		LinearFunction nested = new LinearFunction();
		nested.addTerm(r2, 1.0);
		nested.addTerm(i1, -1.0);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(i1, 1.0);
		lin.addNestedFunction(nested, 2.0);

		// Constraints
		// i1 != 5 (lower bound)
		i1.setLowerBound(5);
		LinearConstraint c1 = new LinearConstraint(Operator.NOT_EQUAL, 5.0);
		c1.addTerm(i1, 1.0);

		// r2 != 100 (upper bound)
		r2.setUpperBound(100.0);
		LinearConstraint c2 = new LinearConstraint(Operator.NOT_EQUAL, 100.0);
		c2.addTerm(r2, 1.0);

		// Model
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.CPLEX, false, 0.0, true, 42, false, 1.0E-4, false, 0, 0,
				false, false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		solver.updateValuesFromSolution();

		assertEquals(193.99, out.getObjVal(), 0.01);
		assertEquals(6, problem.getVariables().get("i1").getValue());
		assertEquals(99.99, problem.getVariables().get("r2").getValue().doubleValue(), 0.01);

		solver.terminate();
	}

	@Test
	public void testLessQuadraticConstraint() {
		// Objective
		// maximize i1 + 2* (i2 - i1)
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

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
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.CPLEX, false, 0.0, true, 42, true, 1.0E-6, false, 0, 0, false,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		solver.updateValuesFromSolution();

		assertEquals(13, out.getObjVal(), 0.0001);
		assertEquals(5, problem.getVariables().get("i1").getValue());
		assertEquals(9, problem.getVariables().get("i2").getValue());

		solver.terminate();
	}

	@Test
	public void testGreaterQuadraticConstraint() {
		// Objective
		// maximize i1 + 2* (i2 - i1)
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

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
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.CPLEX, false, 0.0, true, 42, true, 1.0E-6, false, 0, 0, false,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		solver.updateValuesFromSolution();

		assertEquals(194.0, out.getObjVal(), 0.0001);
		assertEquals(6, problem.getVariables().get("i1").getValue());
		assertEquals(100, problem.getVariables().get("i2").getValue());

		solver.terminate();
	}

	@Test
	@Disabled
	public void testNotEqualQuadraticConstraint() {
		// kein funktionierendes Beispiel eingefallen (ist nicht positiv semidefinit)
	}

	@Test
	public void testBasicSOS1Constraint() {
		// Objective
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MIN);

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
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);
		problem.add(sos1);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.CPLEX, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		solver.solve();
		solver.updateValuesFromSolution();

		assertEquals(1, problem.getVariables().get("b1").getValue());
		assertEquals(0, problem.getVariables().get("b2").getValue());
		assertEquals(0, problem.getVariables().get("b3").getValue());

		solver.terminate();
	}

	@Test
	public void testCompareSOS1toSubstitution() {
		// Objective
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MIN);

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
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);
		problem.add(sos1);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.CPLEX, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		solver.solve();
		solver.updateValuesFromSolution();

		solver.terminate();

		// Now with substitution instead of Gurobi SOS1 Constraints

		BinaryVariable b1_sub = new BinaryVariable("b1_sub");
		BinaryVariable b2_sub = new BinaryVariable("b2_sub");
		BinaryVariable b3_sub = new BinaryVariable("b3_sub");

		// Objective
		Problem obj_sub = new Problem();
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
		Solver solver_sub = (new SolverHelper(config)).getSolver();
		solver_sub.buildILPProblem(obj_sub);
		solver_sub.solve();
		solver_sub.updateValuesFromSolution();

		assertEquals(problem.getVariables().get("b1").getValue(), obj_sub.getVariables().get("b1_sub").getValue());
		assertEquals(problem.getVariables().get("b2").getValue(), obj_sub.getVariables().get("b2_sub").getValue());
		assertEquals(problem.getVariables().get("b3").getValue(), obj_sub.getVariables().get("b3_sub").getValue());

		solver.terminate();
	}

	@Test
	public void testNonBinVarSOS1Constraint() {
		// Objective
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MIN);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(i1, 1.0);
		lin.addTerm(i2, 1.0);
		lin.addTerm(r1, 1.0);

		// Constraints
		// i1 >= 20
		LinearConstraint c1 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 20.0);
		c1.addTerm(i1, 1.0);

		// 2*i2 <= 4
		LinearConstraint c2 = new LinearConstraint(Operator.LESS_OR_EQUAL, 4.0);
		c2.addTerm(i2, 2.0);

		// 5*i1 - r1 >= 4
		LinearConstraint c3 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 4.0);
		c3.addTerm(i1, 5.0);
		c3.addTerm(r1, -1.0);

		// SOS1
		List<Variable<?>> sosVars = new ArrayList<Variable<?>>();
		sosVars.add(i1);
		sosVars.add(i2);
		sosVars.add(r1);
		SOS1Constraint sos1 = new SOS1Constraint(sosVars);

		// Model
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);
		problem.add(sos1);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.CPLEX, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		solver.solve();
		solver.updateValuesFromSolution();

		assertEquals(20, problem.getVariables().get("i1").getValue());
		assertEquals(0, problem.getVariables().get("i2").getValue());
		assertEquals(0.0, problem.getVariables().get("r1").getValue());

		solver.terminate();
	}

	@Test
	public void testCplexSOS() {
		// Objective
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

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
		double[] weights = { 1, 2 };
		SOS1Constraint sos1 = new SOS1Constraint(sosVars1, weights);

		// SOS1: x0=0 or x2=0
		// here: SOS1(r1, r3)

		List<Variable<?>> sosVars2 = new ArrayList<Variable<?>>();
		sosVars2.add(r1);
		sosVars2.add(r3);
		SOS1Constraint sos2 = new SOS1Constraint(sosVars2, weights);

		// Model
		problem.setObjective(lin);
		problem.add(sos1);
		problem.add(sos2);
		// problem.add(l1);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.CPLEX, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		solver.updateValuesFromSolution();

		assertEquals(20000.0, out.getObjVal(), 0.0001);
		assertEquals(0.0, problem.getVariables().get("r1").getValue().doubleValue(), 0.0001);
		assertEquals(10000.0, problem.getVariables().get("r2").getValue().doubleValue(), 0.0001);
		assertEquals(10000.0, problem.getVariables().get("r3").getValue().doubleValue(), 0.0001);

		solver.terminate();
	}

	@Test
	public void testBasicOrConstraint() {
		// Objective
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

		// max b1 + b2
		LinearFunction lin = new LinearFunction();
		lin.addTerm(b1, 1.0);
		lin.addTerm(b2, 1.0);

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
		problem.setObjective(lin);
		problem.add(or1);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.CPLEX, false, 0.0, true, 42, false, 0.0, true, -10, 10, false,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		solver.solve();
		solver.updateValuesFromSolution();

		assertEquals(1, problem.getVariables().get("b1").getValue());
		assertEquals(1, problem.getVariables().get("b2").getValue());

		solver.terminate();
	}

	@Test
	public void testOperatorConversion() {
		// Objective
		// max b1
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(b1, 1.0);
		lin.addTerm(i1, 1.0);
		lin.addTerm(r1, -1.0);
		lin.addTerm(i2, 1.0);

		// Constraints
		// i1 != 10000
		LinearConstraint c1 = new LinearConstraint(Operator.NOT_EQUAL, i1.getUpperBound());
		c1.addTerm(i1, 1.0);

		// r1 > 1
		LinearConstraint c2 = new LinearConstraint(Operator.GREATER, 1.0);
		c2.addTerm(r1, 1.0);

		// i2^2 < 4
		QuadraticConstraint c3 = new QuadraticConstraint(Operator.LESS, 4);
		c3.addTerm(i2, i2, 1.0);

		// Model
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);
		problem.add(c3);

		assertEquals(3, problem.getConstraintCount());

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.CPLEX, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		solver.solve();
		solver.updateValuesFromSolution();

		assertEquals(5, problem.getConstraintCount());

		assertNotEquals(i1.getUpperBound(), problem.getVariables().get("i1").getValue());
		assertTrue(problem.getVariables().get("r1").getValue().doubleValue() > 1);
		assertTrue(problem.getVariables().get("i2").getValue().intValue() < 2
				&& problem.getVariables().get("i2").getValue().intValue() > -2);

		solver.terminate();
	}

	@Test
	public void testEmptyObjectiveFunction() {
		// Objective
		Problem problem = new Problem();

		// Constraints
		// i1 <= 10
		LinearConstraint c1 = new LinearConstraint(Operator.LESS_OR_EQUAL, 10);
		c1.addTerm(i1, 1.0);

		// r1 >= 1
		LinearConstraint c2 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 1.0);
		c2.addTerm(r1, 1.0);

		// 2*i2 = 4
		LinearConstraint c3 = new LinearConstraint(Operator.EQUAL, 4);
		c3.addTerm(i2, 2.0);

		// Model
		problem.setObjective(null);
		problem.add(c1);
		problem.add(c2);
		problem.add(c3);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.CPLEX, false, 0.0, true, 42, false, 0.0, false, 0, 0, false,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		solver.updateValuesFromSolution();

		assertEquals(1, out.getSolCount());
		assertTrue(problem.getVariables().get("i1").getValue().doubleValue() <= 10);
		assertTrue(problem.getVariables().get("r1").getValue().doubleValue() >= 1);
		assertEquals(2, problem.getVariables().get("i2").getValue().intValue());

		solver.terminate();
	}

	@Test
	public void testConfigParameterTimeout() {
		// Objective
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(b1, 1.0);

		// lin.addTerm(i1, 1.0);
		lin.addTerm(r1, -1.0);
		lin.addTerm(i2, 1.0);

		// Constraints
		// i1 <= 10
		LinearConstraint c1 = new LinearConstraint(Operator.LESS_OR_EQUAL, 10);
		c1.addTerm(i1, 1.0);

		// r1 >= 1
		LinearConstraint c2 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 1.0);
		c2.addTerm(r1, 1.0);

		// 2*i2 = 4
		LinearConstraint c3 = new LinearConstraint(Operator.EQUAL, 4);
		c3.addTerm(i2, 2.0);

		// Model
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);
		problem.add(c3);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.CPLEX, true, 1.0E-8, false, 0, false, 0.0, false, 0, 0, true,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();

		assertEquals(SolverStatus.TIME_OUT, out.getStatus());

		solver.terminate();
	}

	@Test
	public void testConfigParameterBounds() {
		// Objective
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(b1, 1.0);

		// lin.addTerm(i1, 1.0);
		lin.addTerm(r1, -1.0);
		lin.addTerm(i2, 1.0);

		// Constraints
		// i1 <= 10
		LinearConstraint c1 = new LinearConstraint(Operator.LESS_OR_EQUAL, 10);
		c1.addTerm(i1, 1.0);

		// r1 >= 1
		LinearConstraint c2 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 1.0);
		c2.addTerm(r1, 1.0);

		// 2*i2 = 4
		LinearConstraint c3 = new LinearConstraint(Operator.EQUAL, 4);
		c3.addTerm(i2, 2.0);

		// Model
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);
		problem.add(c3);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.CPLEX, false, 0.0, false, 0, false, 0.0, true, -5, 5, true,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		solver.solve();
		solver.updateValuesFromSolution();

		assertEquals(-5, problem.getVariables().get("i1").getLowerBound());
		assertEquals(5, problem.getVariables().get("i1").getUpperBound());
		assertEquals(-5, problem.getVariables().get("i2").getLowerBound());
		assertEquals(5, problem.getVariables().get("i2").getUpperBound());
		assertEquals(-5.0, problem.getVariables().get("r1").getLowerBound());
		assertEquals(5.0, problem.getVariables().get("r1").getUpperBound());

		assertTrue(problem.getVariables().get("i1").getValue().doubleValue() >= -5);
		assertTrue(problem.getVariables().get("i1").getValue().doubleValue() <= 5);
		assertTrue(problem.getVariables().get("i2").getValue().doubleValue() >= -5);
		assertTrue(problem.getVariables().get("i2").getValue().doubleValue() <= 5);
		assertTrue(problem.getVariables().get("r1").getValue().doubleValue() >= -5.0);
		assertTrue(problem.getVariables().get("r1").getValue().doubleValue() <= 5.0);

		solver.terminate();
	}
}
