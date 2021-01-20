package org.minijpa.jpa.criteria;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;

public class PredicateUtils {

	private static void checkExpressionAsParameter(Expression<?> x, Set<ParameterExpression<?>> parameterExpressions) {
		if (x != null && x instanceof MiniParameterExpression<?>) {
			parameterExpressions.add((ParameterExpression<?>) x);
		}
	}

	private static void findParameters(Predicate predicate, Set<ParameterExpression<?>> parameterExpressions) {
		if (predicate instanceof MultiplePredicate) {
			MultiplePredicate multiplePredicate = (MultiplePredicate) predicate;
			Predicate[] predicates = multiplePredicate.getRestrictions();
			for (Predicate p : predicates) {
				findParameters(p, parameterExpressions);
			}
		} else if (predicate instanceof ComparisonPredicate) {
			ComparisonPredicate comparisonPredicate = (ComparisonPredicate) predicate;
			checkExpressionAsParameter(comparisonPredicate.getX(), parameterExpressions);
			checkExpressionAsParameter(comparisonPredicate.getY(), parameterExpressions);
		} else if (predicate instanceof BetweenExpressionsPredicate) {
			BetweenExpressionsPredicate betweenExpressionsPredicate = (BetweenExpressionsPredicate) predicate;
			checkExpressionAsParameter(betweenExpressionsPredicate.getX(), parameterExpressions);
			checkExpressionAsParameter(betweenExpressionsPredicate.getY(), parameterExpressions);
			checkExpressionAsParameter(betweenExpressionsPredicate.getV(), parameterExpressions);
		} else if (predicate instanceof BetweenValuesPredicate) {
			BetweenValuesPredicate betweenValuesPredicate = (BetweenValuesPredicate) predicate;
			checkExpressionAsParameter(betweenValuesPredicate.getV(), parameterExpressions);
		} else if (predicate instanceof BinaryBooleanExprPredicate) {
			BinaryBooleanExprPredicate binaryBooleanExprPredicate = (BinaryBooleanExprPredicate) predicate;
			checkExpressionAsParameter(binaryBooleanExprPredicate.getX(), parameterExpressions);
			checkExpressionAsParameter(binaryBooleanExprPredicate.getY(), parameterExpressions);
		} else if (predicate instanceof BooleanExprPredicate) {
			BooleanExprPredicate booleanExprPredicate = (BooleanExprPredicate) predicate;
			checkExpressionAsParameter(booleanExprPredicate.getX(), parameterExpressions);
		} else if (predicate instanceof ExprPredicate) {
			ExprPredicate exprPredicate = (ExprPredicate) predicate;
			checkExpressionAsParameter(exprPredicate.getX(), parameterExpressions);
		} else if (predicate instanceof LikePatternExprPredicate) {
			LikePatternExprPredicate likePatternExprPredicate = (LikePatternExprPredicate) predicate;
			checkExpressionAsParameter(likePatternExprPredicate.getX(), parameterExpressions);
		} else if (predicate instanceof LikePatternPredicate) {
			LikePatternPredicate likePatternPredicate = (LikePatternPredicate) predicate;
			checkExpressionAsParameter(likePatternPredicate.getX(), parameterExpressions);
		}
	}

	public static Set<ParameterExpression<?>> findParameters(Predicate predicate) {
		Set<ParameterExpression<?>> parameterExpressions = new HashSet<>();
		findParameters(predicate, parameterExpressions);
		return parameterExpressions;
	}

}
