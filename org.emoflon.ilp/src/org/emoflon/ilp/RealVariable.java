package org.emoflon.ilp;

/**
 * This class represents real variables.
 *
 */
public class RealVariable implements Variable<Double> {

	final private String name;
	private double value;

	private boolean defaultUpperBound = true;
	private boolean defaultLowerBound = true;

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
		this.defaultUpperBound = false;
	}

	/**
	 * Returns if the upper bound of this variable is the default bound (true) or of
	 * it got changed (false)
	 * 
	 * @return True, if the upper bound of this variable is still the default. False
	 *         otherwise.
	 */
	public boolean isDefaultUpperBound() {
		return this.defaultUpperBound;
	}

	@Override
	public Double getLowerBound() {
		return lowerBound;
	}

	@Override
	public void setLowerBound(Double bound) {
		this.lowerBound = bound;
		this.defaultLowerBound = false;
	}

	/**
	 * Returns if the lower bound of this variable is the default bound (true) or of
	 * it got changed (false)
	 * 
	 * @return True, if the lower bound of this variable is still the default. False
	 *         otherwise.
	 */
	public boolean isDefaultLowerBound() {
		return this.defaultLowerBound;
	}

	@Override
	public VarType getType() {
		return VarType.REAL;
	}

}