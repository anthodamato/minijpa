package org.minijpa.jpa.criteria.predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class LikePatternExprPredicate extends AbstractPredicate implements PredicateExpressionInfo, PredicateTypeInfo {

    private Expression<?> x;
    private Expression<String> patternEx;
    private Character escapeChar;
    private Expression<java.lang.Character> escapeCharEx;
    private final List<Expression<Boolean>> expressions = new ArrayList<>();

    public LikePatternExprPredicate(Expression<?> x, Expression<String> patternEx, Character escapeChar,
	    Expression<Character> escapeCharEx, boolean not, boolean negated) {
	super(not, negated);
	this.x = x;
	this.patternEx = patternEx;
	this.escapeChar = escapeChar;
	this.escapeCharEx = escapeCharEx;
    }

    @Override
    public PredicateType getPredicateType() {
	return PredicateType.LIKE_PATTERN_EXPR;
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
	return new LikePatternExprPredicate(x, patternEx, escapeChar, escapeCharEx, !isNot(), true);
    }

    public Expression<?> getX() {
	return x;
    }

    public Character getEscapeChar() {
	return escapeChar;
    }

    public Expression<Character> getEscapeCharEx() {
	return escapeCharEx;
    }

    public Expression<String> getPatternEx() {
	return patternEx;
    }

}
