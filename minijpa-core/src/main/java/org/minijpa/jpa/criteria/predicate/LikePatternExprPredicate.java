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

public class LikePatternExprPredicate extends AbstractPredicate implements PredicateExpressionInfo, PredicateTypeInfo {

    private Expression<?> x;
    private Expression<String> patternEx;
    private Character escapeChar;
    private Expression<java.lang.Character> escapeCharEx;
    private final List<Expression<Boolean>> expressions = new ArrayList<>();

    public LikePatternExprPredicate(Expression<?> x, Expression<String> patternEx, Character escapeChar,
            Expression<Character> escapeCharEx, boolean not, boolean negated) {
        super(not, negated);
        this.x = x;
        this.patternEx = patternEx;
        this.escapeChar = escapeChar;
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
    public List<Expression<?>> getSimpleExpressions() {
        return Arrays.asList(x);
    }

    @Override
    public List<Expression<Boolean>> getExpressions() {
        return new ArrayList<>(expressions);
    }

    @Override
    public Predicate not() {
        return new LikePatternExprPredicate(x, patternEx, escapeChar, escapeCharEx, !isNot(), true);
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

}
