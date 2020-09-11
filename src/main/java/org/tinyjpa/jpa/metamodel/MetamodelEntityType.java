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
	private SingularAttribute id;
	private IdentifiableType<? super X> superType;
	private Set<Attribute<? super X, ?>> attributes;
	private Set<Attribute<X, ?>> declaredAttributes;
	private Set<SingularAttribute<? super X, ?>> singularAttributes;
	private Set<SingularAttribute<X, ?>> declaredSingularAttributes;
	private PersistenceType persistenceType;
	private Class<X> javaType;
	private BindableType bindableType;
	private Class<X> bindableJavaType;
	private String name;

	@Override
	public <Y> SingularAttribute<? super X, Y> getId(Class<Y> type) {
		if (type != id.getJavaType())
			throw new IllegalArgumentException("Expected type: " + id.getJavaType().getName());

		return id;
	}

	@Override
	public <Y> SingularAttribute<X, Y> getDeclaredId(Class<Y> type) {
		if (type != id.getJavaType())
			throw new IllegalArgumentException("Expected type: " + id.getJavaType().getName());

		return id;
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
		return superType;
	}

	@Override
	public boolean hasSingleIdAttribute() {
		return true;
	}

	@Override
	public boolean hasVersionAttribute() {
		return false;
	}

	@Override
	public Set<SingularAttribute<? super X, ?>> getIdClassAttributes() {
		return null;
	}

	@Override
	public Type<?> getIdType() {
		return id.getType();
	}

	@Override
	public Set<Attribute<? super X, ?>> getAttributes() {
		return attributes;
	}

	@Override
	public Set<Attribute<X, ?>> getDeclaredAttributes() {
		return declaredAttributes;
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
		return singularAttributes;
	}

	@Override
	public Set<SingularAttribute<X, ?>> getDeclaredSingularAttributes() {
		return declaredSingularAttributes;
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
		return persistenceType;
	}

	@Override
	public Class<X> getJavaType() {
		return javaType;
	}

	@Override
	public BindableType getBindableType() {
		return bindableType;
	}

	@Override
	public Class<X> getBindableJavaType() {
		return bindableJavaType;
	}

	@Override
	public String getName() {
		return name;
	}

	public static class Builder {
		private SingularAttribute<?, ?> id;
		private IdentifiableType superType;
		private Set<Attribute> attributes;
		private Set<Attribute> declaredAttributes;
		private PersistenceType persistenceType;
		private Class javaType;
		private BindableType bindableType;
		private Class bindableJavaType;
		private String name;

		public Builder() {
			super();
		}

		public Builder withId(SingularAttribute<?, ?> id) {
			this.id = id;
			return this;
		}

		public Builder withSuperType(IdentifiableType<?> superType) {
			this.superType = superType;
			return this;
		}

		public Builder withAttributes(Set<Attribute> attributes) {
			this.attributes = attributes;
			return this;
		}

		public Builder withDeclaredAttributes(Set<Attribute> declaredAttributes) {
			this.declaredAttributes = declaredAttributes;
			return this;
		}

		public Builder withPersistenceType(PersistenceType persistenceType) {
			this.persistenceType = persistenceType;
			return this;
		}

		public Builder withJavaType(Class javaType) {
			this.javaType = javaType;
			return this;
		}

		public Builder withBindableType(BindableType bindableType) {
			this.bindableType = bindableType;
			return this;
		}

		public Builder withBindableJavaType(Class bindableJavaType) {
			this.bindableJavaType = bindableJavaType;
			return this;
		}

		public Builder withName(String name) {
			this.name = name;
			return this;
		}

		public EntityType<?> build() {
			MetamodelEntityType entityType = new MetamodelEntityType();
			entityType.id = id;
			entityType.superType = superType;
			entityType.attributes = attributes;
			entityType.declaredAttributes = declaredAttributes;
			entityType.persistenceType = persistenceType;
			entityType.javaType = javaType;
			entityType.bindableType = bindableType;
			entityType.bindableJavaType = bindableJavaType;
			entityType.name = name;

			((IdSingularAttribute) id).setDeclaringType(entityType);

			return entityType;
		}
	}
}
