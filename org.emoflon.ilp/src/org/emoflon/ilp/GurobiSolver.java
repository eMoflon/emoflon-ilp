package org.emoflon.ilp;

import java.util.HashMap;

import gurobi.GRB;
import gurobi.GRB.DoubleParam;
import gurobi.GRB.IntParam;
import gurobi.GRBEnv;
import gurobi.GRBException;
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
	public void buildILPProbelm() {
		// TODO Auto-generated method stub

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
		/* Notizen Besprechung:
		 * generische Lösung
		 * -> lambda übergeben
		 * -> lambda auf alle Werte anwenden
		 */
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