package org.emoflon.ilp;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import gurobi.GRB;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.DoubleParam;
import gurobi.GRB.IntParam;
import gurobi.GRBLinExpr;
import gurobi.GRBQuadExpr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class GurobiSolver extends Solver {

	private GRBEnv env;
	private GRBModel model;
	private String outputPath;
	final private SolverConfig config;
	private final HashMap<String, GRBVar> grbVars = new HashMap<>();

	public GurobiSolver(final SolverConfig config) throws GRBException {
		this.config = config;
		init();
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
		// TODO: Hilfsfunktionen einfuehren? Ist etwas lang...
		try {
			// Initialize decision variables and objective
			// Translate Variables
			Set<Variable<?>> vars = objective.getVariables();
			GRBVar temp = null;
			for (Variable<?> var : vars) {
				switch (var.getType()) {
				case BINARY:
					temp = model.addVar(var.getLowerBound().doubleValue(), var.getUpperBound().doubleValue(), 0,
							GRB.BINARY, var.getName());
					break;
				case INTEGER:
					temp = model.addVar(var.getLowerBound().doubleValue(), var.getUpperBound().doubleValue(), 0,
							GRB.INTEGER, var.getName());
					break;
				case REAL:
					temp = model.addVar(var.getLowerBound().doubleValue(), var.getUpperBound().doubleValue(), 0,
							GRB.CONTINUOUS, var.getName());
					break;
				}
				grbVars.put(var.getName(), temp);
			}

			// Translate Objective to GRB
			// TODO: add support for quadratic objective functions
			Function obj = objective.getObjective().expand();
			GRBQuadExpr expr = new GRBQuadExpr();

			// TODO: nested Functions OR use "expand" in LinearFunction/QuadraticFunction

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
				model.setObjective(expr.getLinExpr(), sense);
			} else {
				model.setObjective(expr, sense);
			}

			// Translate Constraints
			for (Constraint constraint : objective.getConstraints()) {
				List<Term> lhs = constraint.getLhsTerms();
				char op = translateOp(constraint.getOp());
				double rhs = constraint.getRhs();

				switch (constraint.getType()) {
				case LINEAR:
					GRBLinExpr tempLin = new GRBLinExpr();
					for (Term term : lhs) {
						tempLin.addTerm(term.getWeight(), grbVars.get(term.getVar1().getName()));
					}
					model.addConstr(tempLin, op, rhs, constraint.toString());
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
					model.addQConstr(tempQuad, op, rhs, constraint.toString());
					break;
				case SOS:
					// TODO: add SOS constraints
					throw new Error("Not yet implemented!");
				case OR:
					throw new Error("Or Constraints are general constraints!");
				}
			}

			// Translate General Constraints
			for (GeneralConstraint constraint : objective.getGeneralConstraints()) {
				List<? extends Variable<?>> var = constraint.getVariables();
				Variable<?> res = constraint.getResult();

				switch (constraint.getType()) {
				case LINEAR:
					throw new Error("Linear Constraints are not general constraints!");
				case QUADRATIC:
					throw new Error("Quadratic Constraints are not general constraints!");
				case SOS:
					// TODO: add SOS constraints
					throw new Error("Not yet implemented!");
				case OR:
					// TODO: add OR constraints
					GRBVar[] grbVars = new GRBVar[var.size()];
					for (int i = 0; i < var.size(); i++) {
						grbVars[i] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, var.get(i).getName());
					}
					model.addGenConstrOr(model.addVar(0.0, 1.0, 0.0, GRB.BINARY, res.getName()), grbVars,
							constraint.toString());
				}
			}

			// Translate SOS Constraints
			for (SOS1Constraint constraint : objective.getSOSConstraints()) {
				List<Variable<?>> var = constraint.getVariables();
				GRBVar[] grbVars = new GRBVar[var.size()];
				for (int i = 0; i < var.size(); i++) {
					if (var instanceof BinaryVariable) {
						grbVars[i] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, var.get(i).getName());
					} else if (var instanceof IntegerVariable) {
						grbVars[i] = model.addVar(((IntegerVariable) var).getLowerBound(),
								((IntegerVariable) var).getUpperBound(), 0.0, GRB.INTEGER, var.get(i).getName());
					} else {
						// RealVariable
						grbVars[i] = model.addVar(((RealVariable) var).getLowerBound(),
								((RealVariable) var).getUpperBound(), 0.0, GRB.CONTINUOUS, var.get(i).getName());
					}
				}
				model.addSOS(grbVars, constraint.getWeights(), GRB.SOS_TYPE1);
			}

		} catch (GRBException e) {
			e.printStackTrace();
		}

	}

	private char translateOp(Operator op) {
		switch (op) {
		case LESS:
			throw new Error("Not yet implemented!");
		case LESS_OR_EQUAL:
			return GRB.LESS_EQUAL;
		case EQUAL:
			return GRB.EQUAL;
		case GREATER_OR_EQUAL:
			return GRB.GREATER_EQUAL;
		case GREATER:
			throw new Error("Not yet implemented!");
		default: // NOT_EQUAL
			throw new Error("Not yet implemented!");
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

		return new SolverOutput(status, objVal, solCount);
	}

	@Override
	public void updateValuesFromSolution() {
		// TODO Auto-generated method stub
		/*
		 * Notizen Besprechung: generische Lösung -> lambda übergeben -> lambda auf alle
		 * Werte anwenden
		 */

		for (final String name : this.grbVars.keySet()) {

			try {
				// Get value of the ILP variable and round it (to eliminate small deltas)
				double result = Math.round(this.grbVars.get(name).get(DoubleAttr.X));
				// Save result value
				// TODO
			} catch (final GRBException e) {
				throw new RuntimeException(e);
			}

		}
		// Solver reset will be handled by the GipsEngine afterward
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
		env.release();
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