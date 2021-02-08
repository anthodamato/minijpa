package org.minijpa.jpa.criteria.predicate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class ExprPredicate extends AbstractPredicate implements PredicateExpressionInfo, PredicateTypeInfo {

    private final PredicateType predicateType;
    private final Expression<?> x;

    public ExprPredicate(PredicateType predicateType, Expression<?> x) {
	super(false, false);
	this.predicateType = predicateType;
	this.x = x;
    }

    public ExprPredicate(PredicateType predicateType, Expression<?> x, boolean not, boolean negated) {
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
	return Collections.emptyList();
    }

    @Override
    public Predicate not() {
	return new ExprPredicate(predicateType, x, !isNot(), true);
    }

    public Expression<?> getX() {
	return x;
    }

}
