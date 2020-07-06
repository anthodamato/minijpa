package org.tinyjpa.jdbc.relationship;

import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.Entity;

public final class OneToOne extends AbstractToOne {
	private String mappedBy;
	// for bidirectional relationships
//	private OneToOne owningOneToOne;

	public OneToOne() {
		super();
	}

	public String getMappedBy() {
		return mappedBy;
	}

//	public Attribute getOwningAttribute() {
//		return owningAttribute;
//	}

	@Override
	public boolean isOwner() {
		return mappedBy == null || mappedBy.isEmpty();
	}

	@Override
	public String toString() {
		return OneToOne.class.getName() + ": joinColumn=" + joinColumn + "; mappedBy=" + mappedBy + "; fetchType="
				+ fetchType;
	}

	public static class Builder {
		private String joinColumn;
		private String mappedBy;
		private FetchType fetchType = FetchType.EAGER;
		private Entity owningEntity;
//		private OneToOne owningOneToOne;
		private Attribute owningAttribute;

		public Builder() {
		}

		public Builder withJoinColumn(String joinColumn) {
			this.joinColumn = joinColumn;
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

		public Builder withOwningEntity(Entity owningEntity) {
			this.owningEntity = owningEntity;
			return this;
		}

//		public Builder withOwningOneToOne(OneToOne oneToOne) {
//			this.owningOneToOne = oneToOne;
//			return this;
//		}

		public Builder withOwningAttribute(Attribute attribute) {
			this.owningAttribute = attribute;
			return this;
		}

		public Builder with(OneToOne oneToOne) {
			this.joinColumn = oneToOne.joinColumn;
			this.mappedBy = oneToOne.mappedBy;
			this.fetchType = oneToOne.fetchType;
			this.owningEntity = oneToOne.owningEntity;
			this.owningAttribute = oneToOne.owningAttribute;
			return this;
		}

		public OneToOne build() {
			OneToOne oto = new OneToOne();
			oto.joinColumn = joinColumn;
			oto.mappedBy = mappedBy;
			oto.fetchType = fetchType;
			oto.owningEntity = owningEntity;
//			oto.owningOneToOne = owningOneToOne;
			oto.owningAttribute = owningAttribute;
			return oto;
		}
	}
}
