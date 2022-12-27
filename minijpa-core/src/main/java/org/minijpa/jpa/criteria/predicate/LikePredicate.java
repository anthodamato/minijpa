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
import java.util.Optional;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class LikePredicate extends AbstractPredicate implements PredicateExpressionInfo, PredicateTypeInfo {

    private Expression<?> x;
    private Optional<String> pattern;
    private Optional<Expression<String>> patternExpression;
    private Optional<Character> escapeChar;
    private Optional<Expression<java.lang.Character>> escapeCharEx;
    private final List<Expression<Boolean>> expressions = new ArrayList<>();

    public LikePredicate(Expression<?> x, Optional<String> pattern, Optional<Expression<String>> patternExpression,
            Optional<Character> escapeChar, Optional<Expression<java.lang.Character>> escapeCharEx, boolean not,
            boolean negated) {
        super(not, negated);
        this.x = x;
        this.pattern = pattern;
        this.patternExpression = patternExpression;
        this.escapeChar = escapeChar;
        this.escapeCharEx = escapeCharEx;
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
    public List<Expression<?>> getSimpleExpressions() {
        return Arrays.asList(x);
    }

    @Override
    public List<Expression<Boolean>> getExpressions() {
        return new ArrayList<>(expressions);
    }

    @Override
    public Predicate not() {
        return new LikePredicate(x, pattern, patternExpression, escapeChar, escapeCharEx, !isNot(), true);
    }

    public Expression<?> getX() {
        return x;
    }

    public Optional<String> getPattern() {
        return pattern;
    }

    public Optional<Expression<String>> getPatternExpression() {
        return patternExpression;
    }

    public Optional<Character> getEscapeChar() {
        return escapeChar;
    }

    public Optional<Expression<java.lang.Character>> getEscapeCharEx() {
        return escapeCharEx;
    }

}
