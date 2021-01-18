package org.minijpa.jdbc.model.condition;

public enum ConditionType {
	EQUAL, NOT_EQUAL, OR, AND, NOT, IS_NULL, IS_NOT_NULL, IS_TRUE, IS_FALSE, EMPTY_CONJUNCTION, EMPTY_DISJUNCTION,
	GREATER_THAN, LESS_THAN, BETWEEN, LIKE;
}
