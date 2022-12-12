package org.minijpa.jpa.criteria;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

public class LocateExpression implements Expression<Integer> {
    private Expression<String> x;
    private Optional<Expression<String>> pattern;
    private Optional<String> patternString;
    private Optional<Expression<Integer>> from;
    private Optional<Integer> fromInteger;

    public LocateExpression(Expression<String> x, Optional<Expression<String>> pattern, Optional<String> patternString,
            Optional<Expression<Integer>> from, Optional<Integer> fromInteger) {
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

    public Optional<Expression<String>> getPattern() {
        return pattern;
    }

    public Optional<String> getPatternString() {
        return patternString;
    }

    public Optional<Expression<Integer>> getFrom() {
        return from;
    }

    public Optional<Integer> getFromInteger() {
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
