package org.minijpa.jpa.criteria;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class LikePatternPredicate extends AbstractExpression<Boolean> implements Predicate, PredicateTypeInfo {
	private Expression<?> x;
	private String pattern;
	private Character escapeChar;
	private Expression<java.lang.Character> escapeCharEx;
	private List<Expression<Boolean>> expressions = new ArrayList<Expression<Boolean>>();

	public LikePatternPredicate(Expression<?> x, String pattern) {
		super(Boolean.class);
		this.x = x;
		this.pattern = pattern;
	}

	public LikePatternPredicate(Expression<?> x, String pattern, Character escapeChar) {
		super(Boolean.class);
		this.x = x;
		this.pattern = pattern;
		this.escapeChar = escapeChar;
	}

	public LikePatternPredicate(Expression<?> x, String pattern, Expression<Character> escapeCharEx) {
		super(Boolean.class);
		this.x = x;
		this.pattern = pattern;
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
	public boolean isNegated() {
		return false;
	}

	@Override
	public List<Expression<Boolean>> getExpressions() {
		return new ArrayList<Expression<Boolean>>(expressions);
	}

	@Override
	public Predicate not() {
		// TODO Auto-generated method stub
		return null;
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
