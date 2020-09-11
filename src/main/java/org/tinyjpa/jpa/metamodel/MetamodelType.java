package org.tinyjpa.jpa.metamodel;

import javax.persistence.metamodel.Type;

public class MetamodelType<X> implements Type<X> {
	private PersistenceType persistenceType;
	private Class<X> javaType;

	public MetamodelType(PersistenceType persistenceType, Class<X> javaType) {
		super();
		this.persistenceType = persistenceType;
		this.javaType = javaType;
	}

	@Override
	public PersistenceType getPersistenceType() {
		return persistenceType;
	}

	@Override
	public Class<X> getJavaType() {
		return javaType;
	}

}
