package org.minijpa.jpa.criteria.predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class ComparisonPredicate extends AbstractPredicate implements PredicateExpressionInfo, PredicateTypeInfo {

    private PredicateType predicateType;
    private Expression<?> x;
    private Expression<?> y;
    private Object value;
    private final List<Expression<Boolean>> expressions = new ArrayList<>();

    public ComparisonPredicate(PredicateType predicateType, Expression<?> x, Expression<?> y, Object value) {
	super(false, false);
	this.predicateType = predicateType;
	this.x = x;
	this.y = y;
	this.value = value;
    }

    public ComparisonPredicate(PredicateType predicateType, Expression<?> x, Expression<?> y, Object value,
	    boolean not, boolean negated) {
	super(not, negated);
	this.predicateType = predicateType;
	this.x = x;
	this.y = y;
	this.value = value;
    }

    @Override
    public PredicateType getPredicateType() {
	return predicateType;
    }

    @Override
    public List<Expression<?>> getSimpleExpressions() {
	return Arrays.asList(x, y);
    }

    @Override
    public BooleanOperator getOperator() {
	if (predicateType == PredicateType.OR)
	    return BooleanOperator.OR;

	if (predicateType == PredicateType.AND)
	    return BooleanOperator.AND;

	return BooleanOperator.AND;
    }

    @Override
    public List<Expression<Boolean>> getExpressions() {
	return new ArrayList<>(expressions);
    }

    @Override
    public Predicate not() {
	return new ComparisonPredicate(predicateType, x, y, value, !isNot(), true);
    }

    public Expression<?> getX() {
	return x;
    }

    public Expression<?> getY() {
	return y;
    }

    public Object getValue() {
	return value;
    }

}
