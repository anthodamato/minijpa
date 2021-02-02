package org.minijpa.jpa.criteria;

import org.minijpa.jpa.criteria.predicate.PredicateType;
import org.minijpa.jpa.criteria.predicate.MultiplePredicate;
import java.util.Map;

import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.minijpa.jdbc.MetaEntity;

public class MiniCriteriaDelete<T> implements CriteriaDelete<T> {
	private Class<T> resultClass;
	private Metamodel metamodel;
	private Map<String, MetaEntity> entities;
	private Root<T> root;
	private Predicate restriction;

	public MiniCriteriaDelete(Class<T> resultClass, Metamodel metamodel, Map<String, MetaEntity> entities) {
		super();
		this.resultClass = resultClass;
		this.metamodel = metamodel;
		this.entities = entities;
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
	public Root<T> from(Class<T> entityClass) {
		EntityType<T> entityType = metamodel.entity(entityClass);
//		Map<String, MetaEntity> entities = ((MiniEntityManager) em).getEntities();
		MetaEntity metaEntity = entities.get(entityClass.getName());
		root = new MiniRoot<T>(entityType, metaEntity);
		return root;
	}

	@Override
	public Root<T> from(EntityType<T> entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Root<T> getRoot() {
		return root;
	}

	@Override
	public CriteriaDelete<T> where(Expression<Boolean> restriction) {
		this.restriction = (Predicate) restriction;
		return this;
	}

	@Override
	public CriteriaDelete<T> where(Predicate... restrictions) {
		if (restrictions.length == 0)
			this.restriction = null;

		this.restriction = new MultiplePredicate(PredicateType.AND, restrictions);
		return this;
	}

}
