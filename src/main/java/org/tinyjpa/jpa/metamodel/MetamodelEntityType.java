package org.tinyjpa.jpa.metamodel;

import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

public class MetamodelEntityType<X> implements EntityType<X> {

	@Override
	public <Y> SingularAttribute<? super X, Y> getId(Class<Y> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> SingularAttribute<X, Y> getDeclaredId(Class<Y> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> SingularAttribute<? super X, Y> getVersion(Class<Y> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> SingularAttribute<X, Y> getDeclaredVersion(Class<Y> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IdentifiableType<? super X> getSupertype() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasSingleIdAttribute() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasVersionAttribute() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<SingularAttribute<? super X, ?>> getIdClassAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type<?> getIdType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Attribute<? super X, ?>> getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Attribute<X, ?>> getDeclaredAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> SingularAttribute<? super X, Y> getSingularAttribute(String name, Class<Y> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> SingularAttribute<X, Y> getDeclaredSingularAttribute(String name, Class<Y> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<SingularAttribute<? super X, ?>> getSingularAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<SingularAttribute<X, ?>> getDeclaredSingularAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> CollectionAttribute<? super X, E> getCollection(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> CollectionAttribute<X, E> getDeclaredCollection(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> SetAttribute<? super X, E> getSet(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> SetAttribute<X, E> getDeclaredSet(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> ListAttribute<? super X, E> getList(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> ListAttribute<X, E> getDeclaredList(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <K, V> MapAttribute<? super X, K, V> getMap(String name, Class<K> keyType, Class<V> valueType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <K, V> MapAttribute<X, K, V> getDeclaredMap(String name, Class<K> keyType, Class<V> valueType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<PluralAttribute<? super X, ?, ?>> getPluralAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<PluralAttribute<X, ?, ?>> getDeclaredPluralAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Attribute<? super X, ?> getAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Attribute<X, ?> getDeclaredAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SingularAttribute<? super X, ?> getSingularAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SingularAttribute<X, ?> getDeclaredSingularAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CollectionAttribute<? super X, ?> getCollection(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CollectionAttribute<X, ?> getDeclaredCollection(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SetAttribute<? super X, ?> getSet(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SetAttribute<X, ?> getDeclaredSet(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListAttribute<? super X, ?> getList(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListAttribute<X, ?> getDeclaredList(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MapAttribute<? super X, ?, ?> getMap(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MapAttribute<X, ?, ?> getDeclaredMap(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PersistenceType getPersistenceType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<X> getJavaType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BindableType getBindableType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<X> getBindableJavaType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
