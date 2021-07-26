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

public class BetweenValuesPredicate extends AbstractPredicate implements PredicateTypeInfo, PredicateExpressionInfo {

    private final Expression<?> v;
    private final Object x;
    private final Object y;
    private final List<Expression<Boolean>> expressions = new ArrayList<>();

    public BetweenValuesPredicate(Expression<?> v, Object x, Object y) {
	super(false, false);
	this.v = v;
	this.x = x;
	this.y = y;
    }

    public BetweenValuesPredicate(Expression<?> v, Object x, Object y, boolean not, boolean negated) {
	super(not, negated);
	this.v = v;
	this.x = x;
	this.y = y;
    }

    @Override
    public PredicateType getPredicateType() {
	return PredicateType.BETWEEN_VALUES;
    }

    @Override
    public BooleanOperator getOperator() {
	return BooleanOperator.AND;
    }

    @Override
    public List<Expression<?>> getSimpleExpressions() {
	return Arrays.asList(v);
    }

    @Override
    public List<Expression<Boolean>> getExpressions() {
	return new ArrayList<>(expressions);
    }

    @Override
    public Predicate not() {
	return new BetweenValuesPredicate(v, x, y, !isNot(), true);
    }

    public Expression<?> getV() {
	return v;
    }

    public Object getX() {
	return x;
    }

    public Object getY() {
	return y;
    }

}
