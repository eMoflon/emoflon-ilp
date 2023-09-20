package org.emoflon.ilp;

import java.util.Map;
import java.util.HashMap;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_iocp;
import org.gnu.glpk.glp_prob;

/**
 * This class represents the Glpk Solver. Here the problem formulation gets
 * translated to be comprehensible for Glpk and solved.
 *
 */
public class GlpkSolver implements Solver {

	private glp_prob model;
	private glp_iocp iocp;
	private String outputPath;
	final private SolverConfig config;
	private Problem problem;
	private SolverOutput result;
	private Map<String, Integer> indexNameMap;

	/**
	 * The constructor for GlpkSolver.
	 * 
	 * @param config The configuration parameters used for this solver.
	 * @see SolverConfig
	 */
	public GlpkSolver(final SolverConfig config) {
		this.config = config;
		this.indexNameMap = new HashMap<>();
		init();
	}

	/**
	 * Initializes the solver with the parameters given in the configuration.
	 */
	private void init() {
		GLPK.glp_free_env();

		// Create problem
		model = GLPK.glp_create_prob();
		GLPK.glp_set_prob_name(model, "Glpk_ILP");

		// Configuration
		iocp = new glp_iocp();
		GLPK.glp_init_iocp(iocp);
		// Set configuration parameters
		// Presolve?
		iocp.setPresolve(config.presolveEnabled() ? GLPK.GLP_ON : GLPK.GLP_OFF);
		// Random Seed?
		// not supported in glpk for Java
		// --seed value is option fo glpsol
		// Debug Output?
		if (!config.debugOutputEnabled()) {
			GLPK.glp_term_out(GLPK.GLP_OFF);
		}
		// Output?
		if (config.outputEnabled()) {
			this.outputPath = config.outputPath();
		}
		// Tolerance?
		if (config.toleranceEnabled()) {
			iocp.setTol_int(config.tolerance());
			iocp.setTol_obj(config.tolerance());
			iocp.setMip_gap(config.tolerance());
		}
		// Timeout?
		if (config.timeoutEnabled()) {
			// GLPK expects milliseconds
			iocp.setTm_lim((int) config.timeout() * 1000);
		}

	}

	@Override
	public void buildILPProblem(Problem problem) {
		this.problem = problem;

		// Quadratic Constraints or Functions are not supported by GLPK
		if (problem.getConstraints().stream().anyMatch(QuadraticConstraint.class::isInstance)
				|| (problem.getObjective() instanceof QuadraticFunction)) {
			throw new IllegalArgumentException("GLPK does not support quadratic constraints and quadratic functions!");
		}
		// General Constraints are not supported
		// TODO: (future work) convert OrVarsConstraints to OrConstraints or remove
		if (problem.getGenConstraintCount() != 0) {
			throw new IllegalArgumentException("General Constraints are not supported by GLPK.");
		}

		// Substitute Or Constraints
		problem.substituteOr();

		// Substitute <, >, != Operators
		problem.substituteOperators();

		// Substitute SOS1 Constraints
		problem.substituteSOS1();

		// Translate Variables
		translateVariables(problem.getVariables());

		// Translate Objective
		translateObjective();

		// Translate Constraints
		translateConstraints();

	}

	/**
	 * Translates the variables to Glpk variables.
	 * 
	 * @param vars A map of the variables to be translated.
	 */
	private void translateVariables(Map<String, Variable<?>> vars) {
		if (vars.size() == 0) {
			return;
		}

		int j = 1;
		GLPK.glp_add_cols(model, vars.size());
		for (final String name : vars.keySet()) {
			Variable<?> var = vars.get(name);
			indexNameMap.put(name, j);

			double lb = var.getLowerBound().doubleValue();
			double ub = var.getUpperBound().doubleValue();

			GLPK.glp_set_col_name(model, j, name);

			switch (var.getType()) {
			case BINARY:
				translateVariable(j, GLPK.GLP_BV, lb, ub);
				break;
			case INTEGER:
				// Check if other bounds are defined in the solver config
				if (config.boundsEnabled()) {
					if (((IntegerVariable) var).isDefaultLowerBound()) {
						lb = config.lowerBound();
						((IntegerVariable) var).setLowerBound((int) lb);
					}
					if (((IntegerVariable) var).isDefaultUpperBound()) {
						ub = config.upperBound();
						((IntegerVariable) var).setUpperBound((int) ub);
					}
				}
				translateVariable(j, GLPK.GLP_IV, lb, ub);
				break;
			case REAL:
				// Check if other bounds are defined in the solver config
				if (config.boundsEnabled()) {
					if (((RealVariable) var).isDefaultLowerBound()) {
						lb = config.lowerBound();
						((RealVariable) var).setLowerBound(lb);
					}
					if (((RealVariable) var).isDefaultUpperBound()) {
						ub = config.upperBound();
						((RealVariable) var).setUpperBound(ub);
					}
				}
				translateVariable(j, GLPK.GLP_CV, lb, ub);
				break;
			default:
				throw new UnsupportedOperationException("This variable type is not known.");
			}

			j++;
		}
	}

	/**
	 * Translates a single variable and sets the column values for the glpk problem.
	 * 
	 * @param index   Index of this variable.
	 * @param varType Type of this variable (binary, integer, continuous).
	 * @param lb      Lower bound of this variable.
	 * @param ub      Upper bound of this variable.
	 */
	private void translateVariable(int index, int varType, double lb, double ub) {
		if (lb > ub) {
			throw new IllegalArgumentException("The lower bound is not allowed to be greater than the upper bound.");
		}
		GLPK.glp_set_col_kind(model, index, varType);
		if (lb == ub) {
			GLPK.glp_set_col_bnds(model, index, GLPK.GLP_FX, lb, ub);
		} else {
			GLPK.glp_set_col_bnds(model, index, GLPK.GLP_DB, lb, ub);
		}
	}

	/**
	 * Translates the objective function and sets the glpk objective.
	 */
	private void translateObjective() {
		if (problem == null) {
			return;
		}

		// Get objective function and expand nested functions
		Function obj = problem.getObjective().expand();
		if (!obj.nestedFunctions.isEmpty()) {
			throw new Error("There should be no nested functions left after expand().");
		}

		// Set sense of the objective (min/max)
		switch (problem.getType()) {
		case MIN:
			GLPK.glp_set_obj_dir(model, GLPK.GLP_MIN);
			break;
		case MAX:
			GLPK.glp_set_obj_dir(model, GLPK.GLP_MAX);
			break;
		}

		// Set constant
		double constant = 0;
		for (Constant c : obj.getConstants()) {
			constant += c.weight();
		}
		GLPK.glp_set_obj_coef(model, 0, constant);

		for (Term term : obj.terms) {
			// Get index of the variable in term
			int index = indexNameMap.get(term.getVar1().getName());

			// Get previous coefficient (if there is one)
			double prevCoef = GLPK.glp_get_obj_coef(model, index);

			// Set new coefficient for the variable
			GLPK.glp_set_obj_coef(model, index, prevCoef + term.getWeight());
		}
	}

	/**
	 * Translates the constraints and sets the row values of the glpk problem.
	 */
	private void translateConstraints() {
		if (problem.getConstraintCount() != problem.getTotalConstraintCount()) {
			throw new Error("All Constraints should be linear constraints!");
		}
		if (problem.getConstraintCount() == 0) {
			return;
		}

		// Add rows according to the constraint count
		GLPK.glp_add_rows(model, problem.getConstraintCount());

		int counter = 1;
		for (final NormalConstraint constraint : problem.getConstraints()) {

			Map<Variable<?>, Double> weights = new HashMap<>();
			for (Term term : constraint.getLhsTerms()) {
				weights.put(term.getVar1(), weights.getOrDefault(term.getVar1(), 0.0) + term.getWeight());
			}

			final int size = weights.size();
			final SWIGTYPE_p_int vars = GLPK.new_intArray(size + 1);
			final SWIGTYPE_p_double coeffs = GLPK.new_doubleArray(size + 1);

			int varIndex = 1;
			for (final Variable<?> var : weights.keySet()) {
				GLPK.intArray_setitem(vars, varIndex, indexNameMap.get(var.getName()));
				GLPK.doubleArray_setitem(coeffs, varIndex, weights.get(var));
				varIndex++;
			}

			GLPK.glp_set_row_name(model, counter, constraint.toString());
			GLPK.glp_set_mat_row(model, counter, size, vars, coeffs);
			GLPK.glp_set_row_bnds(model, counter, translateOp(constraint.getOp()), constraint.getRhs(),
					constraint.getRhs());

			counter++;
		}
	}

	/**
	 * Translates the operator used in constraints into a glpk operator.
	 * 
	 * @param op Operator to be translated.
	 * @return Glpk operator value.
	 */
	private int translateOp(Operator op) {
		switch (op) {
		case LESS:
			throw new Error("After converting < to <= with epsilon, there should be no constraint with < left.");
		case LESS_OR_EQUAL:
			return GLPK.GLP_UP;
		case EQUAL:
			return GLPK.GLP_FX;
		case GREATER_OR_EQUAL:
			return GLPK.GLP_LO;
		case GREATER:
			throw new Error("After converting > to >= with epsilon, there should be no constraint with > left.");
		case NOT_EQUAL:
			throw new Error(
					"After converting != to substitution constraints, there should be no constraint with != left.");
		default:
			throw new UnsupportedOperationException("Unknown operator.");
		}
	}

	@Override
	public SolverOutput solve() {

		// Write the model in a file if output was enabled
		if (this.outputPath != null) {
			GLPK.glp_write_lp(model, null, this.outputPath);
		}

		// Solve
		// solve MIP problem with the branch-and-cut method
		// returns 0, GLP_EBOUND, GLP_EROOT, GLP_ENOPFS, GLP_ENODFS, GLP_EFAIL,
		// GLP_EMIPGAP, GLP_ETMLIM, GLP_ESTOP

		// For GLPK all SolverConfigs have to set presolve to true.
		// If set to false, an error occurs because glpk expects the problem object
		// (model) to contain an optimal solution to the LP relaxation.
		final int solveStatus = GLPK.glp_intopt(model, iocp);

		// MIP problem instance successfully solved (does not have to be the optimal
		// solution)
		final boolean solved = solveStatus == 0;
		// time limit exceeded -> search prematurely terminated
		final boolean timeOut = solveStatus == GLPK.GLP_ETMLIM;

		// not a return value that glp_intopt returns
		// invalid basis
		final boolean invalid = solveStatus == GLPK.GLP_EBADB;
		// no primal feasible solution
		final boolean noPrimalFeasSol = solveStatus == GLPK.GLP_ENOPFS;

		// unable to start search, LP relaxation of MIP prob has no dual feasible
		// solution
		final boolean noDualFeasSol = solveStatus == GLPK.GLP_ENODFS;

		// get_status returns the generic status of the current basic solution
		// GLP_OPT, GLP_FEAS, GLP_INFEAS, GLP_NOFEAS, GLP_UNBND, GLP_UNDEF
		final int modelStatus = GLPK.glp_get_status(model);

		final boolean optimal = modelStatus == GLPK.GLP_OPT;
		final boolean feasible = modelStatus == GLPK.GLP_FEAS;
		final boolean infeasible = modelStatus == GLPK.GLP_INFEAS;
		final boolean noFeasibleSol = modelStatus == GLPK.GLP_NOFEAS;
		final boolean unbounded = modelStatus == GLPK.GLP_UNBND;
		// final boolean undefined = modelStatus == GLPK.GLP_UNDEF;

		// mip_status returns the status of a MIP solution found by the MIP solver
		// GLP_UNDEF, GLP_OPT, GLP_FEAS, GLP_NOFEAS
		final int mipModelStatus = GLPK.glp_mip_status(model);

		// final boolean mip_undefined = mipModelStatus == GLPK.GLP_UNDEF;
		final boolean mip_optimal = mipModelStatus == GLPK.GLP_OPT;
		final boolean mip_feasible = mipModelStatus == GLPK.GLP_FEAS;
		final boolean mip_noFeasibleSol = mipModelStatus == GLPK.GLP_NOFEAS;

		SolverStatus status = null;
		int solutionCount = -1;

		if (solved && (optimal || mip_optimal)) {
			status = SolverStatus.OPTIMAL;
			solutionCount = 1;
		} else if (unbounded) {
			status = SolverStatus.UNBOUNDED;
			solutionCount = 0;
		} else if (timeOut) {
			status = SolverStatus.TIME_OUT;
			solutionCount = solved ? 1 : 0;
		} else if (infeasible || noPrimalFeasSol || noFeasibleSol || modelStatus == 1 || mip_noFeasibleSol
				|| noDualFeasSol) {
			status = SolverStatus.INFEASIBLE;
			solutionCount = 0;
		} else if (invalid) {
			status = SolverStatus.INF_OR_UNBD;
			solutionCount = 0;
		} else if (feasible || mip_feasible) {
			status = SolverStatus.FEASIBLE;
		} else {
			throw new RuntimeException("GLPK: Solver status could not be determined.");
		}

		this.result = new SolverOutput(status, GLPK.glp_mip_obj_val(model), solutionCount);
		return this.result;
	}

	@Override
	public void updateValuesFromSolution() {

		Map<String, Variable<?>> objVars = this.problem.getVariables();

		for (final String name : problem.getVariables().keySet()) {

			// Save result value
			// TODO: (future work) configuration for round in SolverConfig
			Variable<?> objVar = objVars.get(name);
			if (objVar instanceof BinaryVariable) {
				long val = Math.round(GLPK.glp_mip_col_val(model, indexNameMap.get(objVar.getName())));
				if (val >= 1) {
					((BinaryVariable) objVar).setValue(1);
				} else {
					((BinaryVariable) objVar).setValue(0);
				}
			} else if (objVar instanceof IntegerVariable) {
				((IntegerVariable) objVar)
						.setValue((int) Math.round(GLPK.glp_mip_col_val(model, indexNameMap.get(objVar.getName()))));
			} else if (objVar instanceof RealVariable) {
				((RealVariable) objVar).setValue(GLPK.glp_mip_col_val(model, indexNameMap.get(objVar.getName())));
			} else {
				throw new Error("This variable type is not implemented!");
			}

		}
	}

	@Override
	public void terminate() {
		model.delete();
		iocp.delete();
	}

	@Override
	public void reset() {
		init();
	}

}