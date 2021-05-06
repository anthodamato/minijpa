package org.minijpa.jdbc.relationship;

import java.util.Optional;
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
	return ManyToOneRelationship.class.getName() + ": fetchType=" + getFetchType();
    }

    public static class Builder {

	private String joinColumnTable;
	private FetchType fetchType = FetchType.EAGER;
	private MetaEntity owningEntity;
	private MetaAttribute owningAttribute;
	private MetaEntity attributeType;
	private Optional<JoinColumnDataList> joinColumnDataList = Optional.empty();
	private Optional<JoinColumnMapping> joinColumnMapping = Optional.empty();

	public Builder() {
	}

	public Builder withJoinColumnTable(String joinColumnTable) {
	    this.joinColumnTable = joinColumnTable;
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

	public ManyToOneRelationship.Builder withJoinColumnDataList(Optional<JoinColumnDataList> joinColumnDataList) {
	    this.joinColumnDataList = joinColumnDataList;
	    return this;
	}

	public ManyToOneRelationship.Builder withJoinColumnMapping(Optional<JoinColumnMapping> joinColumnMapping) {
	    this.joinColumnMapping = joinColumnMapping;
	    return this;
	}

	public Builder with(ManyToOneRelationship manyToOne) {
	    this.joinColumnTable = manyToOne.joinColumnTable;
	    this.fetchType = manyToOne.fetchType;
	    this.owningEntity = manyToOne.owningEntity;
	    this.owningAttribute = manyToOne.owningAttribute;
	    this.attributeType = manyToOne.attributeType;
	    this.joinColumnDataList = manyToOne.joinColumnDataList;
	    this.joinColumnMapping = manyToOne.joinColumnMapping;
	    return this;
	}

	public ManyToOneRelationship build() {
	    ManyToOneRelationship r = new ManyToOneRelationship();
	    r.joinColumnTable = joinColumnTable;
	    r.fetchType = fetchType;
	    r.owningEntity = owningEntity;
	    r.owningAttribute = owningAttribute;
	    r.attributeType = attributeType;
	    r.joinColumnDataList = joinColumnDataList;
	    r.joinColumnMapping = joinColumnMapping;
	    return r;
	}
    }
}
