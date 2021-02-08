package org.minijpa.jpa.criteria.predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class BetweenValuesPredicate extends AbstractPredicate implements PredicateTypeInfo, PredicateExpressionInfo {

    private final Expression<?> v;
    private final Object x;
    private final Object y;
    private final List<Expression<Boolean>> expressions = new ArrayList<>();

    public BetweenValuesPredicate(Expression<?> v, Object x, Object y) {
	super(false, false);
	this.v = v;
	this.x = x;
	this.y = y;
    }

    public BetweenValuesPredicate(Expression<?> v, Object x, Object y, boolean not, boolean negated) {
	super(not, negated);
	this.v = v;
	this.x = x;
	this.y = y;
    }

    @Override
    public PredicateType getPredicateType() {
	return PredicateType.BETWEEN_VALUES;
    }

    @Override
    public BooleanOperator getOperator() {
	return BooleanOperator.AND;
    }

    @Override
    public List<Expression<?>> getSimpleExpressions() {
	return Arrays.asList(v);
    }

    @Override
    public List<Expression<Boolean>> getExpressions() {
	return new ArrayList<>(expressions);
    }

    @Override
    public Predicate not() {
	return new BetweenValuesPredicate(v, x, y, !isNot(), true);
    }

    public Expression<?> getV() {
	return v;
    }

    public Object getX() {
	return x;
    }

    public Object getY() {
	return y;
    }

}
