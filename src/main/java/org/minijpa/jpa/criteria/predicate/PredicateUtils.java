/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.jpa.criteria.predicate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import org.minijpa.jpa.criteria.MiniParameterExpression;

public class PredicateUtils {

    private static void checkExpressionAsParameter(Expression<?> x, Set<ParameterExpression<?>> parameterExpressions) {
	if (x != null && x instanceof MiniParameterExpression<?>)
	    parameterExpressions.add((ParameterExpression<?>) x);
    }

    private static void findExpressions(Predicate predicate, List<Expression<?>> expressions) {
	List<Expression<Boolean>> exprs = predicate.getExpressions();
	if (exprs == null || exprs.isEmpty())
	    expressions.addAll(((PredicateExpressionInfo) predicate).getSimpleExpressions());
	else
	    exprs.stream().filter(e -> (e instanceof Predicate)).forEachOrdered(e -> {
		findExpressions((Predicate) e, expressions);
	    });
    }

    public static void findExpressions(Predicate[] predicates, List<Expression<?>> expressions) {
	for (Predicate p : predicates) {
	    findExpressions(p, expressions);
	}
    }

    public static Set<ParameterExpression<?>> findParameters(Predicate predicate) {
	Set<ParameterExpression<?>> parameterExpressions = new HashSet<>();
	PredicateExpressionInfo predicateExpressionInfo = (PredicateExpressionInfo) predicate;
	predicateExpressionInfo.getSimpleExpressions().stream().forEach(e -> {
	    checkExpressionAsParameter(e, parameterExpressions);
	});

	return parameterExpressions;
    }

    public static void findTopLevelExpressions(MultiplePredicate multiplePredicate, List<Expression<Boolean>> expressions) {
	Predicate[] predicates = multiplePredicate.getRestrictions();
	for (Predicate p : predicates) {
	    if (p instanceof MultiplePredicate)
		expressions.add(p);
	}
    }
}
