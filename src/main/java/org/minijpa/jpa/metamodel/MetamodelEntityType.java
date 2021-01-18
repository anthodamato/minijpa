package org.minijpa.jpa.metamodel;

import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.SingularAttribute;

public final class MetamodelEntityType<X> extends AbstractIdentifiableType<X> implements EntityType<X> {
	private BindableType bindableType;
	private Class<X> bindableJavaType;
	private String name;

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
		private Set<SingularAttribute> singularAttributes;

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

		public Builder withSingularAttributes(Set<SingularAttribute> singularAttributes) {
			this.singularAttributes = singularAttributes;
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
			entityType.singularAttributes = singularAttributes;
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
