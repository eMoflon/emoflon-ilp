package org.emoflon.ilp;

import java.util.List;
import java.util.ArrayList;

public record LinearFunction(List<Term> terms, List<Constant> constantTerms) {

	public LinearFunction() {
		this(new ArrayList<Term>(), new ArrayList<Constant>());
	}

	public void addTerm(Term term) {
		this.terms.add(term);
	}

	public void addConstant(Constant constant) {
		this.constantTerms.add(constant);
	}
}