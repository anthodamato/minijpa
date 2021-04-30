package org.minijpa.jdbc.relationship;

import java.util.Optional;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;

public abstract class Relationship {

    protected FetchType fetchType = FetchType.EAGER;
    protected String joinColumn;
    protected String joinColumnTable;
    protected MetaEntity owningEntity;
    // for bidirectional relationships
    protected MetaAttribute owningAttribute;

    /**
     * This is the target entity.
     */
    protected MetaEntity attributeType;
    // for bidirectional relationships
    protected MetaAttribute targetAttribute;
    protected Optional<String> mappedBy;
    protected RelationshipJoinTable joinTable;
    protected Class<?> targetEntityClass;
    protected JoinTableAttributes joinTableAttributes;

    public Relationship() {
	super();
    }

    public FetchType getFetchType() {
	return fetchType;
    }

    public String getJoinColumn() {
	return joinColumn;
    }

    public String getJoinColumnTable() {
	return joinColumnTable;
    }

    public MetaEntity getOwningEntity() {
	return owningEntity;
    }

    public MetaAttribute getOwningAttribute() {
	return owningAttribute;
    }

    public MetaEntity getAttributeType() {
	return attributeType;
    }

    public MetaAttribute getTargetAttribute() {
	return targetAttribute;
    }

    public Optional<String> getMappedBy() {
	return mappedBy;
    }

    public boolean isOwner() {
	return mappedBy.isEmpty();
    }

    public RelationshipJoinTable getJoinTable() {
	return joinTable;
    }

    public boolean toMany() {
	return false;
    }

    public boolean toOne() {
	return false;
    }

    public Class<?> getTargetEntityClass() {
	return targetEntityClass;
    }

    public JoinTableAttributes getJoinTableAttributes() {
	return joinTableAttributes;
    }

    @Override
    public String toString() {
	return Relationship.class.getName() + ": fetchType=" + fetchType + "; joinColumn=" + joinColumn;
    }

}
