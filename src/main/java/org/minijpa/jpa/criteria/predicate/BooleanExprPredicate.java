package org.minijpa.jpa.criteria.predicate;

import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class BooleanExprPredicate extends AbstractPredicate implements PredicateExpressionInfo, PredicateTypeInfo {

    private final PredicateType predicateType;
    private Expression<Boolean> x;

    public BooleanExprPredicate(PredicateType predicateType, Expression<Boolean> x) {
	super(false, false);
	this.predicateType = predicateType;
	this.x = x;
    }

    public BooleanExprPredicate(PredicateType predicateType, Expression<Boolean> x, boolean not, boolean negated) {
	super(not, negated);
	this.predicateType = predicateType;
	this.x = x;
    }

    @Override
    public PredicateType getPredicateType() {
	return predicateType;
    }

    @Override
    public List<Expression<?>> getSimpleExpressions() {
	return Arrays.asList(x);
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
	return Arrays.asList(x);
    }

    @Override
    public Predicate not() {
	return new BooleanExprPredicate(predicateType, x, !isNot(), true);
    }

    public Expression<Boolean> getX() {
	return x;
    }

}
