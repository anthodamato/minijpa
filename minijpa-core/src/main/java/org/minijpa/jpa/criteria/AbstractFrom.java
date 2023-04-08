package org.minijpa.jpa.criteria;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.SetJoin;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import org.minijpa.jpa.metamodel.AttributeImpl;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.relationship.Relationship;

public abstract class AbstractFrom<Z, X> implements From<Z, X> {

  private MetaEntity metaEntity;
  private final Set<Join<X, ?>> joins = new HashSet<>();

  public AbstractFrom(MetaEntity metaEntity) {
    this.metaEntity = metaEntity;
  }

  public MetaEntity getMetaEntity() {
    return metaEntity;
  }

  @Override
  public Set<Join<X, ?>> getJoins() {
    return Set.copyOf(joins);
  }

  @Override
  public boolean isCorrelated() {
    return false;
  }

  @Override
  public From<Z, X> getCorrelationParent() {
    return null;
  }

  @Override
  public <Y> Join<X, Y> join(SingularAttribute<? super X, Y> attribute) {
    return null;
  }

  @Override
  public <Y> Join<X, Y> join(SingularAttribute<? super X, Y> attribute, JoinType jt) {
    return null;
  }

  @Override
  public <Y> CollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection) {
    return (CollectionJoin<X, Y>) new CollectionJoinImpl<>(metaEntity, collection, JoinType.INNER);
  }

  @Override
  public <Y> SetJoin<X, Y> join(SetAttribute<? super X, Y> set) {
    return null;
  }

  @Override
  public <Y> ListJoin<X, Y> join(ListAttribute<? super X, Y> list) {
    return null;
  }

  @Override
  public <K, V> MapJoin<X, K, V> join(MapAttribute<? super X, K, V> map) {
    return null;
  }

  @Override
  public <Y> CollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, JoinType jt) {
    return null;
  }

  @Override
  public <Y> SetJoin<X, Y> join(SetAttribute<? super X, Y> set, JoinType jt) {
    return null;
  }

  @Override
  public <Y> ListJoin<X, Y> join(ListAttribute<? super X, Y> list, JoinType jt) {
    return null;
  }

  @Override
  public <K, V> MapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, JoinType jt) {
    return null;
  }

  @Override
  public <X1, Y> Join<X1, Y> join(String attributeName) {
    return join(attributeName, JoinType.INNER);
  }

  @Override
  public <X1, Y> CollectionJoin<X1, Y> joinCollection(String attributeName) {
    return null;
  }

  @Override
  public <X1, Y> SetJoin<X1, Y> joinSet(String attributeName) {
    return null;
  }

  @Override
  public <X1, Y> ListJoin<X1, Y> joinList(String attributeName) {
    return null;
  }

  @Override
  public <X1, K, V> MapJoin<X1, K, V> joinMap(String attributeName) {
    return null;
  }

  @Override
  public <X1, Y> Join<X1, Y> join(String attributeName, JoinType jt) {
    MetaAttribute metaAttribute = metaEntity.getAttribute(attributeName);
    if (metaAttribute == null) {
      throw new IllegalArgumentException("Attribute '" + attributeName + "' not found");
    }

    if (metaAttribute.getRelationship() != null) {
      if (metaAttribute.isCollection() && metaAttribute.getType() == Collection.class) {
        Attribute attribute = new AttributeImpl<>(attributeName,
            PersistentAttributeType.BASIC, null, metaAttribute.getType(), null, false, true);
        Join join = new CollectionJoinImpl<>(metaAttribute.getRelationship().getAttributeType(),
            attribute, jt);
        joins.add(join);
        return join;
      }
    }

//    Optional<MetaEntity> optional = metaEntity.getEmbeddable(attributeName);
//    if (optional.isPresent()) {
//    }

    throw new IllegalArgumentException(
        "Join not supported: attribute '" + attributeName + "' Join: " + jt);
  }

  @Override
  public <X1, Y> CollectionJoin<X1, Y> joinCollection(String attributeName, JoinType jt) {
    return null;
  }

  @Override
  public <X1, Y> SetJoin<X1, Y> joinSet(String attributeName, JoinType jt) {
    return null;
  }

  @Override
  public <X1, Y> ListJoin<X1, Y> joinList(String attributeName, JoinType jt) {
    return null;
  }

  @Override
  public <X1, K, V> MapJoin<X1, K, V> joinMap(String attributeName, JoinType jt) {
    return null;
  }

  @Override
  public Set<Fetch<X, ?>> getFetches() {
    return null;
  }

  @Override
  public <Y> Fetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute) {
    return null;
  }

  @Override
  public <Y> Fetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute, JoinType jt) {
    return null;
  }

  @Override
  public <Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute) {
    return null;
  }

  @Override
  public <Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, JoinType jt) {
    return null;
  }

  @Override
  public <X1, Y> Fetch<X1, Y> fetch(String attributeName) {
    return null;
  }

  @Override
  public <X1, Y> Fetch<X1, Y> fetch(String attributeName, JoinType jt) {
    return null;
  }

  @Override
  public Bindable<X> getModel() {
    return null;
  }

  @Override
  public Path<?> getParentPath() {
    return null;
  }

  @Override
  public <Y> Path<Y> get(SingularAttribute<? super X, Y> attribute) {
    MetaAttribute metaAttribute = metaEntity.getAttribute(attribute.getName());
    if (metaAttribute == null) {
      throw new IllegalArgumentException("Attribute '" + attribute.getName() + "' does not exist");
    }

    return new AttributePath<>(metaAttribute, metaEntity);
  }

  @Override
  public <E, C extends Collection<E>> Expression<C> get(PluralAttribute<X, C, E> collection) {
    return null;
  }

  @Override
  public <K, V, M extends Map<K, V>> Expression<M> get(MapAttribute<X, K, V> map) {
    return null;
  }

  @Override
  public Expression<Class<? extends X>> type() {
    return null;
  }

  @Override
  public <Y> Path<Y> get(String attributeName) {
    MetaAttribute metaAttribute = metaEntity.getAttribute(attributeName);
    if (metaAttribute != null) {
      return new AttributePath<>(metaAttribute, metaEntity);
    }

    Optional<MetaEntity> optional = metaEntity.getEmbeddable(attributeName);
    if (optional.isPresent()) {
      return new EmbeddablePath<>(optional.get(), metaEntity);
    }

    throw new IllegalArgumentException("Attribute '" + attributeName + "' not found");
  }

  @Override
  public Predicate isNull() {
    return null;
  }

  @Override
  public Predicate isNotNull() {
    return null;
  }

  @Override
  public Predicate in(Object... values) {
    return null;
  }

  @Override
  public Predicate in(Expression<?>... values) {
    return null;
  }

  @Override
  public Predicate in(Collection<?> values) {
    return null;
  }

  @Override
  public Predicate in(Expression<Collection<?>> values) {
    return null;
  }

  @Override
  public <X1> Expression<X1> as(Class<X1> type) {
    return null;
  }

  @Override
  public Selection<X> alias(String name) {
    return null;
  }

  @Override
  public boolean isCompoundSelection() {
    return false;
  }

  @Override
  public List<Selection<?>> getCompoundSelectionItems() {
    return null;
  }

  @Override
  public Class<? extends X> getJavaType() {
    return null;
  }

  @Override
  public String getAlias() {
    return null;
  }
}
