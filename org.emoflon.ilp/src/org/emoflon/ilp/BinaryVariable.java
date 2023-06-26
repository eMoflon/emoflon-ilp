package org.emoflon.ilp;

public class BinaryVariable implements Variable<Integer> {

	final private String name;
	private boolean value;

	private int upperBound = 1;
	private int lowerBound = 0;

	public BinaryVariable(final String name) {
		this.name = name;

		// Default value: 0 / false
		value = false;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Integer getValue() {
		return (value) ? 1 : 0;
	}

	@Override
	public void setValue(Integer value) {
		this.value = (value != 0) ? true : false;
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
		return VarType.BINARY;
	}

}