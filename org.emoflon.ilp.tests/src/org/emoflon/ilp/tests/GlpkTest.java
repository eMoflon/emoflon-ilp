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
import org.emoflon.ilp.Problem;
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
import org.emoflon.ilp.SolverStatus;
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
		System.out.println("--------- testGurobiMip1Example() ---------");
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
		// TODO: Anmerkung zu presolve = true
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, false, 0, 0, true,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		solver.updateValuesFromSolution();

		assertEquals(1, problem.getVariables().get("b1").getValue());

		solver.terminate();
	}

	@Test
	public void testLinearConstrLinearObj() {
		System.out.println("--------- testLinearConstrLinearObj() ---------");
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
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, false, 0, 0, true,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		solver.updateValuesFromSolution();

		assertEquals(195.0, out.getObjVal(), 0.0001);
		assertEquals(5, problem.getVariables().get("i1").getValue());
		assertEquals(100.0, problem.getVariables().get("r2").getValue());

		solver.terminate();
	}

	@Test
	public void testQuadraticConstrLinearObj() {
		System.out.println("--------- testQuadraticConstrLinearObj() ---------");
		// Objective
		// maximize i1 + r1
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

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
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, false, 0, 0, true,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		assertThrows(IllegalArgumentException.class, () -> {
			solver.buildILPProblem(problem);
		});

		solver.terminate();
	}

	@Test
	public void testLinearConstrQuadraticObj() {
		System.out.println("--------- testLinearConstrQuadraticObj() ---------");
		// Objective
		// maximize i1^2 + r1
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

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
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, false, 0, 0, true,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		assertThrows(IllegalArgumentException.class, () -> {
			solver.buildILPProblem(problem);
		});

		solver.terminate();
	}

	@Test
	public void testQuadraticConstrQuadraticObj() {
		System.out.println("--------- testQuadraticConstrQuadraticObj() ---------");
		// Objective
		// maximize i1^2 + r1
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

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
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, false, 0, 0, true,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		assertThrows(IllegalArgumentException.class, () -> {
			solver.buildILPProblem(problem);
		});

		solver.terminate();
	}

	@Test
	public void testLessLinearConstraint() {
		System.out.println("--------- testLessLinearConstraint() ---------");
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
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, false, 0, 0, true,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		solver.updateValuesFromSolution();

		assertEquals(193.0, out.getObjVal(), 0.0001);
		assertEquals(5, problem.getVariables().get("i1").getValue());
		assertEquals(99, problem.getVariables().get("i2").getValue());

		solver.terminate();
	}

	@Test
	public void testGreaterLinearConstraint() {
		System.out.println("--------- testGreaterLinearConstraint() ---------");
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
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, false, 0, 0, true,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		solver.updateValuesFromSolution();

		assertEquals(194.0, out.getObjVal(), 0.0001);
		assertEquals(6, problem.getVariables().get("i1").getValue());
		assertEquals(100.0, problem.getVariables().get("r2").getValue());

		solver.terminate();
	}

	@Test
	public void testNotEqualLinearConstraint() {
		System.out.println("--------- testNotEqualLinearConstraint() ---------");
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

		c1.setEpsilon(1.0);

		// r2 != 100 (upper bound)
		r2.setUpperBound(100.0);
		LinearConstraint c2 = new LinearConstraint(Operator.NOT_EQUAL, 100.0);
		c2.addTerm(r2, 1.0);

		c2.setEpsilon(1.0);

		// Model
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, false, 0, 0, true,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		solver.updateValuesFromSolution();

		// assertEquals(195.0, out.getObjVal(), 0.0001);
		assertEquals(6, problem.getVariables().get("i1").getValue());
		assertEquals(99.0, problem.getVariables().get("r2").getValue().doubleValue(), 0.9999);

		solver.terminate();
	}

	@Test
	public void testBasicSOS1Constraint() {
		System.out.println("--------- testBasicSOS1Constr() ---------");
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

		// 2*b2 <= 5
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
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, false, 0, 0, true,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		solver.updateValuesFromSolution();

		assertEquals(1, problem.getVariables().get("b1").getValue());
		assertEquals(0, problem.getVariables().get("b2").getValue());
		assertEquals(0, problem.getVariables().get("b3").getValue());

		solver.terminate();
	}

	@Test
	public void testNonBinVarSOS1Constraint() {
		System.out.println("--------- testNonBinVarSOS1Constr() ---------");
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
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, false, 0, 0, true,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		solver.updateValuesFromSolution();

		assertEquals(20, problem.getVariables().get("i1").getValue());
		assertEquals(0, problem.getVariables().get("i2").getValue());
		assertEquals(0.0, problem.getVariables().get("r1").getValue());

		solver.terminate();
	}

	@Test
	public void testBasicOrConstraint() {
		System.out.println("--------- testBasicOrConstraint() ---------");
		// Objective
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(b1, 1.0);
		lin.addTerm(b2, 1.0);

		// Constraints
		// 5*b1 + b2 >= 1
		LinearConstraint c1 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 1.0);
		c1.addTerm(b1, 5.0);
		c1.addTerm(b2, 1.0);

		// b2 <= 0
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
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, true, -10, 10, true,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		solver.updateValuesFromSolution();

		assertEquals(1, problem.getVariables().get("b1").getValue());
		assertEquals(1, problem.getVariables().get("b2").getValue());

		solver.terminate();
	}

	@Test
	public void testOrVarsConstraint() {
		System.out.println("--------- testOrVarsConstraint() ---------");
		// Objective
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

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
		problem.setObjective(lin);
		problem.add(or1);
		problem.add(c1);
		problem.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, false, 0, 0, true,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		assertThrows(IllegalArgumentException.class, () -> {
			solver.buildILPProblem(problem);
		});

		solver.terminate();
	}

	@Test
	public void testOperatorConversion() {
		System.out.println("--------- testOperatorConversion() ---------");
		// Objective
		// max b1
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(b1, 1.0);

		// lin.addTerm(i1, 1.0);
		lin.addTerm(r1, -1.0);
		lin.addTerm(i2, 1.0);

		// Constraints
		// i1 != 5
		LinearConstraint c1 = new LinearConstraint(Operator.NOT_EQUAL, 5.0);
		c1.addTerm(i1, 1.0);

		c1.setEpsilon(1.0);

		// r1 > 1
		LinearConstraint c2 = new LinearConstraint(Operator.GREATER, 1.0);
		c2.addTerm(r1, 1.0);

		// i2 < 4
		LinearConstraint c3 = new LinearConstraint(Operator.LESS, 4);
		c3.addTerm(i2, 1.0);

		// Model
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);
		problem.add(c3);

		assertEquals(3, problem.getConstraintCount());

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, true, 1.0E-4, false, 0, 0, true,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		solver.updateValuesFromSolution();

		assertEquals(10, problem.getConstraintCount());

		assertNotEquals(5, problem.getVariables().get("i1").getValue());
		assertTrue(problem.getVariables().get("r1").getValue().doubleValue() > 1);
		assertTrue(problem.getVariables().get("i2").getValue().intValue() < 4);

		solver.terminate();
	}

	@Test
	public void testEmptyObjectiveFunction() {
		System.out.println("--------- testEmptyObjectiveFunction() ---------");
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
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, false, 0, 0, true,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		solver.updateValuesFromSolution();

		assertTrue(problem.getVariables().get("i1").getValue().doubleValue() <= 10);
		assertTrue(problem.getVariables().get("r1").getValue().doubleValue() >= 1);
		assertEquals(2, problem.getVariables().get("i2").getValue().intValue());

		solver.terminate();
	}

	@Test
	public void testConfigParameterTimeout() {
		System.out.println("--------- testConfigParameterTimeout() ---------");
		// Objective
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(b1, 1.0);
		lin.addTerm(i1, 1.0);
		lin.addTerm(r1, -1.0);
		lin.addTerm(i2, 1.0);

		// Constraints
		// 7*i1 + 3*r1 - 13*i2 <= 10
		LinearConstraint c1 = new LinearConstraint(Operator.LESS_OR_EQUAL, 10);
		c1.addTerm(i1, 7.0);
		c1.addTerm(r1, 3.0);
		c1.addTerm(i2, -13.0);

		// r1 + b1 >= 1
		LinearConstraint c2 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 1.0);
		c2.addTerm(r1, 1.0);
		c2.addTerm(b1, 1.0);

		// 2*i2 = 4
		LinearConstraint c3 = new LinearConstraint(Operator.EQUAL, 4);
		c3.addTerm(i2, 2.0);

		// r1 + 20*i1 >= 13
		LinearConstraint c4 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 13.0);
		c4.addTerm(r1, 1.0);
		c4.addTerm(i1, 20.0);

		// Model
		problem.setObjective(lin);
		problem.add(c1);
		problem.add(c2);
		problem.add(c3);
		problem.add(c4);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.GLPK, true, 1.0E-3, false, 0, false, 0.0, false, 0, 0, true,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();

		assertEquals(SolverStatus.TIME_OUT, out.getStatus());

		solver.terminate();
	}

	@Test
	public void testConfigParameterBounds() {
		System.out.println("--------- testConfigParameterBounds() ---------");
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
		SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, false, 0, false, 0.0, true, -5, 5, true,
				false, false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(problem);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
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
