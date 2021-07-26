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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class ComparisonPredicate extends AbstractPredicate implements PredicateExpressionInfo, PredicateTypeInfo {

    private PredicateType predicateType;
    private Expression<?> x;
    private Expression<?> y;
    private Object value;
    private final List<Expression<Boolean>> expressions = new ArrayList<>();

    public ComparisonPredicate(PredicateType predicateType, Expression<?> x, Expression<?> y, Object value) {
	super(false, false);
	this.predicateType = predicateType;
	this.x = x;
	this.y = y;
	this.value = value;
    }

    public ComparisonPredicate(PredicateType predicateType, Expression<?> x, Expression<?> y, Object value,
	    boolean not, boolean negated) {
	super(not, negated);
	this.predicateType = predicateType;
	this.x = x;
	this.y = y;
	this.value = value;
    }

    @Override
    public PredicateType getPredicateType() {
	return predicateType;
    }

    @Override
    public List<Expression<?>> getSimpleExpressions() {
	return Arrays.asList(x, y);
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
	return new ArrayList<>(expressions);
    }

    @Override
    public Predicate not() {
	return new ComparisonPredicate(predicateType, x, y, value, !isNot(), true);
    }

    public Expression<?> getX() {
	return x;
    }

    public Expression<?> getY() {
	return y;
    }

    public Object getValue() {
	return value;
    }

}
