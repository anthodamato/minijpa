package org.minijpa.jpa.criteria.join;

import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.Attribute;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;

public class CollectionFetchJoinImpl<Z, E> extends CollectionJoinImpl<Z, E> implements Fetch<Z, E> {

  public CollectionFetchJoinImpl(MetaEntity metaEntity, RelationshipMetaAttribute metaAttribute,
      Attribute<? super Z, E> attribute,
      JoinType joinType, FetchJoinType fetchJoinType) {
    super(metaEntity, metaAttribute, attribute, joinType, fetchJoinType);
  }
}
