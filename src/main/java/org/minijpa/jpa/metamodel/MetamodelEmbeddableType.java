package org.minijpa.jpa.metamodel;

import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.SingularAttribute;

public final class MetamodelEmbeddableType<X> extends AbstractManagedType<X> implements EmbeddableType<X> {
	public static class Builder {
		private Set<Attribute> attributes;
		private Set<Attribute> declaredAttributes;
		private PersistenceType persistenceType;
		private Class javaType;
		private Set<SingularAttribute> singularAttributes;

		public Builder() {
			super();
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

		public MetamodelEmbeddableType<?> build() {
			MetamodelEmbeddableType embeddableType = new MetamodelEmbeddableType();
			embeddableType.attributes = attributes;
			embeddableType.declaredAttributes = declaredAttributes;
			embeddableType.singularAttributes = singularAttributes;
			embeddableType.persistenceType = persistenceType;
			embeddableType.javaType = javaType;
			return embeddableType;
		}
	}

}
