package org.tinyjpa.jpa.criteria;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class PredicateImpl extends AbstractExpression<Boolean> implements Predicate, PredicateTypeInfo {
	private PredicateType predicateType;
	private Expression<?> x;
	private Object y;
	private List<Expression<Boolean>> expressions = new ArrayList<Expression<Boolean>>();

	public PredicateImpl(PredicateType predicateType, Expression<?> x, Object y) {
		super(Boolean.class);
		this.predicateType = predicateType;
		this.x = x;
		this.y = y;
	}

	public PredicateImpl(PredicateType predicateType, Expression<Boolean> x, Expression<Boolean> y) {
		super(Boolean.class);
		this.predicateType = predicateType;
		this.expressions.add(x);
		this.expressions.add(y);
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Expression<Boolean>> getExpressions() {
		return new ArrayList<Expression<Boolean>>(expressions);
	}

	@Override
	public Predicate not() {
		// TODO Auto-generated method stub
		return null;
	}

	public Expression<?> getX() {
		return x;
	}

	public Object getY() {
		return y;
	}

}
