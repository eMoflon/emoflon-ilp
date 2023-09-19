package org.emoflon.ilp;

/**
 * This class represents integer variables.
 *
 */
public class IntegerVariable implements Variable<Integer> {

	final private String name;
	private int value;

	private boolean defaultUpperBound = true;
	private boolean defaultLowerBound = true;

	private int upperBound = 10_000;
	private int lowerBound = -10_000;

	/**
	 * Constructor for Integer Variable.
	 * 
	 * @param name The name of this integer variable.
	 */
	public IntegerVariable(final String name) {
		this.name = name;

		// Default value: 0
		value = 0;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Integer getValue() {
		return value;
	}

	@Override
	public void setValue(Integer value) {
		this.value = value;
	}

	@Override
	public Integer getUpperBound() {
		return upperBound;
	}

	@Override
	public void setUpperBound(Integer bound) {
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
	public Integer getLowerBound() {
		return lowerBound;
	}

	@Override
	public void setLowerBound(Integer bound) {
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
		return VarType.INTEGER;
	}

}