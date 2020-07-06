package org.tinyjpa.jdbc.relationship;

import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.Entity;

public final class ManyToOne extends AbstractToOne {

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

		public Builder with(ManyToOne manyToOne) {
			this.joinColumn = manyToOne.joinColumn;
			this.fetchType = manyToOne.fetchType;
			this.owningEntity = manyToOne.owningEntity;
			this.owningAttribute = manyToOne.owningAttribute;
			return this;
		}

		public ManyToOne build() {
			ManyToOne oto = new ManyToOne();
			oto.joinColumn = joinColumn;
			oto.fetchType = fetchType;
			oto.owningEntity = owningEntity;
			oto.owningAttribute = owningAttribute;
			return oto;
		}
	}
}
