package org.minijpa.jpa.criteria;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder.Trimspec;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

public class TrimExpression implements Expression<String> {
    private Expression<String> x;
    private Optional<Expression<Character>> t;
    private Optional<Character> tChar;
    private Optional<Trimspec> ts;

    public TrimExpression(Expression<String> x, Optional<Expression<Character>> t, Optional<Character> tChar,
            Optional<Trimspec> ts) {
        super();
        this.x = x;
        this.t = t;
        this.tChar = tChar;
        this.ts = ts;
    }

    public Expression<String> getX() {
        return x;
    }

    public Optional<Expression<Character>> getT() {
        return t;
    }

    public Optional<Character> gettChar() {
        return tChar;
    }

    public Optional<Trimspec> getTs() {
        return ts;
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
