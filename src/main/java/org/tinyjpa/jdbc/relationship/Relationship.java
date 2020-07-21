package org.tinyjpa.jdbc.relationship;

import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.Entity;

public abstract class Relationship {
	protected FetchType fetchType = FetchType.EAGER;
	protected String joinColumn;
	protected Entity owningEntity;
	// for bidirectional relationships
	protected Attribute owningAttribute;
	protected Entity attributeType;
	// for bidirectional relationships
	protected Attribute targetAttribute;
	protected String mappedBy;

	public Relationship() {
		super();
	}

	public FetchType getFetchType() {
		return fetchType;
	}

	public String getJoinColumn() {
		return joinColumn;
	}

	public Entity getOwningEntity() {
		return owningEntity;
	}

	public Attribute getOwningAttribute() {
		return owningAttribute;
	}

	public Entity getAttributeType() {
		return attributeType;
	}

	public Attribute getTargetAttribute() {
		return targetAttribute;
	}

	public String getMappedBy() {
		return mappedBy;
	}

	public boolean isOwner() {
		return false;
	}

	@Override
	public String toString() {
		return Relationship.class.getName() + ": fetchType=" + fetchType + "; joinColumn=" + joinColumn;
	}

}
