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
import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class InPredicate<T> extends AbstractPredicate implements In<T>, PredicateTypeInfo, PredicateExpressionInfo {

    private Expression<? extends T> expression;
    private List<T> values = new ArrayList<T>();

    public InPredicate(Expression<? extends T> expression, boolean not, boolean negated) {
	super(not, negated);
	this.expression = expression;
    }

    @Override
    public List<Expression<?>> getSimpleExpressions() {
	return Arrays.asList(expression);
    }

    @Override
    public PredicateType getPredicateType() {
	return PredicateType.IN;
    }

    @Override
    public BooleanOperator getOperator() {
	return BooleanOperator.AND;
    }

    @Override
    public List<Expression<Boolean>> getExpressions() {
	return Collections.emptyList();
    }

    @Override
    public Predicate not() {
	return new InPredicate<T>(expression, !isNot(), true);
    }

    @Override
    public Expression<T> getExpression() {
	return (Expression<T>) expression;
    }

    @Override
    public In<T> value(T value) {
	values.add(value);
	return this;
    }

    @Override
    public In<T> value(Expression<? extends T> value) {
//	values.add(value);
	return this;
    }

    public List<T> getValues() {
	return values;
    }

}
