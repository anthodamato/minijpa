package org.minijpa.jpa.criteria.predicate;

import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class BinaryBooleanExprPredicate extends AbstractPredicate implements PredicateExpressionInfo, PredicateTypeInfo {

    private PredicateType predicateType;
    private Expression<Boolean> x;
    private Expression<Boolean> y;

    public BinaryBooleanExprPredicate(PredicateType predicateType, Expression<Boolean> x, Expression<Boolean> y) {
	super(false, false);
	this.predicateType = predicateType;
	this.x = x;
	this.y = y;
    }

    public BinaryBooleanExprPredicate(PredicateType predicateType, Expression<Boolean> x, Expression<Boolean> y, boolean not, boolean negated) {
	super(not, negated);
	this.predicateType = predicateType;
	this.x = x;
	this.y = y;
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
	return Arrays.asList(x, y);
    }

    @Override
    public Predicate not() {
	return new BinaryBooleanExprPredicate(predicateType, x, y, !isNot(), true);
    }

    public Expression<Boolean> getX() {
	return x;
    }

    public Expression<Boolean> getY() {
	return y;
    }

}
