package org.emoflon.ilp;

import java.util.HashMap;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.glp_iocp;
import org.gnu.glpk.glp_prob;

public class GlpkSolver implements Solver {

	private glp_prob model;
	private glp_iocp iocp;
	private String outputPath;
	final private SolverConfig config;
	private final HashMap<String, Variable> vars = new HashMap<>();
	private Objective objective;
	private SolverOutput result;

	public GlpkSolver(final SolverConfig config) {
		this.config = config;
		init();
	}

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
		// TODO: scheinbar nicht unterstützt?

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
			// TODO: bei gips *1000 ? Sekunden vs Millisekunden
			iocp.setTm_lim((int) config.timeout());
		}

	}

	@Override
	public void buildILPProblem(Objective objective) {
		// TODO Auto-generated method stub

		// Substitute Or Constraints
		objective.substituteOr();

		// Substitute <, >, != Operators
		objective.substituteOperators();

		// Substitute SOS1 Constraints
		objective.substituteSOS1();

		//
	}

	@Override
	public SolverOutput solve() {
		// TODO Auto-generated method stub

		// Write the model in a file if the output was enabled
		if (this.outputPath != null) {
			GLPK.glp_write_lp(model, null, this.outputPath);
		}

		// Solve
		final int ret = GLPK.glp_intopt(model, iocp);
		final int modelStatus = GLPK.glp_get_status(model);
		final int mipModelStatus = GLPK.glp_mip_status(model);

		return null;
	}

	@Override
	public Objective updateValuesFromSolution() {
		// TODO Auto-generated method stub
		return null;
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