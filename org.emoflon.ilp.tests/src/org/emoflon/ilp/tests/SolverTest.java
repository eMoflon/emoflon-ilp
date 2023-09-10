package org.emoflon.ilp.tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.emoflon.ilp.BinaryVariable;
import org.emoflon.ilp.LinearConstraint;
import org.emoflon.ilp.LinearFunction;
import org.emoflon.ilp.LinearTerm;
import org.emoflon.ilp.Objective;
import org.emoflon.ilp.ObjectiveType;
import org.emoflon.ilp.Operator;
import org.emoflon.ilp.Solver;
import org.emoflon.ilp.SolverConfig;
import org.emoflon.ilp.SolverConfig.SolverType;
import org.emoflon.ilp.SolverHelper;
import org.emoflon.ilp.SolverOutput;
import org.emoflon.ilp.Term;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class SolverTest {

	@Parameterized.Parameters
	public static Iterable<Object> data() {
		return Arrays.asList(SolverType.GUROBI, SolverType.GLPK);
	}

	private final SolverType type;

	public SolverTest(SolverType type) {
		this.type = type;
	}

	@Test
	public void test() {

		// Create variables
		BinaryVariable b1 = new BinaryVariable("b1");
		BinaryVariable b2 = new BinaryVariable("b2");
		BinaryVariable b3 = new BinaryVariable("b3");

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
		SolverConfig config = new SolverConfig(type, false, 0.0, true, 42, false, 0.0, false, 0, 0, false, false, null,
				false, null);
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

}
