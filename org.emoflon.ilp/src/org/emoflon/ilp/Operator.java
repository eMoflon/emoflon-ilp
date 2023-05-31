package org.emoflon.ilp;

public enum Operator {
	LESS, LESS_OR_EQUAL, EQUAL, GREATER_OR_EQUAL, GREATER, NOT_EQUAL;

	@Override
	public String toString() {
		switch (this) {
		case LESS:
			return "<";
		case LESS_OR_EQUAL:
			return "<=";
		case EQUAL:
			return "==";
		case GREATER_OR_EQUAL:
			return ">=";
		case GREATER:
			return ">";
		default: // NOT_EQUAL
			return "!=";
		}
	}
}