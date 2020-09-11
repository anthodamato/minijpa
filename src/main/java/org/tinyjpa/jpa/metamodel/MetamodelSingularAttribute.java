package org.tinyjpa.jpa.metamodel;

import java.lang.reflect.Member;

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

public class MetamodelSingularAttribute<X, T> implements SingularAttribute<X, T> {
	private String name;
	private PersistentAttributeType persistentAttributeType = PersistentAttributeType.BASIC;
	private ManagedType<X> declaringType;
	private Class<T> javaType;
	private Member javaMember;
	private BindableType bindableType;
	private Class<T> bindableJavaType;
	private Type<T> type;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public PersistentAttributeType getPersistentAttributeType() {
		return persistentAttributeType;
	}

	@Override
	public ManagedType<X> getDeclaringType() {
		return declaringType;
	}

	void setDeclaringType(ManagedType<X> managedType) {
		this.declaringType = managedType;
	}

	@Override
	public Class<T> getJavaType() {
		return javaType;
	}

	@Override
	public Member getJavaMember() {
		return javaMember;
	}

	@Override
	public boolean isAssociation() {
		return false;
	}

	@Override
	public boolean isCollection() {
		return false;
	}

	@Override
	public BindableType getBindableType() {
		return bindableType;
	}

	@Override
	public Class<T> getBindableJavaType() {
		return bindableJavaType;
	}

	@Override
	public boolean isId() {
		return false;
	}

	@Override
	public boolean isVersion() {
		return false;
	}

	@Override
	public boolean isOptional() {
		return true;
	}

	@Override
	public Type<T> getType() {
		return type;
	}

	public static class Builder {
		private PersistentAttributeType persistentAttributeType;
		private Class javaType;
		private Member javaMember;
		private BindableType bindableType;
		private Class bindableJavaType;
		private String name;
		private Type type;

		public Builder() {
			super();
		}

		public Builder withPersistentAttributeType(PersistentAttributeType persistentAttributeType) {
			this.persistentAttributeType = persistentAttributeType;
			return this;
		}

		public Builder withJavaType(Class javaType) {
			this.javaType = javaType;
			return this;
		}

		public Builder withJavaMember(Member javaMember) {
			this.javaMember = javaMember;
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

		public Builder withType(Type type) {
			this.type = type;
			return this;
		}

		public MetamodelSingularAttribute build() {
			MetamodelSingularAttribute<?, ?> singularAttribute = new MetamodelSingularAttribute();
			singularAttribute.persistentAttributeType = persistentAttributeType;
			singularAttribute.javaType = javaType;
			singularAttribute.javaMember = javaMember;
			singularAttribute.bindableType = bindableType;
			singularAttribute.bindableJavaType = bindableJavaType;
			singularAttribute.name = name;

			return singularAttribute;
		}
	}

}
