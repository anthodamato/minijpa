package org.minijpa.jpa.criteria.predicate;

import org.minijpa.jpa.criteria.predicate.PredicateTypeInfo;
import org.minijpa.jpa.criteria.predicate.PredicateType;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import org.minijpa.jpa.criteria.AbstractExpression;

public class LikePatternExprPredicate extends AbstractExpression<Boolean> implements Predicate, PredicateTypeInfo {
	private Expression<?> x;
	private Expression<String> patternEx;
	private Character escapeChar;
	private Expression<java.lang.Character> escapeCharEx;
	private boolean not = false;
	private boolean negated = false;
	private List<Expression<Boolean>> expressions = new ArrayList<Expression<Boolean>>();

	public LikePatternExprPredicate(Expression<?> x, Expression<String> patternEx, Character escapeChar,
			Expression<Character> escapeCharEx, boolean not, boolean negated) {
		super(Boolean.class);
		this.x = x;
		this.patternEx = patternEx;
		this.escapeChar = escapeChar;
		this.escapeCharEx = escapeCharEx;
		this.not = not;
		this.negated = negated;
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
		return negated;
	}

	@Override
	public List<Expression<Boolean>> getExpressions() {
		return new ArrayList<Expression<Boolean>>(expressions);
	}

	@Override
	public Predicate not() {
		return new LikePatternExprPredicate(x, patternEx, escapeChar, escapeCharEx, !not, true);
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

	public boolean isNot() {
		return not;
	}

}
