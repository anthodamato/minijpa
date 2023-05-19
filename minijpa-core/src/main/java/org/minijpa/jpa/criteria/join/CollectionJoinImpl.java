package org.minijpa.jpa.criteria;

import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import org.minijpa.jpa.model.MetaEntity;

public class CollectionJoinImpl<Z, E> extends AbstractFrom<Z, E> implements CollectionJoin<Z, E> {

  private final Attribute<? super Z, E> attribute;
  private final JoinType joinType;

  public CollectionJoinImpl(MetaEntity metaEntity, Attribute<? super Z, E> attribute,
      JoinType joinType) {
    super(metaEntity);
    this.attribute = attribute;
    this.joinType = joinType;
  }

  @Override
  public CollectionJoin<Z, E> on(Expression<Boolean> restriction) {
    return null;
  }

  @Override
  public CollectionJoin<Z, E> on(Predicate... restrictions) {
    return null;
  }

  @Override
  public Predicate getOn() {
    return null;
  }

  @Override
  public Attribute<? super Z, ?> getAttribute() {
    return attribute;
  }

  @Override
  public From<?, Z> getParent() {
    return null;
  }

  @Override
  public JoinType getJoinType() {
    return joinType;
  }

  @Override
  public CollectionAttribute<? super Z, E> getModel() {
    return (CollectionAttribute<? super Z, E>) super.getModel();
  }
}
