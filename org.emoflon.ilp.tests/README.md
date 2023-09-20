# Tests

## Some notes about specific test cases

### GLPK Tests

#### All GLPK tests
For GLPK all SolverConfigs set presolve to true. 
If set to false, an error occurs because glpk expects the problem object to contain an optimal solution to the LP relaxation.

See GLPK documentation of glp_intopt:

> glp intopt — solve MIP problem with the branch-and-cut method
> 
> Synopsis  
> int glp_intopt(glp_prob *P, const glp_iocp *parm);
> 
> Description  
> [...]  
> If the presolver is disabled (see paragraph “Control parameters” below), on entry to the routine
> glp_intopt the problem object, which the parameter mip points to, should contain optimal solution
> to LP relaxation (it can be obtained, for example, with the routine glp_simplex). Otherwise, if
> the presolver is enabled, it is not necessary.

#### testNotEqualLinearConstraint

Espilon and the tolerance had to be changed for this test case.  

The values for psi and psi_prime in the substitution of != otherwise were so close to zero, that the constraints didn't work as intended.  
The optimized value for i1 was 5 (c1: i1 != 5) and for r2 was 100 (c2: r2 != 100).  

Experimental values that worked:
1. tolerance = 1.0E-8  
2. tolerance = 1.0E-6 and epsilon = 9.9999E-2

### CPLEX Tests

#### testLessQuadraticConstraint
For this testcase the tolerance of the solver had to be changed.  
tolerance = 1.0E-6

#### testGreaterQuadraticConstraint
For this testcase the tolerance of the solver had to be changed.  
tolerance = 1.0E-6

## How to run tests
Remember: Depending on the solver a license is necessary (e.g. for Gurobi).

- Run all tests:  
  `$ mvn clean verify`
- Run a specific test class (e.g. GlpkTest.java):  
  `$ mvn -Dtest=GlpkTest -DfailIfNoTests=false verify`

Before running tests with the Cplex solver, it might be necessary to add the following Run Configuration to the VM Arguments (Eclipse: right click on the project -> `Run as` -> `Run Configurations` -> `Arguments` tab), replace with the appropriate path:  
`-Djava.library.path=/opt/ibm/ILOG/CPLEX_Studioxxx/cplex/bin/x86-64_xxx`

## Example Problem
### Knapsack Problem
This example can be found in the Test Class SolverTest.java.

There is a set of items, which all have a weight and a value.
The goal is to determine a collection of items, for which the profit is maximized but the capacity constraint is satisfied.

```
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
Problem problem = new Problem();
problem.setType(ObjectiveType.MAX);

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
problem.setObjective(lin);
problem.add(c1);

// Optimize
SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, false, 0, 0, true, false, false, null);
Solver solver = (new SolverHelper(config)).getSolver();
solver.buildILPProblem(problem);
SolverOutput out = solver.solve();
// Prints the result of the objective
System.out.println(out.toString());
// Sets the values for the Variables
solver.updateValuesFromSolution();

// Do something, e.g. print

solver.terminate();
```
