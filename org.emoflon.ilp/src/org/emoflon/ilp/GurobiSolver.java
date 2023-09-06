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

public class GurobiSolver implements Solver {

	private GRBEnv env;
	private GRBModel model;
	private String outputPath;
	final private SolverConfig config;
	private final HashMap<String, GRBVar> grbVars = new HashMap<>();
	private Objective objective;
	private SolverOutput result;

	public GurobiSolver(final SolverConfig config) {
		try {
			this.config = config;
			init();
		} catch (final GRBException e) {
			throw new RuntimeException(e);
		}

	}

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
		if (!config.outputEnabled()) {
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
		translateObjective(objective);

		// Translate Normal Constraints
		objective.getConstraints().forEach(it -> translateNormalConstraint(it));

		// Translate General Constraints
		// TODO: rausnehmen? -> OrVarsConstraint
		objective.getGeneralConstraints().forEach(it -> translateGeneralConstraint(it));

		// Translate SOS Constraints
		objective.getSOSConstraints().forEach(it -> translateSOSConstraint(it));
	}

	private void translateVariables(Map<String, Variable<?>> vars) {
		GRBVar temp = null;
		for (Variable<?> var : vars.values()) {

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
			}

			grbVars.put(var.getName(), temp);
		}
	}

	private void translateObjective(Objective objective) {
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
			throw new Error("Or Constraints are general constraints!");
		}
	}

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
			// TODO: add Gurobi OR constraints
			GRBVar[] grbVars = new GRBVar[var.size()];
			try {
				for (int i = 0; i < var.size(); i++) {
					grbVars[i] = translateBinaryVariable((BinaryVariable) var.get(i));
				}
				model.addGenConstrOr(model.addVar(0.0, 1.0, 0.0, GRB.BINARY, res.getName()), grbVars,
						constraint.toString());
			} catch (GRBException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void translateSOSConstraint(SOS1Constraint constraint) {
		List<Variable<?>> var = constraint.getVariables();
		GRBVar[] grbVars = new GRBVar[var.size()];
		try {
			for (int i = 0; i < var.size(); i++) {

				if (var.get(i) instanceof BinaryVariable) {
					grbVars[i] = translateBinaryVariable((BinaryVariable) var.get(i));
				} else if (var.get(i) instanceof IntegerVariable) {
					grbVars[i] = translateIntegerVariable((IntegerVariable) var.get(i));
				} else if (var.get(i) instanceof RealVariable) {
					grbVars[i] = translateRealVariable((RealVariable) var.get(i));
				} else {
					throw new Error("This variable type should not be possible!");
				}
			}

			model.addSOS(grbVars, constraint.getWeights(), GRB.SOS_TYPE1);

		} catch (GRBException e) {
			throw new RuntimeException(e);
		}
	}

	private GRBVar translateBinaryVariable(BinaryVariable variable) {
		try {
			return model.addVar(variable.getLowerBound(), variable.getUpperBound(), 0.0, GRB.BINARY,
					variable.getName());
		} catch (GRBException e) {
			throw new RuntimeException(e);
		}
	}

	private GRBVar translateIntegerVariable(IntegerVariable variable) {
		try {
			if (config.boundsEnabled()) {
				return model.addVar(config.lowerBound(), config.upperBound(), 0.0, GRB.INTEGER, variable.getName());
			} else {
				return model.addVar(variable.getLowerBound(), variable.getUpperBound(), 0.0, GRB.INTEGER,
						variable.getName());
			}
		} catch (GRBException e) {
			throw new RuntimeException(e);
		}
	}

	private GRBVar translateRealVariable(RealVariable variable) {
		try {
			if (config.boundsEnabled()) {
				return model.addVar(config.lowerBound(), config.upperBound(), 0.0, GRB.CONTINUOUS, variable.getName());
			} else {
				return model.addVar(variable.getLowerBound(), variable.getUpperBound(), 0.0, GRB.CONTINUOUS,
						variable.getName());
			}
		} catch (GRBException e) {
			throw new RuntimeException(e);
		}
	}

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
	public Objective updateValuesFromSolution() {
		// TODO Auto-generated method stub
		/*
		 * Notizen Besprechung: generische Lösung -> lambda übergeben -> lambda auf alle
		 * Werte anwenden
		 */
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

		return this.objective;
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

	public Objective getObjective() {
		return this.objective;
	}

}