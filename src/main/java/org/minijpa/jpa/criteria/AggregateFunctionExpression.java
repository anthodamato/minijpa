package org.minijpa.jpa.criteria;

import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

public class AggregateFunctionExpression<N extends Number> implements Expression<N>, AggregateFunctionTypeInfo {

    private final AggregateFunctionType aggregateFunctionType;
    private Expression<N> x;
    private boolean distinct = false;
    private String alias;

    public AggregateFunctionExpression(AggregateFunctionType aggregateFunctionType, Expression<N> x, boolean distinct) {
	super();
	this.aggregateFunctionType = aggregateFunctionType;
	this.x = x;
	this.distinct = distinct;
    }

    @Override
    public AggregateFunctionType getAggregateFunctionType() {
	return aggregateFunctionType;
    }

    public boolean isDistinct() {
	return distinct;
    }

    public Expression<N> getX() {
	return x;
    }

    @Override
    public Selection<N> alias(String name) {
	if (this.alias != null)
	    return this;

	this.alias = name;
	return this;
    }

    @Override
    public boolean isCompoundSelection() {
	return false;
    }

    @Override
    public List<Selection<?>> getCompoundSelectionItems() {
	throw new IllegalStateException(this + " is not a compound selection");
    }

    @Override
    public Class<? extends N> getJavaType() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getAlias() {
	return alias;
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
