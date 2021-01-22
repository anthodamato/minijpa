package org.minijpa.jpa.criteria;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;

public class MiniPath<X> implements Path<X> {
	private MetaAttribute metaAttribute;
	private MetaEntity metaEntity;
	private String alias;

	public MiniPath(MetaAttribute metaAttribute, MetaEntity metaEntity) {
		super();
		this.metaAttribute = metaAttribute;
		this.metaEntity = metaEntity;
	}

	public MetaAttribute getMetaAttribute() {
		return metaAttribute;
	}

	public MetaEntity getMetaEntity() {
		return metaEntity;
	}

	@Override
	public Predicate isNull() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate isNotNull() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate in(Object... values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate in(Expression<?>... values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate in(Collection<?> values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate in(Expression<Collection<?>> values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X> Expression<X> as(Class<X> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Selection<X> alias(String name) {
		if (this.alias != null)
			return this;

		this.alias = name;
		return this;
	}

	@Override
	public boolean isCompoundSelection() {
		return false;
	}

	@Override
	public List<Selection<?>> getCompoundSelectionItems() {
		throw new IllegalStateException("Not a compound selection");
	}

	@Override
	public Class<? extends X> getJavaType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAlias() {
		return alias;
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
		MetaAttribute metaAttribute = metaEntity.getAttribute(attributeName);
		if (metaAttribute == null)
			throw new IllegalArgumentException("Attribute '" + attributeName + "' does not exist");

		return new MiniPath<Y>(metaAttribute, metaEntity);
	}

	@Override
	public int hashCode() {
		return Objects.hash(metaEntity.getName(), metaAttribute.getName());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof MiniPath<?>))
			return false;

		MiniPath<?> miniPath = (MiniPath<?>) obj;
		if (miniPath.metaEntity != metaEntity || miniPath.metaAttribute != metaAttribute)
			return false;

		return true;
	}

}
