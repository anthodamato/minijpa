package org.minijpa.jpa.criteria.expression;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

public class NullifExpression<Object> implements Expression<Object> {
    private Expression<Object> x;
    private Optional<Expression<?>> y = Optional.empty();
    private Optional<?> yValue = Optional.empty();

    public NullifExpression(Expression<Object> x, Optional<Expression<?>> y, Optional<?> yValue) {
        super();
        this.x = x;
        this.y = y;
        this.yValue = yValue;
    }

    public Expression<Object> getX() {
        return x;
    }

    public Optional<Expression<?>> getY() {
        return y;
    }

    public Optional<?> getyValue() {
        return yValue;
    }

    @Override
    public Selection<Object> alias(String name) {
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
    public Class<? extends Object> getJavaType() {
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
    public Predicate in(java.lang.Object... values) {
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
