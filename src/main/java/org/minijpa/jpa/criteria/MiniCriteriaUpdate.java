package org.minijpa.jpa.criteria;

import org.minijpa.jpa.criteria.predicate.PredicateType;
import org.minijpa.jpa.criteria.predicate.MultiplePredicate;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.minijpa.jdbc.MetaEntity;
import org.minijpa.metadata.PersistenceUnitContext;

public class MiniCriteriaUpdate<T> implements CriteriaUpdate<T> {

    private final Class<T> resultClass;
    private final Metamodel metamodel;
    private final PersistenceUnitContext persistenceUnitContext;
    private Root<T> root;
    private Predicate restriction;
    private final Map<Path<?>, Object> setValues = new HashMap<>();

    public MiniCriteriaUpdate(Class<T> resultClass, Metamodel metamodel, PersistenceUnitContext persistenceUnitContext) {
	super();
	this.resultClass = resultClass;
	this.metamodel = metamodel;
	this.persistenceUnitContext = persistenceUnitContext;
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
	MetaEntity metaEntity = persistenceUnitContext.getEntities().get(entityClass.getName());
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
    public <Y, X extends Y> CriteriaUpdate<T> set(SingularAttribute<? super T, Y> attribute, X value) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <Y> CriteriaUpdate<T> set(SingularAttribute<? super T, Y> attribute, Expression<? extends Y> value) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <Y, X extends Y> CriteriaUpdate<T> set(Path<Y> attribute, X value) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <Y> CriteriaUpdate<T> set(Path<Y> attribute, Expression<? extends Y> value) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public CriteriaUpdate<T> set(String attributeName, Object value) {
	if (root == null)
	    return this;

	Path<?> path = root.get(attributeName);
	setValues.put(path, value);
	return this;
    }

    @Override
    public CriteriaUpdate<T> where(Expression<Boolean> restriction) {
	this.restriction = (Predicate) restriction;
	return this;
    }

    @Override
    public CriteriaUpdate<T> where(Predicate... restrictions) {
	if (restrictions.length == 0)
	    this.restriction = null;

	this.restriction = new MultiplePredicate(PredicateType.AND, restrictions);
	return this;
    }

    public Map<Path<?>, Object> getSetValues() {
	return setValues;
    }

}
