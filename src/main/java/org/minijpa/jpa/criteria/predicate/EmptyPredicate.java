package org.minijpa.jpa.criteria.predicate;

import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class EmptyPredicate extends AbstractPredicate implements PredicateExpressionInfo, PredicateTypeInfo {

    private PredicateType predicateType;

    public EmptyPredicate(PredicateType predicateType) {
	super(false, false);
	this.predicateType = predicateType;
    }

    public EmptyPredicate(PredicateType predicateType, boolean not, boolean negated) {
	super(not, negated);
	this.predicateType = predicateType;
    }

    @Override
    public PredicateType getPredicateType() {
	return predicateType;
    }

    @Override
    public List<Expression<?>> getSimpleExpressions() {
	return Collections.emptyList();
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
	return new EmptyPredicate(predicateType, !isNot(), true);
    }

}
