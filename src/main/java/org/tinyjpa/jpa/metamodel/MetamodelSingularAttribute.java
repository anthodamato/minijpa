package org.tinyjpa.jpa.metamodel;

import java.lang.reflect.Member;

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

public class MetamodelSingularAttribute<X, T> implements SingularAttribute<X, T> {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PersistentAttributeType getPersistentAttributeType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ManagedType<X> getDeclaringType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<T> getJavaType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Member getJavaMember() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAssociation() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCollection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public BindableType getBindableType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<T> getBindableJavaType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isId() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVersion() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOptional() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Type<T> getType() {
		// TODO Auto-generated method stub
		return null;
	}

}
