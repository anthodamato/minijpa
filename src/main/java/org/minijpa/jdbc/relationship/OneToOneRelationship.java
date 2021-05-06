package org.minijpa.jdbc.relationship;

import java.util.Optional;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;

public final class OneToOneRelationship extends Relationship {

    public OneToOneRelationship() {
	super();
    }

    @Override
    public boolean toOne() {
	return true;
    }

    @Override
    public String toString() {
	return OneToOneRelationship.class.getName()
		+ ": joinColumnTable=" + joinColumnTable + "; mappedBy=" + mappedBy + "; fetchType="
		+ fetchType;
    }

    public static class Builder {

	private String joinColumnTable;
	private Optional<String> mappedBy = Optional.empty();
	private FetchType fetchType = FetchType.EAGER;
	private MetaEntity owningEntity;
	private MetaAttribute targetAttribute;
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

	public Builder withMappedBy(Optional<String> mappedBy) {
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

	public Builder withTargetAttribute(MetaAttribute targetAttribute) {
	    this.targetAttribute = targetAttribute;
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

	public Builder withJoinColumnDataList(Optional<JoinColumnDataList> joinColumnDataList) {
	    this.joinColumnDataList = joinColumnDataList;
	    return this;
	}

	public Builder withJoinColumnMapping(Optional<JoinColumnMapping> joinColumnMapping) {
	    this.joinColumnMapping = joinColumnMapping;
	    return this;
	}

	public Builder with(OneToOneRelationship oneToOne) {
	    this.joinColumnTable = oneToOne.joinColumnTable;
	    this.mappedBy = oneToOne.mappedBy;
	    this.fetchType = oneToOne.fetchType;
	    this.owningEntity = oneToOne.owningEntity;
	    this.owningAttribute = oneToOne.owningAttribute;
	    this.targetAttribute = oneToOne.targetAttribute;
	    this.attributeType = oneToOne.attributeType;
	    this.joinColumnDataList = oneToOne.joinColumnDataList;
	    this.joinColumnMapping = oneToOne.getJoinColumnMapping();
	    return this;
	}

	public OneToOneRelationship build() {
	    OneToOneRelationship oto = new OneToOneRelationship();
	    oto.joinColumnTable = joinColumnTable;
	    oto.mappedBy = mappedBy;
	    oto.fetchType = fetchType;
	    oto.owningEntity = owningEntity;
	    oto.targetAttribute = targetAttribute;
	    oto.owningAttribute = owningAttribute;
	    oto.attributeType = attributeType;
	    oto.joinColumnDataList = joinColumnDataList;
	    oto.joinColumnMapping = joinColumnMapping;
	    return oto;
	}
    }
}
