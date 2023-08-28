package org.emoflon.ilp;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

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
		// TODO: Hilfsfunktionen einfuehren? Ist etwas lang...
		this.objective = objective;
		try {
			// Translate Constraints
			List<NormalConstraint> normalConstraints = new ArrayList<NormalConstraint>();
			normalConstraints.addAll(objective.getConstraints());
			List<SOS1Constraint> sosConstraints = new ArrayList<SOS1Constraint>();

			// Or Constraints
			// Substitute Or Constraints with Linear Constraints and SOS1 Constraints
			for (OrConstraint constraint : objective.getOrConstraints()) {
				List<Constraint> converted = constraint.convert();
				converted.forEach(it -> System.out.println(it.toString()));
				normalConstraints.addAll(converted.stream().filter(NormalConstraint.class::isInstance)
						.map(LinearConstraint.class::cast).collect(Collectors.toList()));
				sosConstraints.addAll(converted.stream().filter(SOS1Constraint.class::isInstance)
						.map(SOS1Constraint.class::cast).collect(Collectors.toList()));
			}

			// Substitute <, >, !=
			List<NormalConstraint> opSubstitution = new ArrayList<NormalConstraint>();
			List<NormalConstraint> delete = new ArrayList<NormalConstraint>();
			for (NormalConstraint constraint : normalConstraints) {
				List<Constraint> substitution = constraint.convertOperator();
				if (!substitution.isEmpty()) {
					delete.add(constraint);
				}
				opSubstitution.addAll(substitution.stream().filter(LinearConstraint.class::isInstance)
						.map(LinearConstraint.class::cast).collect(Collectors.toList()));
				opSubstitution.addAll(substitution.stream().filter(QuadraticConstraint.class::isInstance)
						.map(QuadraticConstraint.class::cast).collect(Collectors.toList()));
				sosConstraints.addAll(substitution.stream().filter(SOS1Constraint.class::isInstance)
						.map(SOS1Constraint.class::cast).collect(Collectors.toList()));
			}
			// delete converted constraints
			normalConstraints.removeAll(delete);
			// add substitutions
			normalConstraints.addAll(opSubstitution);

			// Add substitutions to objective
			normalConstraints.forEach(constraint -> objective.add(constraint));
			sosConstraints.forEach(constraint -> objective.add(constraint));

			// Initialize decision variables and objective
			// Translate Variables
			Map<String, Variable<?>> vars = objective.getVariables();
			GRBVar temp = null;
			for (Variable<?> var : vars.values()) {
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
			// TODO: nested Functions OR use "expand" in LinearFunction/QuadraticFunction
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
				model.setObjective(expr.getLinExpr(), sense);
			} else {
				model.setObjective(expr, sense);
			}

			// Translate Normal Constraints
			for (NormalConstraint constraint : normalConstraints) {
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
					throw new Error("SOS Constraints are a different subclass of constraints!");
				case OR:
					throw new Error("Or Constraints are general constraints!");
				}
			}

			// Translate General Constraints
			// TODO: rausnehmen? -> OrVarsConstraint
			for (GeneralConstraint constraint : objective.getGeneralConstraints()) {
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
			for (SOS1Constraint constraint : sosConstraints) {
				List<Variable<?>> var = constraint.getVariables();
				GRBVar[] grbVars = new GRBVar[var.size()];
				for (int i = 0; i < var.size(); i++) {
					if (var.get(i) instanceof BinaryVariable) {
						grbVars[i] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, var.get(i).getName());
					} else if (var.get(i) instanceof IntegerVariable) {
						grbVars[i] = model.addVar(((IntegerVariable) var.get(i)).getLowerBound(),
								((IntegerVariable) var.get(i)).getUpperBound(), 0.0, GRB.INTEGER, var.get(i).getName());
					} else if (var.get(i) instanceof RealVariable) {
						// RealVariable
						grbVars[i] = model.addVar(((RealVariable) var.get(i)).getLowerBound(),
								((RealVariable) var.get(i)).getUpperBound(), 0.0, GRB.CONTINUOUS, var.get(i).getName());
					} else {
						throw new Error("This variable type should not be possible!");
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

		return new SolverOutput(status, objVal, solCount);
	}

	@Override
	public Objective updateValuesFromSolution() {
		// TODO Auto-generated method stub
		/*
		 * Notizen Besprechung: generische Lösung -> lambda übergeben -> lambda auf alle
		 * Werte anwenden
		 */
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