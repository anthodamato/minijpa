package org.tinyjpa.jdbc.relationship;

import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.MetaEntity;

public final class OneToOne extends Relationship {
	public OneToOne() {
		super();
	}

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
		private MetaEntity owningEntity;
		private MetaAttribute targetAttribute;
		private MetaAttribute owningAttribute;
		private MetaEntity attributeType;

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

		public Builder with(OneToOne oneToOne) {
			this.joinColumn = oneToOne.joinColumn;
			this.mappedBy = oneToOne.mappedBy;
			this.fetchType = oneToOne.fetchType;
			this.owningEntity = oneToOne.owningEntity;
			this.owningAttribute = oneToOne.owningAttribute;
			this.targetAttribute = oneToOne.targetAttribute;
			this.attributeType = oneToOne.attributeType;
			return this;
		}

		public OneToOne build() {
			OneToOne oto = new OneToOne();
			oto.joinColumn = joinColumn;
			oto.mappedBy = mappedBy;
			oto.fetchType = fetchType;
			oto.owningEntity = owningEntity;
			oto.targetAttribute = targetAttribute;
			oto.owningAttribute = owningAttribute;
			oto.attributeType = attributeType;
			return oto;
		}
	}
}
