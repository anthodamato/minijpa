package org.minijpa.jpa.criteria.predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class InPredicate<T> extends AbstractPredicate implements In<T>, PredicateTypeInfo, PredicateExpressionInfo {

    private Expression<? extends T> expression;
    private List<T> values = new ArrayList<T>();

    public InPredicate(Expression<? extends T> expression, boolean not, boolean negated) {
	super(not, negated);
	this.expression = expression;
    }

    @Override
    public List<Expression<?>> getSimpleExpressions() {
	return Arrays.asList(expression);
    }

    @Override
    public PredicateType getPredicateType() {
	return PredicateType.IN;
    }

    @Override
    public BooleanOperator getOperator() {
	return BooleanOperator.AND;
    }

    @Override
    public List<Expression<Boolean>> getExpressions() {
	return Collections.emptyList();
    }

    @Override
    public Predicate not() {
	return new InPredicate<T>(expression, !isNot(), true);
    }

    @Override
    public Expression<T> getExpression() {
	return (Expression<T>) expression;
    }

    @Override
    public In<T> value(T value) {
	values.add(value);
	return this;
    }

    @Override
    public In<T> value(Expression<? extends T> value) {
//	values.add(value);
	return this;
    }

    public List<T> getValues() {
	return values;
    }

}
