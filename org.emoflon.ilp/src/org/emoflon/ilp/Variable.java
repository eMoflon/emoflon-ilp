package org.emoflon.ilp;

public interface Variable<T extends Number> {

	public String getName();

	public T getValue();

	public void setValue(final T value);

	public T getUpperBound();

	public void setUpperBound(final T bound);

	public T getLowerBound();

	public void setLowerBound(final T bound);

	public VarType getType();

}
