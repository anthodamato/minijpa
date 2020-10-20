package org.tinyjpa.jpa.criteria;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class PredicateImpl extends AbstractExpression<Boolean> implements Predicate, PredicateTypeInfo {
	private PredicateType predicateType;
	private Expression<?> x;
	private Expression<?> y;
	private Object value;
	private List<Expression<Boolean>> expressions = new ArrayList<Expression<Boolean>>();

	public PredicateImpl(PredicateType predicateType, Expression<?> x, Expression<?> y, Object value) {
		super(Boolean.class);
		this.predicateType = predicateType;
		this.x = x;
		this.y = y;
		this.value = value;
	}

	public PredicateImpl(PredicateType predicateType, Expression<Boolean> x, Expression<Boolean> y) {
		super(Boolean.class);
		this.predicateType = predicateType;
		this.expressions.add(x);
		this.expressions.add(y);
	}

	public PredicateImpl(PredicateType predicateType, Expression<Boolean>[] restrictions) {
		super(Boolean.class);
		this.predicateType = predicateType;
		for (Expression<Boolean> expression : restrictions)
			this.expressions.add(expression);
	}

	public PredicateImpl(PredicateType predicateType, Expression<Boolean> x) {
		super(Boolean.class);
		this.predicateType = predicateType;
		this.expressions.add(x);
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

	public Expression<?> getY() {
		return y;
	}

	public Object getValue() {
		return value;
	}

}
