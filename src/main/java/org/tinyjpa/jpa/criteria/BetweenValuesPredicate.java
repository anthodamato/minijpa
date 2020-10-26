package org.tinyjpa.jpa.criteria;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class BetweenValuesPredicate extends AbstractExpression<Boolean> implements Predicate, PredicateTypeInfo {
	private Expression<?> v;
	private Object x;
	private Object y;
	private List<Expression<Boolean>> expressions = new ArrayList<Expression<Boolean>>();

	public BetweenValuesPredicate(Expression<?> v, Object x, Object y) {
		super(Boolean.class);
		this.v = v;
		this.x = x;
		this.y = y;
	}

	@Override
	public PredicateType getPredicateType() {
		return PredicateType.BETWEEN_VALUES;
	}

	@Override
	public BooleanOperator getOperator() {
		return BooleanOperator.AND;
	}

	@Override
	public boolean isNegated() {
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

	public Expression<?> getV() {
		return v;
	}

	public Object getX() {
		return x;
	}

	public Object getY() {
		return y;
	}

}
