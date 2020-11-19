package org.tinyjpa.jpa.criteria;

import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class BooleanExprPredicate extends AbstractExpression<Boolean> implements Predicate, PredicateTypeInfo {
	private PredicateType predicateType;
	private Expression<Boolean> x;

	public BooleanExprPredicate(PredicateType predicateType, Expression<Boolean> x) {
		super(Boolean.class);
		this.predicateType = predicateType;
		this.x = x;
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
		return Arrays.asList(x);
	}

	@Override
	public Predicate not() {
		// TODO Auto-generated method stub
		return null;
	}

	public Expression<Boolean> getX() {
		return x;
	}

}
