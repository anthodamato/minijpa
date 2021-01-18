package org.minijpa.jpa.criteria;

import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class EmptyPredicate extends AbstractExpression<Boolean> implements Predicate, PredicateTypeInfo {
	private PredicateType predicateType;

	public EmptyPredicate(PredicateType predicateType) {
		super(Boolean.class);
		this.predicateType = predicateType;
	}

	@Override
	public PredicateType getPredicateType() {
		return predicateType;
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
	public boolean isNegated() {
		return predicateType == PredicateType.NOT;
	}

	@Override
	public List<Expression<Boolean>> getExpressions() {
		return Collections.emptyList();
	}

	@Override
	public Predicate not() {
		// TODO Auto-generated method stub
		return null;
	}

}
