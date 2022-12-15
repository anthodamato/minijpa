package org.minijpa.jpa.criteria.expression;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

public class SubstringExpression implements Expression<String> {
    private Expression<String> x;
    private Optional<Expression<Integer>> from;
    private Optional<Integer> fromInteger;
    private Optional<Expression<Integer>> len;
    private Optional<Integer> lenInteger;

    public SubstringExpression(Expression<String> x, Optional<Expression<Integer>> from, Optional<Integer> fromInteger,
            Optional<Expression<Integer>> len, Optional<Integer> lenInteger) {
        super();
        this.x = x;
        this.from = from;
        this.fromInteger = fromInteger;
        this.len = len;
        this.lenInteger = lenInteger;
    }

    public Expression<String> getX() {
        return x;
    }

    public Optional<Expression<Integer>> getFrom() {
        return from;
    }

    public Optional<Integer> getFromInteger() {
        return fromInteger;
    }

    public Optional<Expression<Integer>> getLen() {
        return len;
    }

    public Optional<Integer> getLenInteger() {
        return lenInteger;
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
