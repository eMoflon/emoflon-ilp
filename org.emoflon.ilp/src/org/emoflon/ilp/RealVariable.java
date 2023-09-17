package org.emoflon.ilp;

/**
 * This class represents real variables.
 *
 */
public class RealVariable implements Variable<Double> {

	final private String name;
	private double value;

	private double upperBound = 10_000;
	private double lowerBound = -10_000;

	/**
	 * Constructor for Real Variable.
	 * 
	 * @param name The name of this real variable.
	 */
	public RealVariable(final String name) {
		this.name = name;

		// default value: 0.0
		value = 0.0d;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Double getValue() {
		return value;
	}

	@Override
	public void setValue(Double value) {
		this.value = value;
	}

	@Override
	public Double getUpperBound() {
		return upperBound;
	}

	@Override
	public void setUpperBound(Double bound) {
		this.upperBound = bound;
	}

	@Override
	public Double getLowerBound() {
		return lowerBound;
	}

	@Override
	public void setLowerBound(Double bound) {
		this.lowerBound = bound;
	}

	@Override
	public VarType getType() {
		return VarType.REAL;
	}

}