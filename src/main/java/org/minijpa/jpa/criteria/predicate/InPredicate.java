package org.minijpa.jpa.criteria.predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

public class InPredicate<T> implements In<T>, PredicateTypeInfo {
	private Expression<? extends T> expression;
	private boolean not = false;
	private boolean negated = false;
	private List<T> values = new ArrayList<T>();

	public InPredicate(Expression<? extends T> expression, boolean not, boolean negated) {
		super();
		this.expression = expression;
		this.not = not;
		this.negated = negated;
	}

	@Override
	public PredicateType getPredicateType() {
		return PredicateType.IN;
	}

	@Override
	public BooleanOperator getOperator() {
		return BooleanOperator.AND;
	}

	@Override
	public boolean isNegated() {
		return negated;
	}

	@Override
	public List<Expression<Boolean>> getExpressions() {
		return Collections.emptyList();
	}

	@Override
	public Predicate not() {
		return new InPredicate<T>(expression, !not, true);
	}

	@Override
	public Predicate isNull() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate isNotNull() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate in(Object... values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate in(Expression<?>... values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate in(Collection<?> values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate in(Expression<Collection<?>> values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X> Expression<X> as(Class<X> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Selection<Boolean> alias(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCompoundSelection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Selection<?>> getCompoundSelectionItems() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends Boolean> getJavaType() {
		return Boolean.class;
	}

	@Override
	public String getAlias() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<T> getExpression() {
		return (Expression<T>) expression;
	}

	@Override
	public In<T> value(T value) {
		values.add(value);
		return this;
	}

	@Override
	public In<T> value(Expression<? extends T> value) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isNot() {
		return not;
	}

	public List<T> getValues() {
		return values;
	}

}
