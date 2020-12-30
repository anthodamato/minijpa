package org.tinyjpa.jpa.criteria;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Parameter;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;

public class CriteriaUtils {
	public static Parameter<?> findParameterByName(Set<Parameter<?>> parameters, String name) {
		for (Parameter<?> parameter : parameters) {
			if ((parameter instanceof ParameterExpression<?>) && ((ParameterExpression<?>) parameter).getName() != null
					&& ((ParameterExpression<?>) parameter).getName().equals(name)) {
				return parameter;
			}
		}

		return null;
	}

	public static Set<Parameter<?>> findParameters(Predicate predicate) {
		Set<Parameter<?>> parameters = new HashSet<>();
		findParameters(predicate, parameters);
		return parameters;
	}

	private static void findParameters(Predicate predicate, Set<Parameter<?>> parameters) {
		if (predicate instanceof ComparisonPredicate) {
			ComparisonPredicate comparisonPredicate = (ComparisonPredicate) predicate;
			Expression<?> expr = comparisonPredicate.getX();
			if (expr instanceof ParameterExpression<?>)
				parameters.add((ParameterExpression<?>) expr);

			expr = comparisonPredicate.getY();
			if (expr instanceof ParameterExpression<?>)
				parameters.add((ParameterExpression<?>) expr);
		} else if (predicate instanceof BetweenExpressionsPredicate) {
			BetweenExpressionsPredicate betweenExpressionsPredicate = (BetweenExpressionsPredicate) predicate;
			Expression<?> expr = betweenExpressionsPredicate.getX();
			if (expr instanceof ParameterExpression<?>)
				parameters.add((ParameterExpression<?>) expr);

			expr = betweenExpressionsPredicate.getY();
			if (expr instanceof ParameterExpression<?>)
				parameters.add((ParameterExpression<?>) expr);
		} else if (predicate instanceof BetweenValuesPredicate) {
			BetweenValuesPredicate betweenValuesPredicate = (BetweenValuesPredicate) predicate;
			Expression<?> expr = betweenValuesPredicate.getV();
			if (expr instanceof ParameterExpression<?>)
				parameters.add((ParameterExpression<?>) expr);
		} else if (predicate instanceof LikePatternPredicate) {
			LikePatternPredicate likePatternPredicate = (LikePatternPredicate) predicate;
			Expression<?> expr = likePatternPredicate.getX();
			if (expr instanceof ParameterExpression<?>)
				parameters.add((ParameterExpression<?>) expr);
		} else if (predicate instanceof LikePatternExprPredicate) {
			LikePatternExprPredicate likePatternPredicate = (LikePatternExprPredicate) predicate;
			Expression<?> expr = likePatternPredicate.getX();
			if (expr instanceof ParameterExpression<?>)
				parameters.add((ParameterExpression<?>) expr);
		} else if (predicate instanceof MultiplePredicate) {
			MultiplePredicate multiplePredicate = (MultiplePredicate) predicate;
			Predicate[] predicates = multiplePredicate.getRestrictions();
			for (Predicate p : predicates) {
				findParameters(p, parameters);
			}
		} else if (predicate instanceof BinaryBooleanExprPredicate) {
			BinaryBooleanExprPredicate binaryBooleanExprPredicate = (BinaryBooleanExprPredicate) predicate;
			Expression<Boolean> expression = binaryBooleanExprPredicate.getX();
			if (expression instanceof Predicate)
				findParameters((Predicate) expression, parameters);

			expression = binaryBooleanExprPredicate.getY();
			if (expression instanceof Predicate)
				findParameters((Predicate) expression, parameters);
		} else if (predicate instanceof BooleanExprPredicate) {
			BooleanExprPredicate booleanExprPredicate = (BooleanExprPredicate) predicate;
			Expression<Boolean> expression = booleanExprPredicate.getX();
			if (expression instanceof Predicate)
				findParameters((Predicate) expression, parameters);
		} else if (predicate instanceof ExprPredicate) {
			ExprPredicate exprPredicate = (ExprPredicate) predicate;
			Expression<?> expr = exprPredicate.getX();
			if (expr instanceof ParameterExpression<?>)
				parameters.add((ParameterExpression<?>) expr);
		}
	}
}
