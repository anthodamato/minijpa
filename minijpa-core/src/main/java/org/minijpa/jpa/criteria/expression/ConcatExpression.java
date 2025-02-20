package org.minijpa.jpa.criteria.expression;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import java.util.Collection;
import java.util.List;

public class ConcatExpression implements Expression<String> {
    private Expression<String> x;
    private String xValue;
    private Expression<String> y;
    private String yValue;

    public ConcatExpression(
            Expression<String> x,
            String xValue,
            Expression<String> y,
            String yValue) {
        super();
        this.x = x;
        this.xValue = xValue;
        this.y = y;
        this.yValue = yValue;
    }

    public Expression<String> getX() {
        return x;
    }

    public String getxValue() {
        return xValue;
    }

    public Expression<String> getY() {
        return y;
    }

    public String getyValue() {
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
