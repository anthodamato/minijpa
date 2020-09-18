package org.tinyjpa.jpa.metamodel;

import java.util.Set;

import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

public abstract class AbstractIdentifiableType<X> extends AbstractManagedType<X> implements IdentifiableType<X> {
	protected SingularAttribute id;
	protected SingularAttribute declaredId;
	protected SingularAttribute version;
	protected SingularAttribute declaredVersion;
	protected IdentifiableType<? super X> superType;
	protected boolean singleIdAttribute = true;
	protected boolean versionAttribute = false;
	protected Set<SingularAttribute<? super X, ?>> idClassAttributes;

	@Override
	public <Y> SingularAttribute<? super X, Y> getId(Class<Y> type) {
		if (type != id.getJavaType())
			throw new IllegalArgumentException("Expected type id: " + id.getJavaType().getName());

		return id;
	}

	@Override
	public <Y> SingularAttribute<X, Y> getDeclaredId(Class<Y> type) {
		if (type != declaredId.getJavaType())
			throw new IllegalArgumentException("Expected declared type id: " + declaredId.getJavaType().getName());

		return declaredId;
	}

	@Override
	public <Y> SingularAttribute<? super X, Y> getVersion(Class<Y> type) {
		if (type != version.getJavaType())
			throw new IllegalArgumentException("Expected type version: " + version.getJavaType().getName());

		return version;
	}

	@Override
	public <Y> SingularAttribute<X, Y> getDeclaredVersion(Class<Y> type) {
		if (type != declaredVersion.getJavaType())
			throw new IllegalArgumentException(
					"Expected declared type version: " + declaredVersion.getJavaType().getName());

		return declaredVersion;
	}

	@Override
	public IdentifiableType<? super X> getSupertype() {
		return superType;
	}

	@Override
	public boolean hasSingleIdAttribute() {
		return singleIdAttribute;
	}

	@Override
	public boolean hasVersionAttribute() {
		return versionAttribute;
	}

	@Override
	public Set<SingularAttribute<? super X, ?>> getIdClassAttributes() {
		return idClassAttributes;
	}

	@Override
	public Type<?> getIdType() {
		return id.getType();
	}

}
