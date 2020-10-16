package org.tinyjpa.jpa.criteria;

import java.util.Set;

import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

public class RootImpl<X> extends AbstractFrom<X, X> implements Root<X> {
	private EntityType<X> entityType;
	private Class<X> entityClass;

	public RootImpl(Class<X> entityClass, EntityType<X> entityType) {
		super(entityType.getJavaType());
		this.entityClass = entityClass;
		this.entityType = entityType;
	}

	@Override
	public Set<Join<X, ?>> getJoins() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCorrelated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public From<X, X> getCorrelationParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> Join<X, Y> join(SingularAttribute<? super X, Y> attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> Join<X, Y> join(SingularAttribute<? super X, Y> attribute, JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> CollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> SetJoin<X, Y> join(SetAttribute<? super X, Y> set) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> ListJoin<X, Y> join(ListAttribute<? super X, Y> list) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <K, V> MapJoin<X, K, V> join(MapAttribute<? super X, K, V> map) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> CollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> SetJoin<X, Y> join(SetAttribute<? super X, Y> set, JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> ListJoin<X, Y> join(ListAttribute<? super X, Y> list, JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <K, V> MapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> Join<X, Y> join(String attributeName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> CollectionJoin<X, Y> joinCollection(String attributeName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> SetJoin<X, Y> joinSet(String attributeName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> ListJoin<X, Y> joinList(String attributeName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, K, V> MapJoin<X, K, V> joinMap(String attributeName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> Join<X, Y> join(String attributeName, JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> CollectionJoin<X, Y> joinCollection(String attributeName, JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> SetJoin<X, Y> joinSet(String attributeName, JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> ListJoin<X, Y> joinList(String attributeName, JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, K, V> MapJoin<X, K, V> joinMap(String attributeName, JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityType<X> getModel() {
		return entityType;
	}

}
