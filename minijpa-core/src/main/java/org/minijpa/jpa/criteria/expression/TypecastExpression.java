package org.minijpa.jpa.criteria.expression;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import java.util.Collection;
import java.util.List;

public class TypecastExpression<T> implements Expression<T> {
    private ExpressionOperator expressionOperator;
    private Expression<?> expression;

    public TypecastExpression(ExpressionOperator expressionOperator, Expression<?> expression) {
        this.expressionOperator = expressionOperator;
        this.expression = expression;
    }

    public Expression<?> getExpression() {
        return expression;
    }

    public ExpressionOperator getExpressionOperator() {
        return expressionOperator;
    }

    @Override
    public Predicate isNull() {
        return null;
    }

    @Override
    public Predicate isNotNull() {
        return null;
    }

    @Override
    public Predicate in(Object... values) {
        return null;
    }

    @Override
    public Predicate in(Expression<?>... values) {
        return null;
    }

    @Override
    public Predicate in(Collection<?> values) {
        return null;
    }

    @Override
    public Predicate in(Expression<Collection<?>> values) {
        return null;
    }

    @Override
    public <X> Expression<X> as(Class<X> type) {
        return null;
    }

    @Override
    public Selection<T> alias(String name) {
        return null;
    }

    @Override
    public boolean isCompoundSelection() {
        return false;
    }

    @Override
    public List<Selection<?>> getCompoundSelectionItems() {
        return null;
    }

    @Override
    public Class<? extends T> getJavaType() {
        return null;
    }

    @Override
    public String getAlias() {
        return null;
    }
}
