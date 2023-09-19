package org.emoflon.ilp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gurobi.GRB;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.DoubleParam;
import gurobi.GRB.IntParam;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBQuadExpr;
import gurobi.GRBVar;

/**
 * This class represents the Gurobi Solver. Here the problem formulation gets
 * translated to be comprehensible for Gurobi and solved.
 *
 */
public class GurobiSolver implements Solver {

	private GRBEnv env;
	private GRBModel model;
	private String outputPath;
	final private SolverConfig config;
	private final HashMap<String, GRBVar> grbVars = new HashMap<>();
	private Objective objective;
	private SolverOutput result;

	/**
	 * The constructor for GurobiSolver.
	 * 
	 * @param config The configuration parameters used for this solver.
	 */
	public GurobiSolver(final SolverConfig config) {
		try {
			this.config = config;
			init();
		} catch (final GRBException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Initializes the solver with the parameters given in the configuration.
	 * 
	 * @throws GRBException
	 */
	private void init() throws GRBException {
		// create new Gurobi Environment
		env = new GRBEnv("Gurobi_ILP.log");

		// set configuration parameters
		// Presolve?
		env.set(IntParam.Presolve, config.presolveEnabled() ? 1 : 0);
		// Random Seed?
		if (config.randomSeedEnabled()) {
			env.set(IntParam.Seed, config.randomSeed());
		}
		// Output?
		if (!config.debugOutputEnabled()) {
			env.set(IntParam.OutputFlag, 0);
		}
		// Tolerance?
		if (config.toleranceEnabled()) {
			env.set(DoubleParam.OptimalityTol, config.tolerance());
			env.set(DoubleParam.IntFeasTol, config.tolerance());
		}
		// Timeout?
		if (config.timeoutEnabled()) {
			env.set(DoubleParam.TimeLimit, config.timeout());
		}

		// create new Gurobi Model/Problem
		model = new GRBModel(env);

		// set output path, if configured
		if (config.outputEnabled()) {
			this.outputPath = config.outputPath();
		}

		grbVars.clear();

	}

	@Override
	public void buildILPProblem(Objective objective) {
		this.objective = objective;

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
		objective.getConstraints().forEach(it -> translateNormalConstraint(it));

		// Translate General Constraints
		objective.getGeneralConstraints().forEach(it -> translateGeneralConstraint(it));

		// Translate SOS Constraints
		objective.getSOSConstraints().forEach(it -> translateSOSConstraint(it));
	}

	/**
	 * Translates the variables to Gurobi variables.
	 * 
	 * @param vars A map of the variables to be translated.
	 */
	private void translateVariables(Map<String, Variable<?>> vars) {
		GRBVar temp = null;
		for (final String name : vars.keySet()) {

			switch (vars.get(name).getType()) {
			case BINARY:
				temp = translateBinaryVariable((BinaryVariable) vars.get(name));
				break;
			case INTEGER:
				temp = translateIntegerVariable((IntegerVariable) vars.get(name));
				break;
			case REAL:
				temp = translateRealVariable((RealVariable) vars.get(name));
				break;
			default:
				throw new UnsupportedOperationException("This variable type is not known.");
			}

			grbVars.put(name, temp);
		}
	}

	/**
	 * Translates the objective function and sets the Gurobi objective.
	 */
	private void translateObjective() {
		// Translate Objective to GRB
		// TODO: (future work) nested Functions OR use "expand" in
		// LinearFunction/QuadraticFunction, which is more efficient?
		Function obj = objective.getObjective().expand();
		GRBQuadExpr expr = new GRBQuadExpr();

		// Add Terms
		for (Term term : obj.getTerms()) {
			if (term instanceof LinearTerm) {
				expr.addTerm(term.getWeight(), grbVars.get(term.getVar1().getName()));
			} else {
				expr.addTerm(term.getWeight(), grbVars.get(term.getVar1().getName()),
						grbVars.get(((QuadraticTerm) term).getVar2().getName()));
			}

		}

		// Add Constant (sum of constants)
		double constant = 0.0;
		for (Constant cons : obj.getConstants()) {
			constant += cons.weight();
		}
		expr.addConstant(constant);

		// Translate objective sense
		int sense = GRB.MINIMIZE;
		if (objective.getType().equals(ObjectiveType.MAX)) {
			sense = GRB.MAXIMIZE;
		}

		// Set model objective
		if (obj instanceof LinearFunction) {
			// Wenn Objective Linear, darf die GRBQuadExpr keinen quadratischen Anteil haben
			// TODO: testen
			if (expr.size() != 0) {
				throw new Error();
			}
			try {
				model.setObjective(expr.getLinExpr(), sense);
			} catch (GRBException e) {
				throw new RuntimeException(e);
			}
		} else {
			try {
				model.setObjective(expr, sense);
			} catch (GRBException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Translates a normal constraint into a Gurobi constraint and adds it to the
	 * model.
	 * 
	 * @param constraint Normal constraint to be translated and added.
	 */
	private void translateNormalConstraint(NormalConstraint constraint) {
		List<Term> lhs = constraint.getLhsTerms();
		char op = translateOp(constraint.getOp());
		double rhs = constraint.getRhs();

		switch (constraint.getType()) {
		case LINEAR:
			GRBLinExpr tempLin = new GRBLinExpr();
			for (Term term : lhs) {
				tempLin.addTerm(term.getWeight(), grbVars.get(term.getVar1().getName()));
			}
			try {
				model.addConstr(tempLin, op, rhs, constraint.toString());
			} catch (GRBException e) {
				throw new RuntimeException(e);
			}
			break;
		case QUADRATIC:
			GRBQuadExpr tempQuad = new GRBQuadExpr();
			for (Term term : lhs) {
				if (term instanceof LinearTerm) {
					tempQuad.addTerm(term.getWeight(), grbVars.get(term.getVar1().getName()));
				} else {
					tempQuad.addTerm(term.getWeight(), grbVars.get(term.getVar1().getName()),
							grbVars.get(((QuadraticTerm) term).getVar2().getName()));
				}
			}
			try {
				model.addQConstr(tempQuad, op, rhs, constraint.toString());
			} catch (GRBException e) {
				throw new RuntimeException(e);
			}
			break;
		case SOS:
			throw new Error("SOS Constraints are a different subclass of constraints!");
		case OR:
			throw new Error("Or Constraints are not normal constraints!");
		case GUROBI_OR:
			throw new Error("Gurobi Or Constraints are general constraints!");
		default:
			break;
		}
	}

	/**
	 * Translates a general constraint into a Gurobi General Constraint and adds it
	 * to the model.
	 * 
	 * @param constraint General constraint to be translated and added.
	 */
	private void translateGeneralConstraint(GeneralConstraint constraint) {
		List<? extends Variable<?>> var = constraint.getVariables();
		Variable<?> res = constraint.getResult();

		switch (constraint.getType()) {
		case LINEAR:
			throw new Error("Linear Constraints are not general constraints!");
		case QUADRATIC:
			throw new Error("Quadratic Constraints are not general constraints!");
		case SOS:
			throw new Error("SOS Constraints are not general constraints!");
		case OR:
			throw new Error("Or Constraints are not general constraints!");
		case GUROBI_OR:
			// TODO: add Gurobi OR constraints
			GRBVar[] grbVars = new GRBVar[var.size()];
			try {
				for (int i = 0; i < var.size(); i++) {
					model.update();
					if (this.grbVars.containsKey(var.get(i).getName())) {
						grbVars[i] = model.getVarByName(var.get(i).getName());
					} else {
						grbVars[i] = translateBinaryVariable((BinaryVariable) var.get(i));
					}
				}
				model.addGenConstrOr(model.addVar(0.0, 1.0, 0.0, GRB.BINARY, res.getName()), grbVars,
						constraint.toString());
			} catch (GRBException e) {
				throw new RuntimeException(e);
			}
		default:
			break;
		}
	}

	/**
	 * Translates a SOS constraint into a Gurobi SOS constraint and adds it to the
	 * model.
	 * 
	 * @param constraint SOS constraint to be translated and added.
	 */
	private void translateSOSConstraint(SOS1Constraint constraint) {
		List<Variable<?>> var = constraint.getVariables();
		GRBVar[] sosVars = new GRBVar[var.size()];
		try {
			for (int i = 0; i < var.size(); i++) {

				model.update();
				if (grbVars.containsKey(var.get(i).getName())) {
					sosVars[i] = model.getVarByName(var.get(i).getName());
				} else if (var.get(i) instanceof BinaryVariable) {
					sosVars[i] = translateBinaryVariable((BinaryVariable) var.get(i));
				} else if (var.get(i) instanceof IntegerVariable) {
					sosVars[i] = translateIntegerVariable((IntegerVariable) var.get(i));
				} else if (var.get(i) instanceof RealVariable) {
					sosVars[i] = translateRealVariable((RealVariable) var.get(i));
				} else {
					throw new Error("This variable type should not be possible!");
				}
			}

			model.addSOS(sosVars, constraint.getWeights(), GRB.SOS_TYPE1);

		} catch (GRBException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Translates a binary variable into a Gurobi variable and adds it to the model.
	 * 
	 * @param variable Binary variable to be translated and added.
	 * @return Translated Gurobi variable.
	 */
	private GRBVar translateBinaryVariable(BinaryVariable variable) {
		try {
			if (variable.getLowerBound() > variable.getUpperBound()) {
				throw new IllegalArgumentException(
						"The lower bound is not allowed to be greater than the upper bound.");
			}
			return model.addVar(variable.getLowerBound(), variable.getUpperBound(), 0.0, GRB.BINARY,
					variable.getName());
		} catch (GRBException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Translates an integer variable into a Gurobi variable and adds it to the
	 * model.
	 * 
	 * @param variable Integer variable to be translated and added.
	 * @return Translated Gurobi variable.
	 */
	private GRBVar translateIntegerVariable(IntegerVariable variable) {
		try {
			int lb = variable.getLowerBound();
			int ub = variable.getUpperBound();

			if (config.boundsEnabled()) {
				if (variable.isDefaultLowerBound()) {
					lb = config.lowerBound();
					variable.setLowerBound((int) lb);
				}
				if (variable.isDefaultUpperBound()) {
					ub = config.upperBound();
					variable.setUpperBound((int) ub);
				}
			}
			if (lb > ub) {
				throw new IllegalArgumentException(
						"The lower bound is not allowed to be greater than the upper bound.");
			}

			return model.addVar(lb, ub, 0.0, GRB.INTEGER, variable.getName());

		} catch (GRBException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Translates a real variable into a Gurobi variable and adds it to the model.
	 * 
	 * @param variable Real variable to be translated and added.
	 * @return Translated Gurobi variable.
	 */
	private GRBVar translateRealVariable(RealVariable variable) {
		try {
			double lb = variable.getLowerBound();
			double ub = variable.getUpperBound();

			if (config.boundsEnabled()) {
				if (variable.isDefaultLowerBound()) {
					lb = config.lowerBound();
					variable.setLowerBound(lb);
				}
				if (variable.isDefaultUpperBound()) {
					ub = config.upperBound();
					variable.setUpperBound(ub);
				}
			}
			if (lb > ub) {
				throw new IllegalArgumentException(
						"The lower bound is not allowed to be greater than the upper bound.");
			}

			return model.addVar(lb, ub, 0.0, GRB.CONTINUOUS, variable.getName());
		} catch (GRBException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Translates the operator used in constraints into a Gurobi operator.
	 * 
	 * @param op Operator to be translated.
	 * @return Gurobi operator value.
	 */
	private char translateOp(Operator op) {
		switch (op) {
		case LESS:
			throw new Error("All constraints with this operator should already have been converted!");
		case LESS_OR_EQUAL:
			return GRB.LESS_EQUAL;
		case EQUAL:
			return GRB.EQUAL;
		case GREATER_OR_EQUAL:
			return GRB.GREATER_EQUAL;
		case GREATER:
			throw new Error("All constraints with this operator should already have been converted!");
		default: // NOT_EQUAL
			throw new Error("All constraints with this operator should already have been converted!");
		}
	}

	@Override
	public SolverOutput solve() {
		SolverStatus status = null;
		double objVal = -1;
		int solCount = -1;

		// Write model into output file
		if (this.outputPath != null) {
			try {
				model.write(outputPath);
			} catch (final GRBException e) {
				e.printStackTrace();
			}
		}

		try {
			model.update();

			model.optimize();

			final int grbStatus = model.get(GRB.IntAttr.Status);
			solCount = model.get(GRB.IntAttr.SolCount);

			switch (grbStatus) {
			case GRB.UNBOUNDED -> {
				status = SolverStatus.UNBOUNDED;
				objVal = model.get(GRB.DoubleAttr.ObjVal);
			}
			case GRB.INF_OR_UNBD -> {
				status = SolverStatus.INF_OR_UNBD;
				objVal = 0;
			}
			case GRB.INFEASIBLE -> {
				status = SolverStatus.INFEASIBLE;
				objVal = 0;
			}
			case GRB.OPTIMAL -> {
				status = SolverStatus.OPTIMAL;
				objVal = model.get(GRB.DoubleAttr.ObjVal);
			}
			case GRB.TIME_LIMIT -> {
				status = SolverStatus.TIME_OUT;
				objVal = model.get(GRB.DoubleAttr.ObjVal);
			}
			}
		} catch (final GRBException e) {
			throw new RuntimeException(e);
		}

		this.result = new SolverOutput(status, objVal, solCount);
		return this.result;
	}

	@Override
	public void updateValuesFromSolution() {
		if (this.result.getStatus() == SolverStatus.INFEASIBLE || this.result.getStatus() == SolverStatus.INF_OR_UNBD) {
			throw new RuntimeException(
					"The problem status is " + this.result.getStatus() + " and therefore no values were found.");
		}
		Map<String, Variable<?>> objVars = this.objective.getVariables();

		for (final String name : this.grbVars.keySet()) {
			try {
				// Save result value
				// TODO: runden konfigurierbar machen?!
				Variable<?> objVar = objVars.get(name);
				if (objVar instanceof BinaryVariable) {
					long val = Math.round(this.grbVars.get(name).get(DoubleAttr.X));
					if (val >= 1) {
						((BinaryVariable) objVar).setValue(1);
					} else {
						((BinaryVariable) objVar).setValue(0);
					}
				} else if (objVar instanceof IntegerVariable) {
					((IntegerVariable) objVar).setValue((int) Math.round(this.grbVars.get(name).get(DoubleAttr.X)));
				} else if (objVar instanceof RealVariable) {
					((RealVariable) objVar).setValue(this.grbVars.get(name).get(DoubleAttr.X));
				} else {
					throw new Error("This variable type is not implemented!");
				}
			} catch (final GRBException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void terminate() {
		model.terminate();
		model.dispose();
		try {
			env.dispose();
		} catch (final GRBException e) {
			e.printStackTrace();
		}
		// env.release();
	}

	@Override
	public void reset() {
		try {
			init();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

}