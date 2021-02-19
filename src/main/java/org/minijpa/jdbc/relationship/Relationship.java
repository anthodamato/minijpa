package org.minijpa.jdbc.relationship;

import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;

public abstract class Relationship {

    protected FetchType fetchType = FetchType.EAGER;
    protected String joinColumn;
    protected MetaEntity owningEntity;
    // for bidirectional relationships
    protected MetaAttribute owningAttribute;

    /**
     * This is the target entity.
     */
    protected MetaEntity attributeType;
    // for bidirectional relationships
    protected MetaAttribute targetAttribute;
    protected String mappedBy;
    protected RelationshipJoinTable joinTable;
    protected Class<?> targetEntityClass;

    public Relationship() {
	super();
    }

    public FetchType getFetchType() {
	return fetchType;
    }

    public String getJoinColumn() {
	return joinColumn;
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

    public String getMappedBy() {
	return mappedBy;
    }

    public boolean isOwner() {
	return false;
    }

    public RelationshipJoinTable getJoinTable() {
	return joinTable;
    }

    public boolean toMany() {
	return false;
    }

    public Class<?> getTargetEntityClass() {
	return targetEntityClass;
    }

    @Override
    public String toString() {
	return Relationship.class.getName() + ": fetchType=" + fetchType + "; joinColumn=" + joinColumn;
    }

}
