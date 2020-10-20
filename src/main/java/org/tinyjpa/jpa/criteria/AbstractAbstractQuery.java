package org.tinyjpa.jpa.criteria;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;

public abstract class AbstractAbstractQuery<T> implements AbstractQuery<T> {
	private Class<T> resultClass;
	private EntityManager em;
	private Set<Root<?>> roots = new HashSet<>();
	protected Selection<? extends T> selection;
	protected List<Expression<Boolean>> restrictions = new ArrayList<>();

	public AbstractAbstractQuery(Class<T> resultClass, EntityManager em) {
		super();
		this.resultClass = resultClass;
		this.em = em;
	}

	@Override
	public <U> Subquery<U> subquery(Class<U> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate getRestriction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X> Root<X> from(Class<X> entityClass) {
		EntityType<X> entityType = em.getMetamodel().entity(entityClass);
		Root<X> root = new RootImpl<X>(entityClass, entityType);
		roots.add(root);
		return root;
	}

	@Override
	public <X> Root<X> from(EntityType<X> entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractQuery<T> where(Expression<Boolean> restriction) {
		restrictions.clear();
		restrictions.add(restriction);
		return this;
	}

	@Override
	public AbstractQuery<T> where(Predicate... restrictions) {
		this.restrictions.clear();
		for (Predicate predicate : restrictions) {
			this.restrictions.add(predicate);
		}

		return this;
	}

	public List<Expression<Boolean>> getRestrictions() {
		return restrictions;
	}

	@Override
	public AbstractQuery<T> groupBy(Expression<?>... grouping) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractQuery<T> groupBy(List<Expression<?>> grouping) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractQuery<T> having(Expression<Boolean> restriction) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractQuery<T> having(Predicate... restrictions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractQuery<T> distinct(boolean distinct) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Root<?>> getRoots() {
		return new HashSet<>(roots);
	}

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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Class<T> getResultType() {
		return resultClass;
	}

}
