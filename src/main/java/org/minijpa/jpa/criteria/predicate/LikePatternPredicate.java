package org.minijpa.jpa.criteria.predicate;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import org.minijpa.jpa.criteria.AbstractExpression;

public class LikePatternPredicate extends AbstractExpression<Boolean> implements Predicate, PredicateTypeInfo {
	private Expression<?> x;
	private String pattern;
	private Character escapeChar;
	private Expression<java.lang.Character> escapeCharEx;
	private boolean not = false;
	private boolean negated = false;
	private List<Expression<Boolean>> expressions = new ArrayList<Expression<Boolean>>();

	public LikePatternPredicate(Expression<?> x, String pattern, Character escapeChar,
			Expression<Character> escapeCharEx, boolean not, boolean negated) {
		super(Boolean.class);
		this.x = x;
		this.pattern = pattern;
		this.escapeChar = escapeChar;
		this.escapeCharEx = escapeCharEx;
		this.not = not;
		this.negated = negated;
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
		return negated;
	}

	@Override
	public List<Expression<Boolean>> getExpressions() {
		return new ArrayList<Expression<Boolean>>(expressions);
	}

	@Override
	public Predicate not() {
		return new LikePatternPredicate(x, pattern, escapeChar, escapeCharEx, !not, true);
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

	public boolean isNot() {
		return not;
	}

}
