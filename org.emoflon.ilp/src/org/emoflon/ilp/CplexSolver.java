package org.emoflon.ilp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
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
	private Objective objective;
	private SolverOutput result;

	/**
	 * The constructor for CplexSolver.
	 * 
	 * @param config The configuration parameters used for this solver.
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
	public void buildILPProblem(Objective objective) {
		this.objective = objective;

		// Quadratic Constraints or Functions are not yet implemented
		if (objective.getConstraints().stream().anyMatch(QuadraticConstraint.class::isInstance)
				|| (objective.getObjective() instanceof QuadraticFunction)) {
			throw new IllegalArgumentException(
					"CPLEX does support quadratic constraints and quadratic functions but that is not yet implemented in this plug-in!");
		}
		// General Constraints are not supported
		// TODO (future work): convert OrVarsConstraints to OrConstraints or remove
		if (objective.getGenConstraintCount() != 0) {
			throw new IllegalArgumentException("General Constraints are not yet supported for CPLEX.");
		}

		// Substitute Or Constraints
		objective.substituteOr();

		// Substitute <, >, != Operators
		objective.substituteOperators();

		// Initialize decision variables and objective
		// Translate Variables
		translateVariables(objective.getVariables());

		// Translate Objective to GRB
		translateObjective();

		// Translate Normal Constraints
		translateNormalConstraints();

		// Translate General Constraints
		// objective.getGeneralConstraints().forEach(it ->
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
		IloNumVar temp = null;
		for (final String name : variables.keySet()) {
			Variable<?> var = variables.get(name);

			switch (var.getType()) {
			case BINARY:
				temp = translateBinaryVariable((BinaryVariable) var);
				break;
			case INTEGER:
				temp = translateIntegerVariable((IntegerVariable) var);
				break;
			case REAL:
				temp = translateRealVariable((RealVariable) var);
				break;
			default:
				throw new UnsupportedOperationException("This variable type is not known.");
			}

			cplexVars.put(name, temp);
		}
	}

	/**
	 * Translates a binary variable into a CPLEX variable and adds it to the
	 * problem.
	 * 
	 * @param variable Binary variable to be translated and added.
	 * @return Translated CPLEX variable.
	 */
	private IloNumVar translateBinaryVariable(BinaryVariable binaryVariable) {
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
	private IloNumVar translateIntegerVariable(IntegerVariable integerVariable) {
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
		Function obj = objective.getObjective().expand();
		IloObjective cplexObj = null;

		try {
			// Translate objective sense
			switch (objective.getType()) {
			case MIN:
				cplexObj = cplex.addMinimize();
			case MAX:
				cplexObj = cplex.addMaximize();
			}

			// Add Terms
			// No nested functions because of expand() call above

			// get the coefficients
			HashMap<String, Double> coefficients = new HashMap<>();
			for (Term term : obj.terms) {
				// TODO: (future work) quadratic terms
				// Get name of the variable in term
				String varName = term.getVar1().getName();

				// Get previous coefficient (if there is one)
				double prevCoef = coefficients.remove(varName);

				// Set new coefficient for the variable (replaces old value)
				coefficients.put(varName, prevCoef + term.getWeight());
			}

			// Add Constant (sum of constants)
			double constant = 0.0;
			for (Constant cons : obj.getConstants()) {
				constant += cons.weight();
			}

			// Set Objective Function
			// TODO: (future work) Quadratic Functions?!
			for (String varName : coefficients.keySet()) {
				cplex.setLinearCoef(cplexObj, coefficients.get(varName), cplexVars.get(varName));
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
		if (objective.getConstraintCount() != objective.getTotalConstraintCount()) {
			throw new Error("All Constraints should be linear constraints!");
		}
		if (objective.getConstraintCount() == 0) {
			return;
		}

		try {
			for (final NormalConstraint constraint : objective.getConstraints()) {

				final IloLinearNumExpr linearNumExpr = cplex.linearNumExpr();

				for (Term term : constraint.getLhsTerms()) {
					linearNumExpr.addTerm(term.getWeight(), cplexVars.get(term.getVar1().getName()));
				}

				switch (constraint.getOp()) {
				case LESS_OR_EQUAL:
					cplex.addLe(constraint.getRhs(), linearNumExpr);
				case GREATER_OR_EQUAL:
					cplex.addGe(constraint.getRhs(), linearNumExpr);
				case EQUAL:
					cplex.addEq(constraint.getRhs(), linearNumExpr);
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
		for (SOS1Constraint constraint : objective.getSOSConstraints()) {
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
		Map<String, Variable<?>> objVars = this.objective.getVariables();

		for (final String varName : this.cplexVars.keySet()) {
			try {
				// Save result value
				// TODO: (future work) configuration for round in SolverConfig?
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
