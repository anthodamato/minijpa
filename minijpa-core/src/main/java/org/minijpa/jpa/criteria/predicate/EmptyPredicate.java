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

import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class EmptyPredicate extends AbstractPredicate implements PredicateExpressionInfo, PredicateTypeInfo {

    private PredicateType predicateType;

    public EmptyPredicate(PredicateType predicateType) {
	super(false, false);
	this.predicateType = predicateType;
    }

    public EmptyPredicate(PredicateType predicateType, boolean not, boolean negated) {
	super(not, negated);
	this.predicateType = predicateType;
    }

    @Override
    public PredicateType getPredicateType() {
	return predicateType;
    }

    @Override
    public List<Expression<?>> getSimpleExpressions() {
	return Collections.emptyList();
    }

    @Override
    public BooleanOperator getOperator() {
	if (predicateType == PredicateType.OR)
	    return BooleanOperator.OR;

	if (predicateType == PredicateType.AND)
	    return BooleanOperator.AND;

	return BooleanOperator.AND;
    }

    @Override
    public List<Expression<Boolean>> getExpressions() {
	return Collections.emptyList();
    }

    @Override
    public Predicate not() {
	return new EmptyPredicate(predicateType, !isNot(), true);
    }

}
