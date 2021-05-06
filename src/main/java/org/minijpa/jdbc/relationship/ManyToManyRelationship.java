package org.minijpa.jdbc.relationship;

import java.util.Optional;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;

public final class ManyToManyRelationship extends Relationship {

    private Class<?> collectionClass;

    public ManyToManyRelationship() {
	super();
    }

    @Override
    public boolean toMany() {
	return true;
    }

    @Override
    public String toString() {
	return ManyToManyRelationship.class.getName() + ": mappedBy=" + mappedBy + "; fetchType="
		+ fetchType;
    }

    public static class Builder {

	private String joinColumnTable;
	private Optional<String> mappedBy = Optional.empty();
	private FetchType fetchType = FetchType.LAZY;
	private MetaEntity owningEntity;
	private MetaAttribute owningAttribute;
	private Class<?> collectionClass;
	private Class<?> targetEntityClass;
	private MetaAttribute targetAttribute;
	private RelationshipJoinTable joinTable;
	private MetaEntity attributeType;
	private JoinTableAttributes joinTableAttributes;
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

	public ManyToManyRelationship.Builder withJoinTableAttributes(JoinTableAttributes joinTableAttributes) {
	    this.joinTableAttributes = joinTableAttributes;
	    return this;
	}

	public Builder withJoinColumnDataList(Optional<JoinColumnDataList> joinColumnDataList) {
	    this.joinColumnDataList = joinColumnDataList;
	    return this;
	}

	public ManyToManyRelationship.Builder withJoinColumnMapping(Optional<JoinColumnMapping> joinColumnMapping) {
	    this.joinColumnMapping = joinColumnMapping;
	    return this;
	}

	public Builder with(ManyToManyRelationship r) {
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
	    this.joinColumnDataList = r.joinColumnDataList;
	    this.joinColumnMapping = r.joinColumnMapping;
	    return this;
	}

	public ManyToManyRelationship build() {
	    ManyToManyRelationship r = new ManyToManyRelationship();
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
	    r.joinColumnDataList = joinColumnDataList;
	    r.joinColumnMapping = joinColumnMapping;
	    return r;
	}
    }
}
