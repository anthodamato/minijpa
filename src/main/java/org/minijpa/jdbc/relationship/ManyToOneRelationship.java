package org.minijpa.jdbc.relationship;

import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;

public final class ManyToOneRelationship extends Relationship {

    public ManyToOneRelationship() {
	super();
    }

    @Override
    public boolean isOwner() {
	return true;
    }

    @Override
    public boolean toOne() {
	return true;
    }

    @Override
    public String toString() {
	return ManyToOneRelationship.class.getName() + ": joinColumn=" + getJoinColumn() + "; fetchType=" + getFetchType();
    }

    public static class Builder {

	private String joinColumn;
	private FetchType fetchType = FetchType.EAGER;
	private MetaEntity owningEntity;
	private MetaAttribute owningAttribute;
	private MetaEntity attributeType;

	public Builder() {
	}

	public Builder withJoinColumn(String joinColumn) {
	    this.joinColumn = joinColumn;
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

	public Builder withAttributeType(MetaEntity attributeType) {
	    this.attributeType = attributeType;
	    return this;
	}

	public Builder with(ManyToOneRelationship manyToOne) {
	    this.joinColumn = manyToOne.joinColumn;
	    this.fetchType = manyToOne.fetchType;
	    this.owningEntity = manyToOne.owningEntity;
	    this.owningAttribute = manyToOne.owningAttribute;
	    this.attributeType = manyToOne.attributeType;
	    return this;
	}

	public ManyToOneRelationship build() {
	    ManyToOneRelationship r = new ManyToOneRelationship();
	    r.joinColumn = joinColumn;
	    r.fetchType = fetchType;
	    r.owningEntity = owningEntity;
	    r.owningAttribute = owningAttribute;
	    r.attributeType = attributeType;
	    return r;
	}
    }
}
