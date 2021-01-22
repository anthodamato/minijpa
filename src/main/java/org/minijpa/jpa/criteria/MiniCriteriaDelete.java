package org.minijpa.jpa.criteria;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;

import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jpa.MiniEntityManager;

public class MiniCriteriaDelete<T> implements CriteriaDelete<T> {
	private Class<T> resultClass;
	private EntityManager em;
	private Root<T> root;
	private Predicate restriction;

	public MiniCriteriaDelete(Class<T> resultClass, EntityManager entityManager) {
		super();
		this.resultClass = resultClass;
		this.em = entityManager;
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
		EntityType<T> entityType = em.getMetamodel().entity(entityClass);
		Map<String, MetaEntity> entities = ((MiniEntityManager) em).getEntities();
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
