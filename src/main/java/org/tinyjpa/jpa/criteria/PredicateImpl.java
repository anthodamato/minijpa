package org.tinyjpa.jpa.criteria;

import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class PredicateImpl extends AbstractExpression<Boolean> implements Predicate, PredicateTypeInfo {
	private PredicateType predicateType;
	private Expression<?> x;
	private Object y;

	public PredicateImpl(PredicateType predicateType, Expression<?> x, Object y) {
		super(null);
		this.predicateType = predicateType;
		this.x = x;
		this.y = y;
	}

	@Override
	public PredicateType getPredicateType() {
		return predicateType;
	}

	@Override
	public BooleanOperator getOperator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isNegated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Expression<Boolean>> getExpressions() {
		// TODO Auto-generated method stub
		return null;
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
