package org.tinyjpa.jpa.criteria;

import java.util.Collection;
import java.util.Map;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

public abstract class AbstractPath<X> extends AbstractExpression<X> implements Path<X> {

	public AbstractPath(Class<? extends X> javaType) {
		super(javaType);
	}

	@Override
	public Bindable<X> getModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path<?> getParentPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> Path<Y> get(SingularAttribute<? super X, Y> attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E, C extends Collection<E>> Expression<C> get(PluralAttribute<X, C, E> collection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <K, V, M extends Map<K, V>> Expression<M> get(MapAttribute<X, K, V> map) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Class<? extends X>> type() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> Path<Y> get(String attributeName) {
		return new PathImpl<Y>(attributeName);
	}

}
