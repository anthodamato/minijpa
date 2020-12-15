package org.tinyjpa.jpa.criteria;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class ComparisonPredicate extends AbstractExpression<Boolean> implements Predicate, PredicateTypeInfo {
	private PredicateType predicateType;
	private Expression<?> x;
	private Expression<?> y;
	private Object value1;
	private boolean negated = false;
	private List<Expression<Boolean>> expressions = new ArrayList<Expression<Boolean>>();

	public ComparisonPredicate(PredicateType predicateType, Expression<?> x, Expression<?> y, Object value1) {
		super(Boolean.class);
		this.predicateType = predicateType;
		this.x = x;
		this.y = y;
		this.value1 = value1;
	}

	public ComparisonPredicate(PredicateType predicateType, Expression<?> x, Expression<?> y, Object value1,
			boolean negated) {
		super(Boolean.class);
		this.predicateType = predicateType;
		this.x = x;
		this.y = y;
		this.value1 = value1;
		this.negated = negated;
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
		return negated;
	}

	@Override
	public List<Expression<Boolean>> getExpressions() {
		return new ArrayList<Expression<Boolean>>(expressions);
	}

	@Override
	public Predicate not() {
		return new ComparisonPredicate(predicateType, x, y, value1, !negated);
	}

	public Expression<?> getX() {
		return x;
	}

	public Expression<?> getY() {
		return y;
	}

	public Object getValue1() {
		return value1;
	}

}
