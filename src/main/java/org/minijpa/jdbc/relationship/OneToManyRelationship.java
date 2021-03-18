package org.minijpa.jdbc.relationship;

import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;

public final class OneToManyRelationship extends Relationship {

    private Class<?> collectionClass;

    public OneToManyRelationship() {
	super();
    }

    @Override
    public boolean isOwner() {
	return mappedBy == null || mappedBy.isEmpty();
    }

    @Override
    public boolean toMany() {
	return true;
    }

    @Override
    public String toString() {
	return OneToManyRelationship.class.getName() + ": joinColumn=" + joinColumn + "; mappedBy=" + mappedBy + "; fetchType="
		+ fetchType;
    }

    public static class Builder {

	private String joinColumn;
	private String joinColumnTable;
	private String mappedBy;
	private FetchType fetchType = FetchType.LAZY;
	private MetaEntity owningEntity;
	private MetaAttribute owningAttribute;
	private Class<?> collectionClass;
	private Class<?> targetEntityClass;
	private MetaAttribute targetAttribute;
	private RelationshipJoinTable joinTable;
	private MetaEntity attributeType;
	private JoinTableAttributes joinTableAttributes;

	public Builder() {
	}

	public Builder withJoinColumn(String joinColumn) {
	    this.joinColumn = joinColumn;
	    return this;
	}

	public Builder withJoinColumnTable(String joinColumnTable) {
	    this.joinColumnTable = joinColumnTable;
	    return this;
	}

	public Builder withMappedBy(String mappedBy) {
	    this.mappedBy = mappedBy;
	    return this;
	}

	public Builder withFetchType(FetchType fetchType) {
	    this.fetchType = fetchType;
	    return this;
	}

	public Builder withOwningEntity(MetaEntity owningEntity) {
	    this.owningEntity = owningEntity;
	    return this;
	}

	public Builder withOwningAttribute(MetaAttribute attribute) {
	    this.owningAttribute = attribute;
	    return this;
	}

	public Builder withCollectionClass(Class<?> collectionClass) {
	    this.collectionClass = collectionClass;
	    return this;
	}

	public Builder withTargetEntityClass(Class<?> targetEntityClass) {
	    this.targetEntityClass = targetEntityClass;
	    return this;
	}

	public Builder withTargetAttribute(MetaAttribute targetAttribute) {
	    this.targetAttribute = targetAttribute;
	    return this;
	}

	public Builder withJoinTable(RelationshipJoinTable joinTable) {
	    this.joinTable = joinTable;
	    return this;
	}

	public Builder withAttributeType(MetaEntity attributeType) {
	    this.attributeType = attributeType;
	    return this;
	}

	public Builder withJoinTableAttributes(JoinTableAttributes joinTableAttributes) {
	    this.joinTableAttributes = joinTableAttributes;
	    return this;
	}

	public Builder with(OneToManyRelationship r) {
	    this.joinColumn = r.joinColumn;
	    this.joinColumnTable = r.joinColumnTable;
	    this.mappedBy = r.mappedBy;
	    this.fetchType = r.fetchType;
	    this.owningEntity = r.owningEntity;
	    this.owningAttribute = r.owningAttribute;
	    this.collectionClass = r.collectionClass;
	    this.targetEntityClass = r.targetEntityClass;
	    this.targetAttribute = r.targetAttribute;
	    this.joinTable = r.joinTable;
	    this.attributeType = r.attributeType;
	    this.joinTableAttributes = r.joinTableAttributes;
	    return this;
	}

	public OneToManyRelationship build() {
	    OneToManyRelationship r = new OneToManyRelationship();
	    r.joinColumn = joinColumn;
	    r.joinColumnTable = joinColumnTable;
	    r.mappedBy = mappedBy;
	    r.fetchType = fetchType;
	    r.owningEntity = owningEntity;
	    r.owningAttribute = owningAttribute;
	    r.collectionClass = collectionClass;
	    r.targetEntityClass = targetEntityClass;
	    r.targetAttribute = targetAttribute;
	    r.joinTable = joinTable;
	    r.attributeType = attributeType;
	    r.joinTableAttributes = joinTableAttributes;
	    return r;
	}
    }
}
