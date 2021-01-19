package org.minijpa.jpa.criteria;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

public class CountExpression implements Expression<Long> {
	private Expression<?> expression;
	private boolean distinct = false;

	public CountExpression(Expression<?> expression) {
		super();
		this.expression = expression;
	}

	public CountExpression(Expression<?> expression, boolean distinct) {
		super();
		this.expression = expression;
		this.distinct = distinct;
	}

	public Expression<?> getExpression() {
		return expression;
	}

	public boolean isDistinct() {
		return distinct;
	}

	@Override
	public Selection<Long> alias(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCompoundSelection() {
		return false;
	}

	@Override
	public List<Selection<?>> getCompoundSelectionItems() {
		return Collections.emptyList();
	}

	@Override
	public Class<? extends Long> getJavaType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAlias() {
		// TODO Auto-generated method stub
		return null;
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

}
