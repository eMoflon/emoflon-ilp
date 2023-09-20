package org.emoflon.ilp.tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.emoflon.ilp.BinaryVariable;
import org.emoflon.ilp.RealVariable;
import org.emoflon.ilp.LinearConstraint;
import org.emoflon.ilp.LinearFunction;
import org.emoflon.ilp.LinearTerm;
import org.emoflon.ilp.Problem;
import org.emoflon.ilp.ObjectiveType;
import org.emoflon.ilp.Operator;
import org.emoflon.ilp.Solver;
import org.emoflon.ilp.SolverConfig;
import org.emoflon.ilp.SolverConfig.SolverType;
import org.emoflon.ilp.SolverHelper;
import org.emoflon.ilp.SolverOutput;
import org.emoflon.ilp.Term;
import org.junit.jupiter.api.Test;

public class SolverTest {

	// SolverType type = SolverType.GUROBI;
	// SolverType type = SolverType.GLPK;
	SolverType type = SolverType.CPLEX;

	boolean presolve = (type == SolverType.GLPK) ? true : false;

	@Test
	public void mip1() {

		// Create variables
		BinaryVariable b1 = new BinaryVariable("b1");
		BinaryVariable b2 = new BinaryVariable("b2");
		BinaryVariable b3 = new BinaryVariable("b3");

		// Gurobi Mip1 example

		// Objective
		// maximize b1 + b2 + 2*b3
		Problem obj = new Problem();
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
		SolverConfig config = new SolverConfig(type, false, 0.0, true, 42, false, 0.0, false, 0, 0, presolve, false,
				true, "/Users/luise/Projektseminar/cplex_mip1.lp");
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
	public void knapsackProblem() {

		// Amount of items
		int I = 6;
		// Profit
		int[] p = { 10, 13, 18, 32, 7, 15 };
		// Weight
		int[] w = { 11, 15, 20, 35, 10, 33 };
		// Capacity
		int c = 47;

		// Create variables:
		// 0 -> item i not put in knapsack
		// 1 -> item i put in knapsack
		List<BinaryVariable> x_i = new ArrayList<>();
		for (int i = 0; i < I; i++) {
			x_i.add(new BinaryVariable("x_" + i));
		}

		// Objective: maximize the total price of selected items
		// maximize SUM(p_i * x_i)
		Problem obj = new Problem();
		obj.setType(ObjectiveType.MAX);

		LinearFunction lin = new LinearFunction();
		for (int i = 0; i < I; i++) {
			lin.addTerm(x_i.get(i), p[i]);
		}

		// Constraint: Total weight must be equal or less than the capacity
		// SUM(w_i * x_i) <= c
		LinearConstraint c1 = new LinearConstraint(Operator.LESS_OR_EQUAL, c);
		for (int i = 0; i < I; i++) {
			c1.addTerm(x_i.get(i), w[i]);
		}

		// Model
		obj.setObjective(lin);
		obj.add(c1);

		// Optimize
		SolverConfig config = new SolverConfig(type, false, 0.0, true, 42, false, 0.0, false, 0, 0, presolve, false,
				true, "/Users/luise/Projektseminar/cplex_knapsack.lp");
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(obj);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		solver.updateValuesFromSolution();

		assertEquals(1, obj.getVariables().get("x_0").getValue());
		assertEquals(0, obj.getVariables().get("x_1").getValue());
		assertEquals(0, obj.getVariables().get("x_2").getValue());
		assertEquals(1, obj.getVariables().get("x_3").getValue());
		assertEquals(0, obj.getVariables().get("x_4").getValue());
		assertEquals(0, obj.getVariables().get("x_5").getValue());

		assertEquals(42, out.getObjVal(), 0.001);

		System.out.println("===================");
		System.out.println("Computation Result:");
		for (int i = 0; i < I; i++) {
			System.out.println("Value for x_" + i + ": " + obj.getVariables().get("x_" + i).getValue());
		}
		System.out.println("===================");

		solver.terminate();

	}

	@Test
	public void travelingSalesman() {

		// Locations
		String[] locations = { "Antwerp", "Bruges", "C-Mine", "Dinant", "Ghent", "Grand-Place de Bruxelles", "Hasselt",
				"Leuven", "Mechelen", "Mons", "Montagne de Bueren", "Namur", "Remouchamps", "Waterloo" };
		// Distances
		int[][] d = { { 83, 81, 113, 52, 42, 73, 44, 23, 91, 105, 90, 124, 57 },
				{ 161, 160, 39, 89, 151, 110, 90, 99, 177, 143, 193, 100 },
				{ 90, 125, 82, 13, 57, 71, 123, 38, 72, 59, 82 }, { 123, 77, 81, 71, 91, 72, 64, 24, 62, 63 },
				{ 51, 114, 72, 54, 69, 139, 105, 155, 62 }, { 70, 25, 22, 52, 90, 56, 105, 16 },
				{ 45, 61, 111, 36, 61, 57, 70 }, { 23, 71, 67, 48, 85, 29 }, { 74, 89, 69, 107, 36 },
				{ 117, 65, 125, 43 }, { 54, 22, 84 }, { 60, 44 }, { 97 }, {} };
		// Number of nodes
		int n = d.length;

		// Distance Matrix
		int[][] c = new int[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i == j) {
					c[i][j] = 0;
				} else if (i < j) {
					c[i][j] = d[i][j - i - 1];
				} else {
					c[i][j] = d[j][i - j - 1];
				}
			}
		}

		// Create variables for each node pair
		// 0 -> node pair ij is not used on the route
		// 1 -> node pair ij is used on the route
		BinaryVariable[][] x_ij = new BinaryVariable[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				x_ij[i][j] = new BinaryVariable("x_" + i + j);
			}
		}

		// Each location gets a continuous variable as an id
		RealVariable[] y_i = new RealVariable[n];
		for (int i = 0; i < n; i++) {
			y_i[i] = new RealVariable("y_" + i);
		}

		// Objective: minimize the total distance of the route
		// minimize SUM(c[i][j] * x[i][j])
		Problem obj = new Problem();
		obj.setType(ObjectiveType.MIN);

		LinearFunction lin = new LinearFunction();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				lin.addTerm(x_ij[i][j], c[i][j]);
			}
		}

		// Constraints
		// Leave each location only once
		// for each i: SUM(x_ij over j) == 1
		List<LinearConstraint> leaveLocOnce = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			LinearConstraint temp = new LinearConstraint(Operator.EQUAL, 1);
			for (int j = 0; j < n; j++) {
				if (i != j) {
					temp.addTerm(x_ij[i][j], 1);
				}
			}
			leaveLocOnce.add(temp);
		}

		// Enter each location only once
		// for each j: SUM(x_ij over i) == 1
		List<LinearConstraint> enterLocOnce = new ArrayList<>();
		for (int j = 0; j < n; j++) {
			LinearConstraint temp = new LinearConstraint(Operator.EQUAL, 1);
			for (int i = 0; i < n; i++) {
				if (i != j) {
					temp.addTerm(x_ij[i][j], 1);
				}
			}
			enterLocOnce.add(temp);
		}

		// Subtour elimination ???
		List<LinearConstraint> subtourElimination = new ArrayList<>();

		for (int i = 1; i < n; i++) {
			for (int j = 1; j < n; j++) {
				if (i != j) {
					LinearConstraint temp = new LinearConstraint(Operator.GREATER_OR_EQUAL, -n);
					temp.addTerm(y_i[i], 1);
					temp.addTerm(y_i[j], -1);
					temp.addTerm(x_ij[i][j], -(n + 1));
					subtourElimination.add(temp);
				}
			}
		}

		// Model
		obj.setObjective(lin);
		leaveLocOnce.forEach(it -> obj.add(it));
		enterLocOnce.forEach(it -> obj.add(it));
		subtourElimination.forEach(it -> obj.add(it));

		System.out.println(obj.getConstraintCount());

		// Optimize
		SolverConfig config = new SolverConfig(type, false, 120, true, 42, false, 0.0, false, 0, 0, presolve, false,
				true, "/Users/luise/Projektseminar/cplex_salesman.lp");
		Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(obj);
		SolverOutput out = solver.solve();
		System.out.println(out.toString());
		solver.updateValuesFromSolution();

		System.out.println("===================");
		System.out.println("Computation Result:");
		int k = 0;
		System.out.print(locations[k]);

		while (true) {
			for (int j = 0; j < n; j++) {
				if (x_ij[k][j].getValue() == 1) {
					System.out.print(" -> ");
					System.out.print(locations[j]);
					k = j;
					if (k == 0) {
						break;
					}
				}
			}
			if (k == 0) {
				break;
			}
		}

		System.out.println("");
		System.out.println("===================");

		solver.terminate();

	}

}
