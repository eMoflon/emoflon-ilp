package org.emoflon.ilp.test;

import org.junit.jupiter.api.Test;

import gurobi.GRBException;

import java.util.List;
import java.util.ArrayList;

import org.emoflon.ilp.*;
import org.emoflon.ilp.SolverConfig.SolverType;

public class BasicTest {

	@Test
	public void testBasicProblem() {
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
		lin.addTerm(x, 1.0, TermType.LINEAR);
		lin.addTerm(new Term(y, 1.0, TermType.LINEAR));
		lin.addTerm(new Term(z, 2.0, TermType.LINEAR));

		// Constraints
		// x + 2y + 3z <= 4
		List<Term> c1_terms = new ArrayList<Term>();
		c1_terms.add(new Term(x, 1.0, TermType.LINEAR));
		c1_terms.add(new Term(y, 2.0, TermType.LINEAR));
		c1_terms.add(new Term(z, 3.0, TermType.LINEAR));
		LinearConstraint c1 = new LinearConstraint(c1_terms, Operator.LESS_OR_EQUAL, 4.0);

		// x + y >= 1
		LinearConstraint c2 = new LinearConstraint(Operator.GREATER_OR_EQUAL, 1.0);
		c2.addTerm(x, 1.0, TermType.LINEAR);
		c2.addTerm(new Term(y, 1.0, TermType.LINEAR));

		// Model
		obj.setObjective(lin);
		obj.add(c1);
		obj.add(c2);

		// Optimize
		SolverConfig config = new SolverConfig(false, 0.0, true, 42, false, 0.0, SolverType.GUROBI, false, false, null,
				false, null);
		try {
			Solver solver = new GurobiSolver(config);
			solver.buildILPProblem(obj);
			SolverOutput out = solver.solve();
			System.out.println(out.toString());
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
		lin.addTerm(x, 1.0, TermType.LINEAR);
		lin.addTerm(y, 1.0, TermType.LINEAR);
		lin.addTerm(z, 1.0, TermType.LINEAR);

		// Constraints
		// x | y
		OrConstraint c1 = new OrConstraint(z);
		c1.addVariable(x);
		c1.addVariable(y);


		// Model
		obj.setObjective(lin);
		obj.add(c1);

		// Optimize
		SolverConfig config = new SolverConfig(false, 0.0, true, 42, false, 0.0, SolverType.GUROBI, false, false, null,
				false, null);
		try {
			Solver solver = new GurobiSolver(config);
			solver.buildILPProblem(obj);
			SolverOutput out = solver.solve();
			System.out.println(out.toString());
			solver.terminate();
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}
}
