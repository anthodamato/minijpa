package org.tinyjpa.jpa.criteria;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class LikePatternExprPredicate extends AbstractExpression<Boolean> implements Predicate, PredicateTypeInfo {
	private Expression<?> x;
	private Expression<String> patternEx;
	private Character escapeChar;
	private Expression<java.lang.Character> escapeCharEx;
	private List<Expression<Boolean>> expressions = new ArrayList<Expression<Boolean>>();

	public LikePatternExprPredicate(Expression<?> x, Expression<String> patternEx) {
		super(Boolean.class);
		this.x = x;
		this.patternEx = patternEx;
	}

	public LikePatternExprPredicate(Expression<?> x, Expression<String> patternEx, Character escapeChar) {
		super(Boolean.class);
		this.x = x;
		this.patternEx = patternEx;
		this.escapeChar = escapeChar;
	}

	public LikePatternExprPredicate(Expression<?> x, Expression<String> patternEx,
			Expression<java.lang.Character> escapeCharEx) {
		super(Boolean.class);
		this.x = x;
		this.patternEx = patternEx;
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

	public Character getEscapeChar() {
		return escapeChar;
	}

	public Expression<java.lang.Character> getEscapeCharEx() {
		return escapeCharEx;
	}

	public Expression<String> getPatternEx() {
		return patternEx;
	}

}
