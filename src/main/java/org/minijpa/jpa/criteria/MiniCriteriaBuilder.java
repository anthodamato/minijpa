package org.minijpa.jpa.criteria;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
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

public class MiniCriteriaBuilder implements CriteriaBuilder {
	private EntityManager em;

	public MiniCriteriaBuilder(EntityManager em) {
		super();
		this.em = em;
	}

	@Override
	public CriteriaQuery<Object> createQuery() {
		return new MiniCriteriaQuery<Object>(Object.class, em);
	}

	@Override
	public <T> CriteriaQuery<T> createQuery(Class<T> resultClass) {
		return new MiniCriteriaQuery<T>(resultClass, em);
	}

	@Override
	public CriteriaQuery<Tuple> createTupleQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> CriteriaUpdate<T> createCriteriaUpdate(Class<T> targetEntity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> CriteriaDelete<T> createCriteriaDelete(Class<T> targetEntity) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <N extends Number> Expression<N> sum(Expression<N> x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Long> sumAsLong(Expression<Integer> x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Double> sumAsDouble(Expression<Float> x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <N extends Number> Expression<N> max(Expression<N> x) {
		return new MaxExpression<N>(x);
	}

	@Override
	public <N extends Number> Expression<N> min(Expression<N> x) {
		return new MinExpression<N>(x);
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
		return new CountExpression(x);
	}

	@Override
	public Expression<Long> countDistinct(Expression<?> x) {
		return new CountExpression(x, true);
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
		if (restriction instanceof ComparisonPredicate)
			return ((ComparisonPredicate) restriction).not();

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
		return new BooleanExprPredicate(PredicateType.IS_TRUE, x);
	}

	@Override
	public Predicate isFalse(Expression<Boolean> x) {
		return new BooleanExprPredicate(PredicateType.IS_FALSE, x);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Expression<? extends Y> x, Y y) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> x, Y y) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate ge(Expression<? extends Number> x, Number y) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate le(Expression<? extends Number> x, Number y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <N extends Number> Expression<N> neg(Expression<N> x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <N extends Number> Expression<N> abs(Expression<N> x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <N extends Number> Expression<N> sum(Expression<? extends N> x, Expression<? extends N> y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <N extends Number> Expression<N> sum(Expression<? extends N> x, N y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <N extends Number> Expression<N> sum(N x, Expression<? extends N> y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <N extends Number> Expression<N> prod(Expression<? extends N> x, Expression<? extends N> y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <N extends Number> Expression<N> prod(Expression<? extends N> x, N y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <N extends Number> Expression<N> prod(N x, Expression<? extends N> y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <N extends Number> Expression<N> diff(Expression<? extends N> x, Expression<? extends N> y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <N extends Number> Expression<N> diff(Expression<? extends N> x, N y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <N extends Number> Expression<N> diff(N x, Expression<? extends N> y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Number> quot(Expression<? extends Number> x, Expression<? extends Number> y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Number> quot(Expression<? extends Number> x, Number y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Number> quot(Number x, Expression<? extends Number> y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Integer> mod(Expression<Integer> x, Expression<Integer> y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Integer> mod(Expression<Integer> x, Integer y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Integer> mod(Integer x, Expression<Integer> y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Double> sqrt(Expression<? extends Number> x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Long> toLong(Expression<? extends Number> number) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Integer> toInteger(Expression<? extends Number> number) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Float> toFloat(Expression<? extends Number> number) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Double> toDouble(Expression<? extends Number> number) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<BigDecimal> toBigDecimal(Expression<? extends Number> number) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<BigInteger> toBigInteger(Expression<? extends Number> number) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<String> toString(Expression<Character> character) {
		// TODO Auto-generated method stub
		return null;
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
		return new MiniParameterExpression<T>(paramClass);
	}

	@Override
	public <T> ParameterExpression<T> parameter(Class<T> paramClass, String name) {
		return new MiniParameterExpression<T>(paramClass, name);
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
		return new LikePatternExprPredicate(x, pattern);
	}

	@Override
	public Predicate like(Expression<String> x, String pattern) {
		return new LikePatternPredicate(x, pattern);
	}

	@Override
	public Predicate like(Expression<String> x, Expression<String> pattern, Expression<Character> escapeChar) {
		return new LikePatternExprPredicate(x, pattern, escapeChar);
	}

	@Override
	public Predicate like(Expression<String> x, Expression<String> pattern, char escapeChar) {
		return new LikePatternExprPredicate(x, pattern, escapeChar);
	}

	@Override
	public Predicate like(Expression<String> x, String pattern, Expression<Character> escapeChar) {
		return new LikePatternPredicate(x, pattern, escapeChar);
	}

	@Override
	public Predicate like(Expression<String> x, String pattern, char escapeChar) {
		return new LikePatternPredicate(x, pattern, escapeChar);
	}

	@Override
	public Predicate notLike(Expression<String> x, Expression<String> pattern) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate notLike(Expression<String> x, String pattern) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate notLike(Expression<String> x, Expression<String> pattern, Expression<Character> escapeChar) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate notLike(Expression<String> x, Expression<String> pattern, char escapeChar) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate notLike(Expression<String> x, String pattern, Expression<Character> escapeChar) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate notLike(Expression<String> x, String pattern, char escapeChar) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<String> concat(Expression<String> x, Expression<String> y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<String> concat(Expression<String> x, String y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<String> concat(String x, Expression<String> y) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<String> trim(Trimspec ts, Expression<String> x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<String> trim(Expression<Character> t, Expression<String> x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<String> trim(Trimspec ts, Expression<Character> t, Expression<String> x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<String> trim(char t, Expression<String> x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<String> trim(Trimspec ts, char t, Expression<String> x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<String> lower(Expression<String> x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<String> upper(Expression<String> x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Integer> length(Expression<String> x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Integer> locate(Expression<String> x, Expression<String> pattern) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Integer> locate(Expression<String> x, String pattern) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Integer> locate(Expression<String> x, Expression<String> pattern, Expression<Integer> from) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Integer> locate(Expression<String> x, String pattern, int from) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Date> currentDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Timestamp> currentTimestamp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Time> currentTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> In<T> in(Expression<? extends T> expression) {
		// TODO Auto-generated method stub
		return null;
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
