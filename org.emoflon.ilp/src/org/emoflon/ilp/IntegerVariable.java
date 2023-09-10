package org.emoflon.ilp;

public class IntegerVariable implements Variable<Integer> {

	final private String name;
	private int value;

	// TODO: Add default bounds! Gips: 10_000
	private int upperBound = 10_000;
	private int lowerBound = -10_000;

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
	}

	@Override
	public Integer getLowerBound() {
		return lowerBound;
	}

	@Override
	public void setLowerBound(Integer bound) {
		this.lowerBound = bound;
	}

	@Override
	public VarType getType() {
		return VarType.INTEGER;
	}

}