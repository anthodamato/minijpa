package org.minijpa.jpa.criteria;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

public class ConcatExpression implements Expression<String> {
    private Optional<Expression<String>> x;
    private Optional<String> xValue;
    private Optional<Expression<String>> y;
    private Optional<String> yValue;

    public ConcatExpression(Optional<Expression<String>> x, Optional<String> xValue, Optional<Expression<String>> y,
            Optional<String> yValue) {
        super();
        this.x = x;
        this.xValue = xValue;
        this.y = y;
        this.yValue = yValue;
    }

    public Optional<Expression<String>> getX() {
        return x;
    }

    public Optional<String> getxValue() {
        return xValue;
    }

    public Optional<Expression<String>> getY() {
        return y;
    }

    public Optional<String> getyValue() {
        return yValue;
    }

    @Override
    public Selection<String> alias(String name) {
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
    public Class<? extends String> getJavaType() {
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
