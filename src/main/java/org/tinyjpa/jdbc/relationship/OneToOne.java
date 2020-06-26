package org.tinyjpa.jdbc.relationship;

import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.Entity;

public class OneToOne {
	private String joinColumn;
	private String mappedBy;
	private FetchType fetchType = FetchType.EAGER;
	private Entity owningEntity;
	// for bidirectional relationships
	private OneToOne owningOneToOne;
	private Attribute owningAttribute;

	public OneToOne() {
		super();
	}

	public String getJoinColumn() {
		return joinColumn;
	}

	public String getMappedBy() {
		return mappedBy;
	}

	public FetchType getFetchType() {
		return fetchType;
	}

	public Entity getOwningEntity() {
		return owningEntity;
	}

	public OneToOne getOwningOneToOne() {
		return owningOneToOne;
	}

	public Attribute getOwningAttribute() {
		return owningAttribute;
	}

	public boolean isOwner() {
		return mappedBy == null || mappedBy.isEmpty();
	}

	public OneToOne copyWithJoinColumn(String joinColumnName) {
		OneToOne oto = new OneToOne();
		oto.joinColumn = joinColumnName;
		oto.mappedBy = mappedBy;
		oto.fetchType = fetchType;
		oto.owningEntity = owningEntity;
		oto.owningOneToOne = owningOneToOne;
		oto.owningAttribute = owningAttribute;
		return oto;
	}

	public OneToOne copyWithOwningEntity(Entity owningEntity) {
		OneToOne oto = new OneToOne();
		oto.joinColumn = joinColumn;
		oto.mappedBy = mappedBy;
		oto.fetchType = fetchType;
		oto.owningEntity = owningEntity;
		oto.owningOneToOne = owningOneToOne;
		oto.owningAttribute = owningAttribute;
		return oto;
	}

	public OneToOne copyWithOwningOneToOne(OneToOne owningOneToOne) {
		OneToOne oto = new OneToOne();
		oto.joinColumn = joinColumn;
		oto.mappedBy = mappedBy;
		oto.fetchType = fetchType;
		oto.owningEntity = owningEntity;
		oto.owningOneToOne = owningOneToOne;
		oto.owningAttribute = owningAttribute;
		return oto;
	}

	public OneToOne copyWithOwningAttribute(Attribute attribute) {
		OneToOne oto = new OneToOne();
		oto.joinColumn = joinColumn;
		oto.mappedBy = mappedBy;
		oto.fetchType = fetchType;
		oto.owningEntity = owningEntity;
		oto.owningOneToOne = owningOneToOne;
		oto.owningAttribute = attribute;
		return oto;
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
		private OneToOne owningOneToOne;
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

		public Builder withOwningOneToOne(OneToOne oneToOne) {
			this.owningOneToOne = oneToOne;
			return this;
		}

		public Builder withOwningAttribute(Attribute attribute) {
			this.owningAttribute = attribute;
			return this;
		}

		public OneToOne build() {
			OneToOne oto = new OneToOne();
			oto.joinColumn = joinColumn;
			oto.mappedBy = mappedBy;
			oto.fetchType = fetchType;
			oto.owningEntity = owningEntity;
			oto.owningOneToOne = owningOneToOne;
			oto.owningAttribute = owningAttribute;
			return oto;
		}
	}
}
