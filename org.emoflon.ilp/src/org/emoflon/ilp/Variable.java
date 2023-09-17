package org.emoflon.ilp;

public interface Variable<T extends Number> {

	/**
	 * Returns the name of the variable.
	 * 
	 * @return The name of this variable.
	 */
	public String getName();

	/**
	 * Returns the current value of the variable. This is only set after optimizing
	 * and calling updateValuesFromSolution().
	 * 
	 * @return Value of this variable.
	 */
	public T getValue();

	/**
	 * Sets the value of the variable. Used in updateValuesFromSolution().
	 * 
	 * @param value New value of this variable.
	 */
	public void setValue(final T value);

	/**
	 * Returns the upper bound of this variable.
	 * 
	 * @return Value of the upper bound of this variable.
	 */
	public T getUpperBound();

	/**
	 * Sets the upper bound of this variable.
	 * 
	 * @param bound New value of the upper bound of this variable.
	 */
	public void setUpperBound(final T bound);

	/**
	 * Returns the lower bound of this variable.
	 * 
	 * @return Value of the lower bound of this variable.
	 */
	public T getLowerBound();

	/**
	 * Sets the lower bound of this variable.
	 * 
	 * @param bound New value of the lower bound of this variable.
	 */
	public void setLowerBound(final T bound);

	/**
	 * Returns the type of this variable (BINARY, INTEGER, REAL).
	 * 
	 * @return Type of this variable.
	 */
	public VarType getType();

}
