package org.emoflon.ilp.tests;

import org.junit.jupiter.api.Test;

import gurobi.GRBException;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.emoflon.ilp.*;
import org.emoflon.ilp.SolverConfig.SolverType;

public class GurobiTest {

	@Test
	public void testBasicGurobiProblem() {
		// Gurobi Mip1 example

		// Create variables
		BinaryVariable x = new BinaryVariable("x");
		BinaryVariable y = new BinaryVariable("y");
		BinaryVariable z = new BinaryVariable("z");

		// Objective
		// maximize x + y + 2z
		Objective obj = new Objective();
		obj.setType(ObjectiveType.MAX);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(x, 1.0);
		lin.addTerm(new LinearTerm(y, 1.0));
		lin.addTerm(new LinearTerm(z, 2.0));

		// Constraints
		// x + 2y + 3z <= 4
		List<Term> c1_terms = new ArrayList<Term>();
		c1_terms.add(new LinearTerm(x, 1.0));
		c1_terms.add(new LinearTerm(y, 2.0));
		c1_terms.add(new LinearTerm(z, 3.0));
		LinearConstraint c1 = new LinearConstraint(c1_terms, Operator.LESS_OR_EQUAL, 4.0);

		// x + y >= 1
		LinearConstraint c2 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 1.0);
		c2.addTerm(x, 1.0);
		c2.addTerm(new LinearTerm(y, 1.0));

		// Model
		obj.setObjective(lin);
		obj.add(c1);
		obj.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.GUROBI, false, 0.0, true, 42, false, 0.0, false, false, null,
				false, null);
		try {
			Solver solver = (new SolverHelper(config)).getSolver();
			solver.buildILPProblem(obj);
			SolverOutput out = solver.solve();
			System.out.println(out.toString());
			Objective result = solver.updateValuesFromSolution();

			assertEquals(1, result.getVariables().get("x").getValue());

			System.out.println("===================");
			System.out.println("Computation Result:");
			System.out.println("Value for x: " + result.getVariables().get("x").getValue());
			System.out.println("Value for y: " + result.getVariables().get("y").getValue());
			System.out.println("Value for z: " + result.getVariables().get("z").getValue());
			System.out.println("===================");

			solver.terminate();

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}

	@Test
	public void testBasicOrProblem() {
		// TODO: minimal Or Constraint example
		// Create variables
		BinaryVariable x = new BinaryVariable("x");
		BinaryVariable y = new BinaryVariable("y");
		BinaryVariable z = new BinaryVariable("z");

		// Objective
		// maximize x + y + z
		Objective obj = new Objective();
		obj.setType(ObjectiveType.MIN);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(x, 1.0);
		lin.addTerm(y, 1.0);
		lin.addTerm(z, 1.0);

		// Constraints
		// x | y
		OrVarsConstraint c1 = new OrVarsConstraint(z);
		c1.addVariable(x);
		c1.addVariable(y);

		// Model
		obj.setObjective(lin);
		obj.add(c1);

		// Optimize
		SolverConfig config = new SolverConfig(SolverType.GUROBI, false, 0.0, true, 42, false, 0.0, false, false, null,
				false, null);
		try {
			Solver solver = (new SolverHelper(config)).getSolver();
			solver.buildILPProblem(obj);
			SolverOutput out = solver.solve();
			System.out.println(out.toString());
			solver.updateValuesFromSolution();
			solver.terminate();
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
		assertTrue(true);

	}
}
