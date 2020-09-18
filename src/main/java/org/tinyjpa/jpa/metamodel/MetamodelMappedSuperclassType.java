package org.tinyjpa.jpa.metamodel;

import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.MappedSuperclassType;
import javax.persistence.metamodel.SingularAttribute;

public final class MetamodelMappedSuperclassType<X> extends AbstractIdentifiableType<X>
		implements MappedSuperclassType<X> {
	public static class Builder {
		private SingularAttribute<?, ?> id;
		private IdentifiableType superType;
		private Set<Attribute> attributes;
		private Set<Attribute> declaredAttributes;
		private PersistenceType persistenceType;
		private Class javaType;
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

		public MappedSuperclassType<?> build() {
			MetamodelMappedSuperclassType entityType = new MetamodelMappedSuperclassType();
			entityType.id = id;
			entityType.superType = superType;
			entityType.attributes = attributes;
			entityType.declaredAttributes = declaredAttributes;
			entityType.singularAttributes = singularAttributes;
			entityType.persistenceType = persistenceType;
			entityType.javaType = javaType;

			((IdSingularAttribute) id).setDeclaringType(entityType);

			return entityType;
		}
	}

}
