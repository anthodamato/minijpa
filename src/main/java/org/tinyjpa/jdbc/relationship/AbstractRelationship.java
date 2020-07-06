package org.tinyjpa.jdbc.relationship;

import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.Entity;

public class AbstractRelationship {
	protected FetchType fetchType = FetchType.EAGER;
	protected String joinColumn;
	protected Entity owningEntity;
	// for bidirectional relationships
	protected Attribute owningAttribute;

	public AbstractRelationship() {
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

	public boolean isOwner() {
		return false;
	}

	@Override
	public String toString() {
		return AbstractRelationship.class.getName() + ": fetchType=" + fetchType + "; joinColumn=" + joinColumn;
	}

}
