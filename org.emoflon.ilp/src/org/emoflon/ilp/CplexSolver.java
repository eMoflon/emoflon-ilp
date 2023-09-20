package org.emoflon.ilp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Status;

/**
 * This class represents the CPLEX Solver. Here the problem formulation gets
 * translated to be comprehensible for CPLEX and solved.
 *
 */
public class CplexSolver implements Solver {

	private IloCplex cplex;
	private String outputPath;
	final private SolverConfig config;
	private final HashMap<String, IloNumVar> cplexVars = new HashMap<>();
	private Problem problem;
	private SolverOutput result;

	/**
	 * The constructor for CplexSolver.
	 * 
	 * @param config The configuration parameters used for this solver.
	 * @see SolverConfig
	 */
	public CplexSolver(final SolverConfig config) {
		this.config = config;
		init();
	}

	/**
	 * Initializes the solver with the parameters given in the configuration.
	 */
	private void init() {
		try {
			cplex = new IloCplex();

			// set configuration parameters
			// Presolve?
			cplex.setParam(IloCplex.Param.Preprocessing.Presolve, config.presolveEnabled());
			// Random Seed?
			if (config.randomSeedEnabled()) {
				cplex.setParam(IloCplex.Param.RandomSeed, config.randomSeed());
			}
			// Output?
			if (!config.debugOutputEnabled()) {
				cplex.setOut(null);
			}
			// Tolerance?
			if (config.toleranceEnabled()) {
				cplex.setParam(IloCplex.Param.MIP.Tolerances.Integrality, config.tolerance());
				cplex.setParam(IloCplex.Param.MIP.Tolerances.AbsMIPGap, config.tolerance());
			}
			// Timeout?
			if (config.timeoutEnabled()) {
				cplex.setParam(IloCplex.Param.TimeLimit, config.timeout());
			}

			// set output path, if configured
			if (config.outputEnabled()) {
				this.outputPath = config.outputPath();
			}
		} catch (final IloException e) {
			throw new RuntimeException(e);
		}

		cplexVars.clear();

	}

	@Override
	public void buildILPProblem(Problem problem) {
		this.problem = problem;

		/*
		// Quadratic Constraints or Functions are not yet implemented
		if (problem.getConstraints().stream().anyMatch(QuadraticConstraint.class::isInstance)
				|| (problem.getObjective() instanceof QuadraticFunction)) {
			throw new IllegalArgumentException(
					"CPLEX does support quadratic constraints and quadratic functions but that is not yet implemented in this plug-in!");
		}
		*/
		// General Constraints are not supported
		// TODO: (future work) convert OrVarsConstraints to OrConstraints or remove
		if (problem.getGenConstraintCount() != 0) {
			throw new IllegalArgumentException("General Constraints are not yet supported for CPLEX.");
		}

		// Substitute Or Constraints
		problem.substituteOr();

		// Substitute <, >, != Operators
		problem.substituteOperators();

		// Initialize decision variables and objective
		// Translate Variables
		translateVariables(problem.getVariables());

		// Translate Objective to GRB
		translateObjective();

		// Translate Normal Constraints
		translateNormalConstraints();

		// Translate General Constraints
		// problem.getGeneralConstraints().forEach(it ->
		// translateGeneralConstraint(it));

		// Translate SOS Constraints
		translateSOSConstraints();
	}

	/**
	 * Translates the variables to CPLEX variables.
	 * 
	 * @param vars A map of the variables to be translated.
	 */
	private void translateVariables(Map<String, Variable<?>> variables) {
		for (final String name : variables.keySet()) {
			Variable<?> var = variables.get(name);

			switch (var.getType()) {
			case BINARY:
				cplexVars.put(name, translateBinaryVariable((BinaryVariable) var));
				break;
			case INTEGER:
				cplexVars.put(name, translateIntegerVariable((IntegerVariable) var));
				break;
			case REAL:
				cplexVars.put(name, translateRealVariable((RealVariable) var));
				break;
			default:
				throw new UnsupportedOperationException("This variable type is not known.");
			}
		}
	}

	/**
	 * Translates a binary variable into a CPLEX variable and adds it to the
	 * problem.
	 * 
	 * @param variable Binary variable to be translated and added.
	 * @return Translated CPLEX variable.
	 */
	private IloIntVar translateBinaryVariable(BinaryVariable binaryVariable) {
		try {
			if (binaryVariable.getLowerBound() > binaryVariable.getUpperBound()) {
				throw new IllegalArgumentException(
						"The lower bound is not allowed to be greater than the upper bound.");
			}
			final IloIntVar binaryCplexVar = cplex.boolVar(binaryVariable.getName());
			binaryCplexVar.setLB(binaryVariable.getLowerBound());
			binaryCplexVar.setUB(binaryVariable.getUpperBound());

			return binaryCplexVar;
		} catch (IloException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Translates an integer variable into a CPLEX variable and adds it to the
	 * problem.
	 * 
	 * @param variable Integer variable to be translated and added.
	 * @return Translated CPLEX variable.
	 */
	private IloIntVar translateIntegerVariable(IntegerVariable integerVariable) {
		try {
			int lb = integerVariable.getLowerBound();
			int ub = integerVariable.getUpperBound();

			if (config.boundsEnabled()) {
				if (integerVariable.isDefaultLowerBound()) {
					lb = config.lowerBound();
					integerVariable.setLowerBound((int) lb);
				}
				if (integerVariable.isDefaultUpperBound()) {
					ub = config.upperBound();
					integerVariable.setUpperBound((int) ub);
				}
			}
			if (lb > ub) {
				throw new IllegalArgumentException(
						"The lower bound is not allowed to be greater than the upper bound.");
			}
			final IloIntVar integerCplexVar = cplex.intVar(lb, ub);

			return integerCplexVar;
		} catch (IloException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Translates a real variable into a CPLEX variable and adds it to the problem.
	 * 
	 * @param realVariable Real variable to be translated and added.
	 * @return Translated CPLEX variable.
	 */
	private IloNumVar translateRealVariable(RealVariable realVariable) {
		try {
			double lb = realVariable.getLowerBound();
			double ub = realVariable.getUpperBound();

			if (config.boundsEnabled()) {
				if (realVariable.isDefaultLowerBound()) {
					lb = config.lowerBound();
					realVariable.setLowerBound(lb);
				}
				if (realVariable.isDefaultUpperBound()) {
					ub = config.upperBound();
					realVariable.setUpperBound(ub);
				}
			}
			if (lb > ub) {
				throw new IllegalArgumentException(
						"The lower bound is not allowed to be greater than the upper bound.");
			}
			final IloNumVar realCplexVar = cplex.numVar(lb, ub);

			return realCplexVar;
		} catch (IloException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Translates the objective function and sets the CPLEX objective.
	 */
	private void translateObjective() {
		// Translate Objective to CPLEX
		Function obj = problem.getObjective().expand();
		IloObjective cplexObj = null;

		try {
			// Translate objective sense
			switch (problem.getType()) {
			case MIN:
				cplexObj = cplex.addMinimize();
				break;
			case MAX:
				cplexObj = cplex.addMaximize();
				break;
			}

			// Add Terms
			// No nested functions because of expand() call above

			// Linear Terms
			// get the coefficients of linear terms
			HashMap<String, Double> lin_coefficients = new HashMap<>();
			for (Term term : obj.terms.stream().filter(LinearTerm.class::isInstance).toList()) {
				// Get name of the variable in term
				String varName = term.getVar1().getName();

				// Get previous coefficient (if there is one)
				double prevCoef = 0.0;
				if (lin_coefficients.containsKey(varName)) {
					prevCoef = lin_coefficients.remove(varName);
				}

				// Set new coefficient for the variable (replaces old value)
				lin_coefficients.put(varName, prevCoef + term.getWeight());
			}

			// Set Objective Function
			// Linear Terms
			for (String varName : lin_coefficients.keySet()) {
				cplex.setLinearCoef(cplexObj, lin_coefficients.get(varName), cplexVars.get(varName));
			}
			// Quadratic Terms
			// TODO: (future work) coefficients of quadratic terms are not summed up before
			// adding them to the cplex problem. cplex adds them up in presolve, so there is
			// no issue with this solution. but adding them up manually might be a good
			// idea.
			for (QuadraticTerm term : obj.terms.stream().filter(QuadraticTerm.class::isInstance)
					.map(QuadraticTerm.class::cast).collect(Collectors.toList())) {
				// Get name of the variable in term
				String varName1 = term.getVar1().getName();
				String varName2 = term.getVar2().getName();

				cplex.setQuadCoef(cplexObj, term.getWeight(), cplexVars.get(varName1), cplexVars.get(varName2));
			}

			// Add Constant (sum of constants)
			double constant = 0.0;
			for (Constant cons : obj.getConstants()) {
				constant += cons.weight();
			}

			// Set constant
			cplexObj.setExpr(cplex.sum(cplexObj.getExpr(), cplex.constant(constant)));

		} catch (IloException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Translates the normal constraints into CPLEX constraints and adds them to the
	 * model.
	 */
	private void translateNormalConstraints() {
		if (problem.getConstraintCount() + problem.getSOSConstraintCount() != problem.getTotalConstraintCount()) {
			throw new Error("All Constraints should be linear or SOS constraints!");
		}
		if (problem.getConstraintCount() == 0) {
			return;
		}

		try {
			for (final NormalConstraint constraint : problem.getConstraints()) {

				IloNumExpr constraintExpr = cplex.numExpr();

				if (constraint instanceof LinearConstraint) {
					IloNumExpr[] numExprs = new IloNumExpr[constraint.getLhsTerms().size()];

					int i = 0;
					for (Term term : constraint.getLhsTerms()) {
						numExprs[i] = cplex.prod(term.getWeight(), cplexVars.get(term.getVar1().getName()));
						i++;
					}

					constraintExpr = cplex.sum(numExprs);

				} else if (constraint instanceof QuadraticConstraint) {
					IloNumExpr[] numExprs = new IloNumExpr[constraint.getLhsTerms().size()];

					int i = 0;
					for (Term term : constraint.getLhsTerms()) {
						if (term instanceof LinearTerm) {
							numExprs[i] = cplex.prod(term.getWeight(), cplexVars.get(term.getVar1().getName()));
						} else if (term instanceof QuadraticTerm) {
							numExprs[i] = cplex.prod(term.getWeight(), cplexVars.get(term.getVar1().getName()),
									cplexVars.get(((QuadraticTerm) term).getVar2().getName()));
						}
						i++;
					}

					constraintExpr = cplex.sum(numExprs);
				}

				switch (constraint.getOp()) {
				case LESS_OR_EQUAL:
					cplex.addLe(constraintExpr, constraint.getRhs());
					break;
				case GREATER_OR_EQUAL:
					cplex.addGe(constraintExpr, constraint.getRhs());
					break;
				case EQUAL:
					cplex.addEq(constraintExpr, constraint.getRhs());
					break;
				case LESS:
					throw new Error("All constraints with this operator should already have been converted!");
				case GREATER:
					throw new Error("All constraints with this operator should already have been converted!");
				case NOT_EQUAL:
					throw new Error("All constraints with this operator should already have been converted!");
				default:
					throw new UnsupportedOperationException("Unsupported operator.");
				}
			}
		} catch (IloException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Translates the SOS constraints into CPLEX constraints and adds them to the
	 * model.
	 */
	private void translateSOSConstraints() {
		for (SOS1Constraint constraint : problem.getSOSConstraints()) {
			try {
				ArrayList<IloNumVar> sosVars = new ArrayList<>();
				for (Variable<?> var : constraint.getVariables()) {
					sosVars.add(cplexVars.get(var.getName()));
				}
				IloNumVar[] cplexSosVars = sosVars.toArray(new IloNumVar[sosVars.size()]);
				double[] weights = constraint.getWeights();
				// CPLEX doesn't allow duplicate weights for SOS constraints.
				if (containsDuplicates(weights)) {
					System.out.println(
							"Warning: CPLEX requests unique weights for the priority of the variables in a SOS constraint.");
					System.out.println("Warning: There were duplicate values for the SOS constraint "
							+ constraint.toString() + ".");
					System.out.println("Warning: The weights got changed to unique weights in ascending order.");
					weights = IntStream.range(0, weights.length).asDoubleStream().toArray();
				}
				cplex.addSOS1(cplexSosVars, weights);
			} catch (IloException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Checks if an Array contains multiple entries of the same value.
	 * 
	 * @param weights Double array to be checked.
	 * @return True, if a duplicate was found, else False.
	 */
	private boolean containsDuplicates(double[] weights) {
		Set<Double> seen = new HashSet<Double>();
		for (double i : weights) {
			if (seen.contains(i))
				return true;
			seen.add(i);
		}
		return false;
	}

	@Override
	public SolverOutput solve() {
		// Write model into output file
		if (this.outputPath != null) {
			try {
				cplex.exportModel(this.outputPath);
			} catch (final IloException e) {
				e.printStackTrace();
			}
		}

		try {
			final boolean solve = cplex.solve();

			// Get the objective result
			double objVal = 0;
			int solCount = -1;
			if (solve) {
				objVal = cplex.getObjValue();
				solCount = cplex.getSolnPoolNsolns();
			}

			// Determine status
			SolverStatus status = null;
			final Status cplexStatus = cplex.getStatus();

			if (cplexStatus == IloCplex.Status.Unbounded) {
				status = SolverStatus.UNBOUNDED;
			} else if (cplexStatus == IloCplex.Status.InfeasibleOrUnbounded) {
				status = SolverStatus.INF_OR_UNBD;
			} else if (cplexStatus == IloCplex.Status.Infeasible) {
				status = SolverStatus.INFEASIBLE;
			} else if (cplexStatus == IloCplex.Status.Optimal) {
				status = SolverStatus.OPTIMAL;
			} else if (cplexStatus == IloCplex.Status.Unknown) {
				status = SolverStatus.TIME_OUT;
			} else if (cplexStatus == IloCplex.Status.Feasible) {
				status = SolverStatus.FEASIBLE;
			} else {
				throw new RuntimeException("Unknown solver status.");
			}

			this.result = new SolverOutput(status, objVal, solCount);
			return this.result;

		} catch (final IloException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void updateValuesFromSolution() {
		if (this.result.getStatus() == SolverStatus.INFEASIBLE || this.result.getStatus() == SolverStatus.INF_OR_UNBD) {
			throw new RuntimeException(
					"The problem status is " + this.result.getStatus() + " and therefore no values were found.");
		}
		Map<String, Variable<?>> objVars = this.problem.getVariables();

		for (final String varName : this.cplexVars.keySet()) {
			try {
				// Save result value
				// TODO: (future work) configuration for round in SolverConfig
				Variable<?> objVar = objVars.get(varName);
				if (objVar instanceof BinaryVariable) {
					long val = Math.round(cplex.getValue(cplexVars.get(varName)));
					if (val >= 1) {
						((BinaryVariable) objVar).setValue(1);
					} else {
						((BinaryVariable) objVar).setValue(0);
					}
				} else if (objVar instanceof IntegerVariable) {
					((IntegerVariable) objVar).setValue((int) Math.round(cplex.getValue(cplexVars.get(varName))));
				} else if (objVar instanceof RealVariable) {
					((RealVariable) objVar).setValue(cplex.getValue(cplexVars.get(varName)));
				} else {
					throw new Error("This variable type is not implemented!");
				}
			} catch (final IloException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void terminate() {
		try {
			cplex.setDefaults();
			cplex.clearModel();
			cplex.endModel();
		} catch (final IloException e) {
			e.printStackTrace();
		}
		cplex.end();
	}

	@Override
	public void reset() {
		init();
	}

}
