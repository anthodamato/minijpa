package org.tinyjpa.jpa.criteria;

import java.util.Set;

import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.FetchParent;
import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

public abstract class AbstractFrom<Z, X> extends AbstractPath<X> implements FetchParent<Z, X> {

	public AbstractFrom(Class<? extends X> javaType) {
		super(javaType);
	}

	@Override
	public Set<Fetch<X, ?>> getFetches() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> Fetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> Fetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute, JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> Fetch<X, Y> fetch(String attributeName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> Fetch<X, Y> fetch(String attributeName, JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

}
