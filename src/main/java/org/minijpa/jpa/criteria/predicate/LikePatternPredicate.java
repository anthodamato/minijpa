package org.minijpa.jpa.criteria.predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class LikePatternPredicate extends AbstractPredicate implements PredicateExpressionInfo, PredicateTypeInfo {

    private Expression<?> x;
    private String pattern;
    private Character escapeChar;
    private Expression<java.lang.Character> escapeCharEx;
    private final List<Expression<Boolean>> expressions = new ArrayList<>();

    public LikePatternPredicate(Expression<?> x, String pattern, Character escapeChar,
	    Expression<Character> escapeCharEx, boolean not, boolean negated) {
	super(not, negated);
	this.x = x;
	this.pattern = pattern;
	this.escapeChar = escapeChar;
	this.escapeCharEx = escapeCharEx;
    }

    @Override
    public PredicateType getPredicateType() {
	return PredicateType.LIKE_PATTERN;
    }

    @Override
    public BooleanOperator getOperator() {
	return BooleanOperator.AND;
    }

    @Override
    public List<Expression<?>> getSimpleExpressions() {
	return Arrays.asList(x);
    }

    @Override
    public List<Expression<Boolean>> getExpressions() {
	return new ArrayList<>(expressions);
    }

    @Override
    public Predicate not() {
	return new LikePatternPredicate(x, pattern, escapeChar, escapeCharEx, !isNot(), true);
    }

    public Expression<?> getX() {
	return x;
    }

    public String getPattern() {
	return pattern;
    }

    public Character getEscapeChar() {
	return escapeChar;
    }

    public Expression<java.lang.Character> getEscapeCharEx() {
	return escapeCharEx;
    }

}
