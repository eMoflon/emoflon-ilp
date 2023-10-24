package org.emoflon.ilp;

/**
 * Abstract class for all constraints.
 */
public abstract class Constraint {
	/**
	 * The name of the constraint.
	 */
	private String name;

	/**
	 * Returns the name of the constraint.
	 * 
	 * @return Name of the constraint.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the constraint to a given value.
	 * 
	 * @param name New name of the constraint to set.
	 */
	public void setName(final String name) {
		this.name = name;
	}

}
