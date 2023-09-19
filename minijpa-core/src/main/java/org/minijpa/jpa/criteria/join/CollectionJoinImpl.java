package org.minijpa.jpa.criteria.join;

import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;

import org.minijpa.jpa.criteria.AbstractFrom;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;

public class CollectionJoinImpl<Z, E> extends AbstractFrom<Z, E> implements CollectionJoin<Z, E>,
        FetchJoinSpec {

    private RelationshipMetaAttribute metaAttribute;
    private final Attribute<? super Z, E> attribute;
    private final JoinType joinType;
    private final FetchJoinType fetchJoinType;

    public CollectionJoinImpl(MetaEntity metaEntity, RelationshipMetaAttribute metaAttribute,
                              Attribute<? super Z, E> attribute,
                              JoinType joinType, FetchJoinType fetchJoinType) {
        super(metaEntity);
        this.metaAttribute = metaAttribute;
        this.attribute = attribute;
        this.joinType = joinType;
        this.fetchJoinType = fetchJoinType;
    }

    public RelationshipMetaAttribute getMetaAttribute() {
        return metaAttribute;
    }

    @Override
    public FetchJoinType getFetchJoinType() {
        return fetchJoinType;
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
