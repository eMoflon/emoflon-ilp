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

	@BeforeEach
	public void setup() {
		// (Re-)set variables
		b1 = new BinaryVariable("b1");
		b2 = new BinaryVariable("b2");
		b3 = new BinaryVariable("b3");
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
		SolverConfig config = new SolverConfig(SolverType.GUROBI, false, 0.0, true, 42, false, 0.0, false, false, null,
				false, null);
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
		SolverConfig config = new SolverConfig(SolverType.GUROBI, false, 0.0, true, 42, false, 0.0, false, false, null,
				false, null);
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
		SolverConfig config = new SolverConfig(SolverType.GUROBI, false, 0.0, true, 42, false, 0.0, false, false, null,
				false, null);
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(obj);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		Objective result = solver.updateValuesFromSolution();

		assertEquals(0, result.getVariables().get("b1").getValue());
		assertEquals(1, result.getVariables().get("b2").getValue());
		assertEquals(0, result.getVariables().get("b3").getValue());

		System.out.println("===================");
		System.out.println("Computation Result:");
		System.out.println("Value for b1: " + result.getVariables().get("b1").getValue());
		System.out.println("Value for b2: " + result.getVariables().get("b2").getValue());
		System.out.println("Value for b3: " + result.getVariables().get("b3").getValue());
		System.out.println("===================");

		solver.terminate();
	}
}
