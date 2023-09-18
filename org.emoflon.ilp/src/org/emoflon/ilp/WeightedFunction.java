package org.emoflon.ilp;

/**
 * This record class represents weighted functions that can be part of other
 * functions as nested functions.
 * 
 * weighted_function = weight * function
 *
 */
public record WeightedFunction(Function function, double weight) {

}
