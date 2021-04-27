package org.minijpa.jpa.criteria;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

public class BinaryExpression<N extends Number> implements Expression<N>, BinaryExpressionTypeInfo {

    private final ExpressionOperator expressionOperator;
    private Optional<Expression<N>> x = Optional.empty();
    private Optional<Object> xValue = Optional.empty();
    private Optional<Expression<N>> y = Optional.empty();
    private Optional<Object> yValue = Optional.empty();
    private String alias;

    public BinaryExpression(ExpressionOperator expressionOperator, Expression<N> x, Expression<N> y) {
	super();
	this.expressionOperator = expressionOperator;
	this.x = Optional.of(x);
	this.y = Optional.of(y);
    }

    public BinaryExpression(ExpressionOperator expressionOperator, Expression<N> x, Object yValue) {
	super();
	this.expressionOperator = expressionOperator;
	this.x = Optional.of(x);
	this.yValue = Optional.of(yValue);
    }

    public BinaryExpression(ExpressionOperator expressionOperator, Object xValue, Expression<N> y) {
	super();
	this.expressionOperator = expressionOperator;
	this.xValue = Optional.of(xValue);
	this.y = Optional.of(y);
    }

    @Override
    public ExpressionOperator getExpressionOperator() {
	return expressionOperator;
    }

    public Optional<Expression<N>> getX() {
	return x;
    }

    public Optional<Object> getxValue() {
	return xValue;
    }

    public Optional<Expression<N>> getY() {
	return y;
    }

    public Optional<Object> getyValue() {
	return yValue;
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
	if (x.isPresent() && x.get() instanceof AttributePath)
	    return ((AttributePath) x.get()).getJavaType();

	if (y.isPresent() && y.get() instanceof AttributePath)
	    return ((AttributePath) y.get()).getJavaType();

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
