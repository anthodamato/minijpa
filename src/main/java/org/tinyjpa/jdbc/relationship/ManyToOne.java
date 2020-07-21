package org.tinyjpa.jdbc.relationship;

import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.Entity;

public final class ManyToOne extends Relationship {
	public ManyToOne() {
		super();
	}

	@Override
	public boolean isOwner() {
		return true;
	}

	@Override
	public String toString() {
		return ManyToOne.class.getName() + ": joinColumn=" + getJoinColumn() + "; fetchType=" + getFetchType();
	}

	public static class Builder {
		private String joinColumn;
		private FetchType fetchType = FetchType.EAGER;
		private Entity owningEntity;
		private Attribute owningAttribute;
		private Entity attributeType;

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

		public Builder withOwningEntity(Entity owningEntity) {
			this.owningEntity = owningEntity;
			return this;
		}

		public Builder withOwningAttribute(Attribute attribute) {
			this.owningAttribute = attribute;
			return this;
		}

		public Builder withAttributeType(Entity attributeType) {
			this.attributeType = attributeType;
			return this;
		}

		public Builder with(ManyToOne manyToOne) {
			this.joinColumn = manyToOne.joinColumn;
			this.fetchType = manyToOne.fetchType;
			this.owningEntity = manyToOne.owningEntity;
			this.owningAttribute = manyToOne.owningAttribute;
			this.attributeType = manyToOne.attributeType;
			return this;
		}

		public ManyToOne build() {
			ManyToOne r = new ManyToOne();
			r.joinColumn = joinColumn;
			r.fetchType = fetchType;
			r.owningEntity = owningEntity;
			r.owningAttribute = owningAttribute;
			r.attributeType = attributeType;
			return r;
		}
	}
}
