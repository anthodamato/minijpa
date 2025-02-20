package org.minijpa.jpa.criteria.expression;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import java.util.Collection;
import java.util.List;

public class LocateExpression implements Expression<Integer> {
    private Expression<String> x;
    private Expression<String> pattern;
    private String patternString;
    private Expression<Integer> from;
    private Integer fromInteger;

    public LocateExpression(Expression<String> x, Expression<String> pattern, String patternString,
                            Expression<Integer> from, Integer fromInteger) {
        super();
        this.x = x;
        this.pattern = pattern;
        this.patternString = patternString;
        this.from = from;
        this.fromInteger = fromInteger;
    }

    public Expression<String> getX() {
        return x;
    }

    public Expression<String> getPattern() {
        return pattern;
    }

    public String getPatternString() {
        return patternString;
    }

    public Expression<Integer> getFrom() {
        return from;
    }

    public Integer getFromInteger() {
        return fromInteger;
    }

    @Override
    public Selection<Integer> alias(String name) {
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
    public Class<? extends Integer> getJavaType() {
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
