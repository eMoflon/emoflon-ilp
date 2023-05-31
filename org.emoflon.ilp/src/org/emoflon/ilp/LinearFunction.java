package org.emoflon.ilp;

import java.util.List;

/*
public class LinearFunction {
	
	private List<Term> terms = new ArrayList<Term>();
	
	// TODO
}
*/

public record LinearFunction(List<Term> terms, List<Constant> constantTerms) {

}