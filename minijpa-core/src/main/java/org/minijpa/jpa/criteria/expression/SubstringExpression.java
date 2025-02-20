package org.minijpa.jpa.criteria.expression;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import java.util.Collection;
import java.util.List;

public class SubstringExpression implements Expression<String> {
    private Expression<String> x;
    private Expression<Integer> from;
    private Integer fromInteger;
    private Expression<Integer> len;
    private Integer lenInteger;

    public SubstringExpression(
            Expression<String> x, Expression<Integer> from, Integer fromInteger,
            Expression<Integer> len, Integer lenInteger) {
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

    public Expression<Integer> getFrom() {
        return from;
    }

    public Integer getFromInteger() {
        return fromInteger;
    }

    public Expression<Integer> getLen() {
        return len;
    }

    public Integer getLenInteger() {
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
