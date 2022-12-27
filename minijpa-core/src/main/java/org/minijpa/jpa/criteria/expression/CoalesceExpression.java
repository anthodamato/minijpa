package org.minijpa.jpa.criteria.expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.Coalesce;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

public class CoalesceExpression<Object> implements CriteriaBuilder.Coalesce<Object> {
    private Optional<Expression<Object>> x = Optional.empty();
    private Optional<Expression<?>> y = Optional.empty();
    private Optional<?> yValue = Optional.empty();
    private List<Object> arguments = new ArrayList<>();

    public CoalesceExpression() {
        super();
    }

    public CoalesceExpression(Optional<Expression<Object>> x, Optional<Expression<?>> y, Optional<?> yValue) {
        super();
        this.x = x;
        this.y = y;
        this.yValue = yValue;
    }

    public Optional<Expression<Object>> getX() {
        return x;
    }

    public Optional<Expression<?>> getY() {
        return y;
    }

    public Optional<?> getyValue() {
        return yValue;
    }

    @Override
    public Coalesce<Object> value(Object value) {
        arguments.add(value);
        return this;
    }

    public List<Object> getArguments() {
        return arguments;
    }

    @Override
    public Coalesce<Object> value(Expression<? extends Object> value) {
        arguments.add((Object) value);
        return this;
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
