package org.minijpa.jpa.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.minijpa.jdbc.MetaEntity;

public class MiniCriteriaQuery<T> implements CriteriaQuery<T> {
	private Class<T> resultClass;
	private Metamodel metamodel;
	private Map<String, MetaEntity> entities;
//	private EntityManager em;
	private Set<Root<?>> roots = new HashSet<>();
	protected Selection<? extends T> selection;
	private Predicate restriction;
	private List<Order> orders = new ArrayList<>();
	private boolean distinct;

	public MiniCriteriaQuery(Class<T> resultClass, Metamodel metamodel, Map<String, MetaEntity> entities) {
		super();
		this.resultClass = resultClass;
		this.metamodel = metamodel;
		this.entities = entities;
	}

	@SuppressWarnings("unchecked")
	public MiniCriteriaQuery(Metamodel metamodel, Map<String, MetaEntity> entities) {
		super();
		this.resultClass = (Class<T>) Object[].class;
		this.metamodel = metamodel;
		this.entities = entities;
	}

	@Override
	public <X> Root<X> from(Class<X> entityClass) {
		EntityType<X> entityType = metamodel.entity(entityClass);
//		Map<String, MetaEntity> entities = ((MiniEntityManager) em).getEntities();
		MetaEntity metaEntity = entities.get(entityClass.getName());
		Root<X> root = new MiniRoot<X>(entityType, metaEntity);
		roots.add(root);
		return root;
	}

	@Override
	public <X> Root<X> from(EntityType<X> entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Root<?>> getRoots() {
		return new HashSet<>(roots);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Selection<T> getSelection() {
		return (Selection<T>) selection;
	}

	@Override
	public List<Expression<?>> getGroupList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate getGroupRestriction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDistinct() {
		return distinct;
	}

	@Override
	public Class<T> getResultType() {
		return resultClass;
	}

	@Override
	public <U> Subquery<U> subquery(Class<U> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate getRestriction() {
		return restriction;
	}

	@Override
	public CriteriaQuery<T> select(Selection<? extends T> selection) {
		this.selection = selection;
		return this;
	}

	@Override
	public CriteriaQuery<T> multiselect(Selection<?>... selections) {
		this.selection = new CompoundSelectionImpl<T>(Arrays.asList(selections), getResultType());
		return this;
	}

	@Override
	public CriteriaQuery<T> multiselect(List<Selection<?>> selectionList) {
		this.selection = new CompoundSelectionImpl<T>(Collections.unmodifiableList(selectionList), getResultType());
		return this;
	}

	@Override
	public CriteriaQuery<T> where(Expression<Boolean> restriction) {
		this.restriction = (Predicate) restriction;
		return this;
	}

	@Override
	public CriteriaQuery<T> where(Predicate... restrictions) {
		if (restrictions.length == 0)
			this.restriction = null;

		this.restriction = new MultiplePredicate(PredicateType.AND, restrictions);
		return this;
	}

	@Override
	public CriteriaQuery<T> groupBy(Expression<?>... grouping) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> groupBy(List<Expression<?>> grouping) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> having(Expression<Boolean> restriction) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> having(Predicate... restrictions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> orderBy(Order... o) {
		orders.clear();
		if (o == null)
			return this;

		orders.addAll(Arrays.asList(o));
		return this;
	}

	@Override
	public CriteriaQuery<T> orderBy(List<Order> o) {
		orders.clear();
		if (o == null)
			return this;

		orders.addAll(o);
		return this;
	}

	@Override
	public CriteriaQuery<T> distinct(boolean distinct) {
		this.distinct = distinct;
		return this;
	}

	@Override
	public List<Order> getOrderList() {
		return Collections.unmodifiableList(orders);
	}

	@Override
	public Set<ParameterExpression<?>> getParameters() {
		if (restriction == null)
			return new HashSet<ParameterExpression<?>>();

		Set<ParameterExpression<?>> parameterExpressions = PredicateUtils.findParameters(restriction);
		return parameterExpressions;
	}

}
