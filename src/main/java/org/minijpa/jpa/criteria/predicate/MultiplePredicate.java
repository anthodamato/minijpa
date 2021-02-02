package org.minijpa.jpa.criteria.predicate;

import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import org.minijpa.jpa.criteria.AbstractExpression;

public class MultiplePredicate extends AbstractExpression<Boolean> implements Predicate, PredicateTypeInfo {
	private PredicateType predicateType;
	private Predicate[] restrictions;

	public MultiplePredicate(PredicateType predicateType, Predicate[] restrictions) {
		super(Boolean.class);
		this.predicateType = predicateType;
		this.restrictions = restrictions;
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

	public Predicate[] getRestrictions() {
		return restrictions;
	}

}
