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
package org.minijpa.jpa.criteria;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Tuple;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.Metamodel;

import org.minijpa.jpa.criteria.predicate.AbstractPredicate;
import org.minijpa.jpa.criteria.predicate.BetweenExpressionsPredicate;
import org.minijpa.jpa.criteria.predicate.BetweenValuesPredicate;
import org.minijpa.jpa.criteria.predicate.BinaryBooleanExprPredicate;
import org.minijpa.jpa.criteria.predicate.BooleanExprPredicate;
import org.minijpa.jpa.criteria.predicate.ComparisonPredicate;
import org.minijpa.jpa.criteria.predicate.EmptyPredicate;
import org.minijpa.jpa.criteria.predicate.ExprPredicate;
import org.minijpa.jpa.criteria.predicate.InPredicate;
import org.minijpa.jpa.criteria.predicate.LikePredicate;
import org.minijpa.jpa.criteria.predicate.MultiplePredicate;
import org.minijpa.jpa.criteria.predicate.PredicateType;
import org.minijpa.metadata.PersistenceUnitContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniCriteriaBuilder implements CriteriaBuilder {

    private final Logger LOG = LoggerFactory.getLogger(MiniCriteriaBuilder.class);
    private final Metamodel metamodel;
    private final PersistenceUnitContext persistenceUnitContext;

    public MiniCriteriaBuilder(Metamodel metamodel, PersistenceUnitContext persistenceUnitContext) {
        super();
        this.metamodel = metamodel;
        this.persistenceUnitContext = persistenceUnitContext;
    }

    @Override
    public CriteriaQuery<Object> createQuery() {
        return new MiniCriteriaQuery<>(metamodel, persistenceUnitContext);
    }

    @Override
    public <T> CriteriaQuery<T> createQuery(Class<T> resultClass) {
        return new MiniCriteriaQuery<>(resultClass, metamodel, persistenceUnitContext);
    }

    @Override
    public CriteriaQuery<Tuple> createTupleQuery() {
        return new MiniCriteriaQuery<>(Tuple.class, metamodel, persistenceUnitContext);
    }

    @Override
    public <T> CriteriaUpdate<T> createCriteriaUpdate(Class<T> targetEntity) {
        return new MiniCriteriaUpdate<>(targetEntity, metamodel, persistenceUnitContext);
    }

    @Override
    public <T> CriteriaDelete<T> createCriteriaDelete(Class<T> targetEntity) {
        return new MiniCriteriaDelete<>(targetEntity, metamodel, persistenceUnitContext);
    }

    @Override
    public <Y> CompoundSelection<Y> construct(Class<Y> resultClass, Selection<?>... selections) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CompoundSelection<Tuple> tuple(Selection<?>... selections) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CompoundSelection<Object[]> array(Selection<?>... selections) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Order asc(Expression<?> x) {
        return new OrderImpl(x, true);
    }

    @Override
    public Order desc(Expression<?> x) {
        return new OrderImpl(x, false);
    }

    @Override
    public <N extends Number> Expression<Double> avg(Expression<N> x) {
        return (Expression<Double>) new AggregateFunctionExpression<>(AggregateFunctionType.AVG, x, false);
    }

    @Override
    public <N extends Number> Expression<N> sum(Expression<N> x) {
        return new AggregateFunctionExpression<>(AggregateFunctionType.SUM, x, false);
    }

    @Override
    public Expression<Long> sumAsLong(Expression<Integer> x) {
        return new TypecastExpression<>(ExpressionOperator.TO_LONG,
                new AggregateFunctionExpression<>(AggregateFunctionType.SUM, x, false));
    }

    @Override
    public Expression<Double> sumAsDouble(Expression<Float> x) {
        return new TypecastExpression<>(ExpressionOperator.TO_DOUBLE,
                new AggregateFunctionExpression<>(AggregateFunctionType.SUM, x, false));
    }

    @Override
    public <N extends Number> Expression<N> max(Expression<N> x) {
        return new AggregateFunctionExpression<>(AggregateFunctionType.MAX, x, false);
    }

    @Override
    public <N extends Number> Expression<N> min(Expression<N> x) {
        return new AggregateFunctionExpression<>(AggregateFunctionType.MIN, x, false);
    }

    @Override
    public <X extends Comparable<? super X>> Expression<X> greatest(Expression<X> x) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X extends Comparable<? super X>> Expression<X> least(Expression<X> x) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Expression<Long> count(Expression<?> x) {
        return new AggregateFunctionExpression<>(AggregateFunctionType.COUNT, (Expression<Long>) x, false);
    }

    @Override
    public Expression<Long> countDistinct(Expression<?> x) {
        return new AggregateFunctionExpression<>(AggregateFunctionType.COUNT, (Expression<Long>) x, true);
    }

    @Override
    public Predicate exists(Subquery<?> subquery) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> Expression<Y> all(Subquery<Y> subquery) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> Expression<Y> some(Subquery<Y> subquery) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> Expression<Y> any(Subquery<Y> subquery) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Predicate and(Expression<Boolean> x, Expression<Boolean> y) {
        return new BinaryBooleanExprPredicate(PredicateType.AND, x, y);
    }

    @Override
    public Predicate and(Predicate... restrictions) {
        return new MultiplePredicate(PredicateType.AND, restrictions);
    }

    @Override
    public Predicate or(Expression<Boolean> x, Expression<Boolean> y) {
        return new BinaryBooleanExprPredicate(PredicateType.OR, x, y);
    }

    @Override
    public Predicate or(Predicate... restrictions) {
        return new MultiplePredicate(PredicateType.OR, restrictions);
    }

    @Override
    public Predicate not(Expression<Boolean> restriction) {
        if (restriction instanceof AbstractPredicate)
            return ((AbstractPredicate) restriction).not();

        return new BooleanExprPredicate(PredicateType.NOT, restriction);
    }

    @Override
    public Predicate conjunction() {
        // TODO not tested
        return new EmptyPredicate(PredicateType.EMPTY_CONJUNCTION);
    }

    @Override
    public Predicate disjunction() {
        // TODO not tested
        return new EmptyPredicate(PredicateType.EMPTY_DISJUNCTION);
    }

    @Override
    public Predicate isTrue(Expression<Boolean> x) {
        return new BooleanExprPredicate(PredicateType.EQUALS_TRUE, x);
    }

    @Override
    public Predicate isFalse(Expression<Boolean> x) {
        return new BooleanExprPredicate(PredicateType.EQUALS_FALSE, x);
    }

    @Override
    public Predicate isNull(Expression<?> x) {
        return new ExprPredicate(PredicateType.IS_NULL, x);
    }

    @Override
    public Predicate isNotNull(Expression<?> x) {
        return new ExprPredicate(PredicateType.IS_NOT_NULL, x);
    }

    @Override
    public Predicate equal(Expression<?> x, Expression<?> y) {
        return new ComparisonPredicate(PredicateType.EQUAL, x, y, null);
    }

    @Override
    public Predicate equal(Expression<?> x, Object y) {
        return new ComparisonPredicate(PredicateType.EQUAL, x, null, y);
    }

    @Override
    public Predicate notEqual(Expression<?> x, Expression<?> y) {
        return new ComparisonPredicate(PredicateType.NOT_EQUAL, x, y, null);
    }

    @Override
    public Predicate notEqual(Expression<?> x, Object y) {
        return new ComparisonPredicate(PredicateType.NOT_EQUAL, x, null, y);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> x,
            Expression<? extends Y> y) {
        return new ComparisonPredicate(PredicateType.GREATER_THAN, x, y, null);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> x, Y y) {
        return new ComparisonPredicate(PredicateType.GREATER_THAN, x, null, y);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Expression<? extends Y> x,
            Expression<? extends Y> y) {
        return new ComparisonPredicate(PredicateType.GREATER_THAN_OR_EQUAL_TO, x, y, null);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Expression<? extends Y> x, Y y) {
        return new ComparisonPredicate(PredicateType.GREATER_THAN_OR_EQUAL_TO, x, null, y);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> x, Expression<? extends Y> y) {
        return new ComparisonPredicate(PredicateType.LESS_THAN, x, y, null);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> x, Y y) {
        return new ComparisonPredicate(PredicateType.LESS_THAN, x, null, y);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> x,
            Expression<? extends Y> y) {
        return new ComparisonPredicate(PredicateType.LESS_THAN_OR_EQUAL_TO, x, y, null);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> x, Y y) {
        return new ComparisonPredicate(PredicateType.LESS_THAN_OR_EQUAL_TO, x, null, y);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> v, Expression<? extends Y> x,
            Expression<? extends Y> y) {
        return new BetweenExpressionsPredicate(v, x, y);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> v, Y x, Y y) {
        return new BetweenValuesPredicate(v, x, y);
    }

    @Override
    public Predicate gt(Expression<? extends Number> x, Expression<? extends Number> y) {
        return new ComparisonPredicate(PredicateType.GT, x, y, null);
    }

    @Override
    public Predicate gt(Expression<? extends Number> x, Number y) {
        return new ComparisonPredicate(PredicateType.GT, x, null, y);
    }

    @Override
    public Predicate ge(Expression<? extends Number> x, Expression<? extends Number> y) {
        return new ComparisonPredicate(PredicateType.GREATER_THAN_OR_EQUAL_TO, x, y, null);
    }

    @Override
    public Predicate ge(Expression<? extends Number> x, Number y) {
        return new ComparisonPredicate(PredicateType.GREATER_THAN_OR_EQUAL_TO, x, null, y);
    }

    @Override
    public Predicate lt(Expression<? extends Number> x, Expression<? extends Number> y) {
        return new ComparisonPredicate(PredicateType.LT, x, y, null);
    }

    @Override
    public Predicate lt(Expression<? extends Number> x, Number y) {
        return new ComparisonPredicate(PredicateType.LT, x, null, y);
    }

    @Override
    public Predicate le(Expression<? extends Number> x, Expression<? extends Number> y) {
        return new ComparisonPredicate(PredicateType.LESS_THAN_OR_EQUAL_TO, x, y, null);
    }

    @Override
    public Predicate le(Expression<? extends Number> x, Number y) {
        return new ComparisonPredicate(PredicateType.LESS_THAN_OR_EQUAL_TO, x, null, y);
    }

    @Override
    public <N extends Number> Expression<N> neg(Expression<N> x) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <N extends Number> Expression<N> abs(Expression<N> x) {
        return new UnaryExpression<>(ExpressionOperator.ABS, x);
    }

    @Override
    public <N extends Number> Expression<N> sum(Expression<? extends N> x, Expression<? extends N> y) {
        return new BinaryExpression(ExpressionOperator.SUM, x, y);
    }

    @Override
    public <N extends Number> Expression<N> sum(Expression<? extends N> x, N y) {
        return new BinaryExpression(ExpressionOperator.SUM, x, y);
    }

    @Override
    public <N extends Number> Expression<N> sum(N x, Expression<? extends N> y) {
        return new BinaryExpression(ExpressionOperator.SUM, x, y);
    }

    @Override
    public <N extends Number> Expression<N> prod(Expression<? extends N> x, Expression<? extends N> y) {
        return new BinaryExpression(ExpressionOperator.PROD, x, y);
    }

    @Override
    public <N extends Number> Expression<N> prod(Expression<? extends N> x, N y) {
        return new BinaryExpression(ExpressionOperator.PROD, x, y);
    }

    @Override
    public <N extends Number> Expression<N> prod(N x, Expression<? extends N> y) {
        return new BinaryExpression(ExpressionOperator.PROD, x, y);
    }

    @Override
    public <N extends Number> Expression<N> diff(Expression<? extends N> x, Expression<? extends N> y) {
        return new BinaryExpression(ExpressionOperator.DIFF, x, y);
    }

    @Override
    public <N extends Number> Expression<N> diff(Expression<? extends N> x, N y) {
        return new BinaryExpression(ExpressionOperator.DIFF, x, y);
    }

    @Override
    public <N extends Number> Expression<N> diff(N x, Expression<? extends N> y) {
        return new BinaryExpression(ExpressionOperator.DIFF, x, y);
    }

    @Override
    public Expression<Number> quot(Expression<? extends Number> x, Expression<? extends Number> y) {
        return new BinaryExpression(ExpressionOperator.QUOT, x, y);
    }

    @Override
    public Expression<Number> quot(Expression<? extends Number> x, Number y) {
        return new BinaryExpression(ExpressionOperator.QUOT, x, y);
    }

    @Override
    public Expression<Number> quot(Number x, Expression<? extends Number> y) {
        return new BinaryExpression(ExpressionOperator.QUOT, x, y);
    }

    @Override
    public Expression<Integer> mod(Expression<Integer> x, Expression<Integer> y) {
        return new BinaryExpression<Integer>(ExpressionOperator.MOD, x, y);
    }

    @Override
    public Expression<Integer> mod(Expression<Integer> x, Integer y) {
        return new BinaryExpression<Integer>(ExpressionOperator.MOD, x, y);
    }

    @Override
    public Expression<Integer> mod(Integer x, Expression<Integer> y) {
        return new BinaryExpression<Integer>(ExpressionOperator.MOD, x, y);
    }

    @Override
    public Expression<Double> sqrt(Expression<? extends Number> x) {
        return new UnaryExpression<>(ExpressionOperator.SQRT, x);
    }

    @Override
    public Expression<Long> toLong(Expression<? extends Number> number) {
        return new TypecastExpression<>(ExpressionOperator.TO_LONG, number);
    }

    @Override
    public Expression<Integer> toInteger(Expression<? extends Number> number) {
        return new TypecastExpression<>(ExpressionOperator.TO_INTEGER, number);
    }

    @Override
    public Expression<Float> toFloat(Expression<? extends Number> number) {
        return new TypecastExpression<>(ExpressionOperator.TO_FLOAT, number);
    }

    @Override
    public Expression<Double> toDouble(Expression<? extends Number> number) {
        return new TypecastExpression<>(ExpressionOperator.TO_DOUBLE, number);
    }

    @Override
    public Expression<BigDecimal> toBigDecimal(Expression<? extends Number> number) {
        return new TypecastExpression<>(ExpressionOperator.TO_BIGDECIMAL, number);
    }

    @Override
    public Expression<BigInteger> toBigInteger(Expression<? extends Number> number) {
        return new TypecastExpression<>(ExpressionOperator.TO_BIGINTEGER, number);
    }

    @Override
    public Expression<String> toString(Expression<Character> character) {
        return new TypecastExpression<>(ExpressionOperator.TO_STRING, character);
    }

    @Override
    public <T> Expression<T> literal(T value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> Expression<T> nullLiteral(Class<T> resultClass) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> ParameterExpression<T> parameter(Class<T> paramClass) {
        return new MiniParameterExpression<>(paramClass);
    }

    @Override
    public <T> ParameterExpression<T> parameter(Class<T> paramClass, String name) {
        return new MiniParameterExpression<>(paramClass, name);
    }

    @Override
    public <C extends Collection<?>> Predicate isEmpty(Expression<C> collection) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <C extends Collection<?>> Predicate isNotEmpty(Expression<C> collection) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <C extends Collection<?>> Expression<Integer> size(Expression<C> collection) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <C extends Collection<?>> Expression<Integer> size(C collection) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <E, C extends Collection<E>> Predicate isMember(Expression<E> elem, Expression<C> collection) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <E, C extends Collection<E>> Predicate isMember(E elem, Expression<C> collection) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <E, C extends Collection<E>> Predicate isNotMember(Expression<E> elem, Expression<C> collection) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <E, C extends Collection<E>> Predicate isNotMember(E elem, Expression<C> collection) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <V, M extends Map<?, V>> Expression<Collection<V>> values(M map) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <K, M extends Map<K, ?>> Expression<Set<K>> keys(M map) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Predicate like(Expression<String> x, Expression<String> pattern) {
        return new LikePredicate(x, Optional.empty(), Optional.of(pattern), Optional.empty(), Optional.empty(), false,
                false);
    }

    @Override
    public Predicate like(Expression<String> x, String pattern) {
        return new LikePredicate(x, Optional.of(pattern), Optional.empty(), Optional.empty(), Optional.empty(), false,
                false);
    }

    @Override
    public Predicate like(Expression<String> x, Expression<String> pattern, Expression<Character> escapeChar) {
        return new LikePredicate(x, Optional.empty(), Optional.of(pattern), Optional.empty(), Optional.of(escapeChar),
                false, false);
    }

    @Override
    public Predicate like(Expression<String> x, Expression<String> pattern, char escapeChar) {
        return new LikePredicate(x, Optional.empty(), Optional.of(pattern), Optional.of(escapeChar), Optional.empty(),
                false, false);
    }

    @Override
    public Predicate like(Expression<String> x, String pattern, Expression<Character> escapeChar) {
        return new LikePredicate(x, Optional.of(pattern), Optional.empty(), Optional.empty(), Optional.of(escapeChar),
                false, false);
    }

    @Override
    public Predicate like(Expression<String> x, String pattern, char escapeChar) {
        return new LikePredicate(x, Optional.of(pattern), Optional.empty(), Optional.of(escapeChar), Optional.empty(),
                false, false);
    }

    @Override
    public Predicate notLike(Expression<String> x, Expression<String> pattern) {
        return new LikePredicate(x, Optional.empty(), Optional.of(pattern), Optional.empty(), Optional.empty(), true,
                false);
    }

    @Override
    public Predicate notLike(Expression<String> x, String pattern) {
        return new LikePredicate(x, Optional.of(pattern), Optional.empty(), Optional.empty(), Optional.empty(), true,
                false);
    }

    @Override
    public Predicate notLike(Expression<String> x, Expression<String> pattern, Expression<Character> escapeChar) {
        return new LikePredicate(x, Optional.empty(), Optional.of(pattern), Optional.empty(), Optional.of(escapeChar),
                true, false);
    }

    @Override
    public Predicate notLike(Expression<String> x, Expression<String> pattern, char escapeChar) {
        return new LikePredicate(x, Optional.empty(), Optional.of(pattern), Optional.of(escapeChar), Optional.empty(),
                true, false);
    }

    @Override
    public Predicate notLike(Expression<String> x, String pattern, Expression<Character> escapeChar) {
        return new LikePredicate(x, Optional.of(pattern), Optional.empty(), Optional.empty(), Optional.of(escapeChar),
                true, false);
    }

    @Override
    public Predicate notLike(Expression<String> x, String pattern, char escapeChar) {
        return new LikePredicate(x, Optional.of(pattern), Optional.empty(), Optional.of(escapeChar), Optional.empty(),
                true, false);
    }

    @Override
    public Expression<String> concat(Expression<String> x, Expression<String> y) {
        return new ConcatExpression(Optional.of(x), Optional.empty(), Optional.of(y), Optional.empty());
    }

    @Override
    public Expression<String> concat(Expression<String> x, String y) {
        return new ConcatExpression(Optional.of(x), Optional.empty(), Optional.empty(), Optional.of(y));
    }

    @Override
    public Expression<String> concat(String x, Expression<String> y) {
        return new ConcatExpression(Optional.empty(), Optional.of(x), Optional.of(y), Optional.empty());
    }

    @Override
    public Expression<String> substring(Expression<String> x, Expression<Integer> from) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Expression<String> substring(Expression<String> x, int from) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Expression<String> substring(Expression<String> x, Expression<Integer> from, Expression<Integer> len) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Expression<String> substring(Expression<String> x, int from, int len) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Expression<String> trim(Expression<String> x) {
        return new TrimExpression(x, Optional.empty(), Optional.empty(), Optional.empty());
    }

    @Override
    public Expression<String> trim(Trimspec ts, Expression<String> x) {
        return new TrimExpression(x, Optional.empty(), Optional.empty(), Optional.of(ts));
    }

    @Override
    public Expression<String> trim(Expression<Character> t, Expression<String> x) {
        return new TrimExpression(x, Optional.of(t), Optional.empty(), Optional.empty());
    }

    @Override
    public Expression<String> trim(Trimspec ts, Expression<Character> t, Expression<String> x) {
        return new TrimExpression(x, Optional.of(t), Optional.empty(), Optional.of(ts));
    }

    @Override
    public Expression<String> trim(char t, Expression<String> x) {
        return new TrimExpression(x, Optional.empty(), Optional.of(t), Optional.empty());
    }

    @Override
    public Expression<String> trim(Trimspec ts, char t, Expression<String> x) {
        return new TrimExpression(x, Optional.empty(), Optional.of(t), Optional.of(ts));
    }

    @Override
    public Expression<String> lower(Expression<String> x) {
        return new UnaryExpression<>(ExpressionOperator.LOWER, x);
    }

    @Override
    public Expression<String> upper(Expression<String> x) {
        return new UnaryExpression<>(ExpressionOperator.UPPER, x);
    }

    @Override
    public Expression<Integer> length(Expression<String> x) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Expression<Integer> locate(Expression<String> x, Expression<String> pattern) {
        return new LocateExpression(x, Optional.of(pattern), Optional.empty(), Optional.empty(), Optional.empty());
    }

    @Override
    public Expression<Integer> locate(Expression<String> x, String pattern) {
        return new LocateExpression(x, Optional.empty(), Optional.of(pattern), Optional.empty(), Optional.empty());
    }

    @Override
    public Expression<Integer> locate(Expression<String> x, Expression<String> pattern, Expression<Integer> from) {
        return new LocateExpression(x, Optional.of(pattern), Optional.empty(), Optional.of(from), Optional.empty());
    }

    @Override
    public Expression<Integer> locate(Expression<String> x, String pattern, int from) {
        return new LocateExpression(x, Optional.empty(), Optional.of(pattern), Optional.empty(), Optional.of(from));
    }

    @Override
    public Expression<Date> currentDate() {
        return new CurrentDateExpression();
    }

    @Override
    public Expression<Timestamp> currentTimestamp() {
        return new CurrentTimestampExpression();
    }

    @Override
    public Expression<Time> currentTime() {
        return new CurrentTimeExpression();
    }

    @Override
    public <T> In<T> in(Expression<? extends T> expression) {
        return new InPredicate<>(expression, false, false);
    }

    @Override
    public <Y> Expression<Y> coalesce(Expression<? extends Y> x, Expression<? extends Y> y) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> Expression<Y> coalesce(Expression<? extends Y> x, Y y) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> Expression<Y> nullif(Expression<Y> x, Expression<?> y) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> Expression<Y> nullif(Expression<Y> x, Y y) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> Coalesce<T> coalesce() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <C, R> SimpleCase<C, R> selectCase(Expression<? extends C> expression) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <R> Case<R> selectCase() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> Expression<T> function(String name, Class<T> type, Expression<?>... args) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, T, V extends T> Join<X, V> treat(Join<X, T> join, Class<V> type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, T, E extends T> CollectionJoin<X, E> treat(CollectionJoin<X, T> join, Class<E> type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, T, E extends T> SetJoin<X, E> treat(SetJoin<X, T> join, Class<E> type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, T, E extends T> ListJoin<X, E> treat(ListJoin<X, T> join, Class<E> type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, K, T, V extends T> MapJoin<X, K, V> treat(MapJoin<X, K, T> join, Class<V> type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, T extends X> Path<T> treat(Path<X> path, Class<T> type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, T extends X> Root<T> treat(Root<X> root, Class<T> type) {
        // TODO Auto-generated method stub
        return null;
    }

}
