package org.emoflon.ilp.tests;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.emoflon.ilp.*;
import org.junit.jupiter.api.Test;

public class BasicTest {

	@Test
	public void testLinearTermsException() {
		// Create variables
		BinaryVariable x = new BinaryVariable("x");
		BinaryVariable y = new BinaryVariable("y");
		BinaryVariable z = new BinaryVariable("z");

		// Objective
		// maximize x + y + 2z
		Problem problem = new Problem();
		problem.setType(ObjectiveType.MAX);

		LinearFunction lin = new LinearFunction();
		lin.addTerm(x, 1.0);
		lin.addTerm(new LinearTerm(y, 1.0));
		lin.addTerm(new LinearTerm(z, 2.0));

		// Constraints
		// x + 2y + 3z^2 <= 4
		List<Term> c1_terms = new ArrayList<Term>();
		c1_terms.add(new LinearTerm(x, 1.0));
		c1_terms.add(new LinearTerm(y, 2.0));
		c1_terms.add(new QuadraticTerm(z, z, 3.0));

		assertThrows(IllegalArgumentException.class, () -> {
			new LinearConstraint(c1_terms, Operator.LESS_OR_EQUAL, 4.0);
		});
	}

	@Test
	public void testLinearFunctionExpand() {
		// Create variables
		BinaryVariable x = new BinaryVariable("x");
		BinaryVariable y = new BinaryVariable("y");
		BinaryVariable z = new BinaryVariable("z");

		// Function x + y + 42
		LinearFunction f_x_y = new LinearFunction();
		f_x_y.addTerm(x, 1.0);
		f_x_y.addTerm(new LinearTerm(y, 1.0));
		f_x_y.addConstant(42);

		// Function f(x, y) + 2z
		LinearFunction g_f_z = new LinearFunction();
		g_f_z.addNestedFunction(f_x_y, 1);
		g_f_z.addTerm(new LinearTerm(z, 2.0));

		// Function 2*g(f(x,y), z)
		LinearFunction h_g_z = new LinearFunction();
		h_g_z.addNestedFunction(g_f_z, 2);

		// Function h(g) + 17
		// -> 2*((x + y + 42) + 2*z) + 17
		// -> 2*x + 2*y + 4*z + 17 + 84
		LinearFunction lin = new LinearFunction();
		lin.addNestedFunction(h_g_z, 1);
		lin.addConstant(17);

		assertEquals(1, lin.getNestedFunctions().size());
		assertEquals(42, f_x_y.getConstants().get(0).weight(), 0.01);

		LinearFunction expanded = (LinearFunction) lin.expand();
		assertEquals(17, expanded.getConstants().get(0).weight(), 0.01);
		assertEquals(84, expanded.getConstants().get(1).weight(), 0.01);
		assertEquals(0, expanded.getNestedFunctions().size());
		assertEquals(3, expanded.getTerms().size());
	}

	@Test
	public void testQuadraticNestedFunctionException() {
		// Create variables
		BinaryVariable x = new BinaryVariable("x");
		BinaryVariable y = new BinaryVariable("y");

		// Function x + y^2 + 42
		QuadraticFunction f_x_y = new QuadraticFunction();
		f_x_y.addTerm(x, 1.0);
		f_x_y.addTerm(y, y, 1.0);
		f_x_y.addConstant(42);

		// Function f(x, y) + 2z
		LinearFunction g_f_z = new LinearFunction();
		assertThrows(IllegalArgumentException.class, () -> {
			g_f_z.addNestedFunction(f_x_y, 1);
		});
	}

	@Test
	public void testQuadraticFunctionExpand() {
		// Create variables
		BinaryVariable x = new BinaryVariable("x");
		BinaryVariable y = new BinaryVariable("y");
		BinaryVariable z = new BinaryVariable("z");

		// Function x + y^2 + 42
		QuadraticFunction f_x_y = new QuadraticFunction();
		f_x_y.addTerm(x, 1.0);
		f_x_y.addTerm(y, y, 1.0);
		f_x_y.addConstant(42);

		// Function f(x, y) + 2z
		QuadraticFunction g_f_z = new QuadraticFunction();
		g_f_z.addNestedFunction(f_x_y, 1);
		g_f_z.addTerm(new LinearTerm(z, 2.0));

		// Function 2*g(f(x,y), z)
		QuadraticFunction lin = new QuadraticFunction();
		lin.addNestedFunction(g_f_z, 2);
		lin.addTerm(y, 1.0);
		lin.addTerm(y, y, 2);

		assertEquals(1, lin.getNestedFunctions().size());
		assertEquals(42, f_x_y.getConstants().get(0).weight(), 0.01);

		QuadraticFunction expanded = (QuadraticFunction) lin.expand();

		assertEquals(84, expanded.getConstants().get(0).weight(), 0.01);
		assertEquals(0, expanded.getNestedFunctions().size());
		assertEquals(5, expanded.getTerms().size());

	}

	@Test
	public void testOrSubtitution() {
		// Create variables
		BinaryVariable x = new BinaryVariable("x");
		BinaryVariable y = new BinaryVariable("y");

		// Constraints
		// x + 2y <= 4
		LinearConstraint c1 = new LinearConstraint(Operator.LESS_OR_EQUAL, 4.0);
		c1.addTerm(x, 1.0);
		c1.addTerm(y, 2.0);

		// x - 2y <= 4
		LinearConstraint c2 = new LinearConstraint(Operator.LESS_OR_EQUAL, 4.0);
		c2.addTerm(x, 1.0);
		c2.addTerm(y, 2.0);

		OrConstraint or = new OrConstraint();

		or.addConstraint(c1);
		or.addConstraint(c2);

		List<Constraint> substitution = or.convert();

		// <= -> 7 substitution constraints (5 linear, 2 SOS)
		// 2*7 + 1 substitution constraints total
		assertEquals(15, substitution.size());
	}

	@Test
	public void testSOS1Substitution() {
		// Create variables
		BinaryVariable x = new BinaryVariable("x");
		BinaryVariable y = new BinaryVariable("y");
		BinaryVariable z = new BinaryVariable("z");

		// SOS1(x, y, z)
		SOS1Constraint sos = new SOS1Constraint();
		sos.addVariables(Arrays.asList(x, y, z));

		List<LinearConstraint> substitution = sos.convert();

		// 2 substitution constraints for each variable
		// -> 2*3 + 1 substitution constraints total
		assertEquals(7, substitution.size());
	}

	@Test
	public void testOperatorSubstitution() {
		// Create variables
		BinaryVariable x = new BinaryVariable("x");
		BinaryVariable y = new BinaryVariable("y");

		// Create constraints
		// <
		LinearConstraint less = new LinearConstraint(Operator.LESS, 5, 1.0);
		less.addTerm(x, 1.0);
		less.addTerm(y, 2.0);
		List<Constraint> sub_less = less.convertOperator();
		assertEquals(1, sub_less.size());
		assertEquals(Operator.LESS_OR_EQUAL, ((LinearConstraint) sub_less.get(0)).getOp());
		assertEquals(4, ((LinearConstraint) sub_less.get(0)).getRhs(), 0.00001);
		assertEquals(less.getLhsTerms(), ((LinearConstraint) sub_less.get(0)).getLhsTerms());

		// >
		LinearConstraint greater = new LinearConstraint(Operator.GREATER, 4, 1.0);
		greater.addTerm(x, 10);
		greater.addTerm(y, 1);
		List<Constraint> sub_greater = greater.convertOperator();
		assertEquals(1, sub_greater.size());
		assertEquals(Operator.GREATER_OR_EQUAL, ((LinearConstraint) sub_greater.get(0)).getOp());
		assertEquals(5, ((LinearConstraint) sub_greater.get(0)).getRhs(), 0.00001);
		assertEquals(greater.getLhsTerms(), ((LinearConstraint) sub_greater.get(0)).getLhsTerms());

		// !=
		LinearConstraint not_equal = new LinearConstraint(Operator.NOT_EQUAL, 11, 1.0);
		not_equal.addTerm(x, 3);
		not_equal.addTerm(y, 9);
		List<Constraint> sub_neq = not_equal.convertOperator();
		assertEquals(4, sub_neq.size());
		assertTrue(sub_neq.get(1) instanceof SOS1Constraint);
	}
}
