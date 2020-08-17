package org.tinyjpa.jdbc.relationship;

import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.Entity;

public final class OneToMany extends Relationship {
	private Class<?> collectionClass;

	public OneToMany() {
		super();
	}

	@Override
	public boolean isOwner() {
		return mappedBy == null || mappedBy.isEmpty();
	}

	@Override
	public boolean toMany() {
		return true;
	}

	@Override
	public String toString() {
		return OneToMany.class.getName() + ": joinColumn=" + joinColumn + "; mappedBy=" + mappedBy + "; fetchType="
				+ fetchType;
	}

	public static class Builder {
		private String joinColumn;
		private String mappedBy;
		private FetchType fetchType = FetchType.LAZY;
		private Entity owningEntity;
		private Attribute owningAttribute;
		private Class<?> collectionClass;
		private Class<?> targetEntityClass;
		private Attribute targetAttribute;
		private RelationshipJoinTable joinTable;
		private Entity attributeType;

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

		public Builder withOwningAttribute(Attribute attribute) {
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

		public Builder withTargetAttribute(Attribute targetAttribute) {
			this.targetAttribute = targetAttribute;
			return this;
		}

		public Builder withJoinTable(RelationshipJoinTable joinTable) {
			this.joinTable = joinTable;
			return this;
		}

		public Builder withAttributeType(Entity attributeType) {
			this.attributeType = attributeType;
			return this;
		}

		public Builder with(OneToMany oneToMany) {
			this.joinColumn = oneToMany.joinColumn;
			this.mappedBy = oneToMany.mappedBy;
			this.fetchType = oneToMany.fetchType;
			this.owningEntity = oneToMany.owningEntity;
			this.owningAttribute = oneToMany.owningAttribute;
			this.collectionClass = oneToMany.collectionClass;
			this.targetEntityClass = oneToMany.targetEntityClass;
			this.targetAttribute = oneToMany.targetAttribute;
			this.joinTable = oneToMany.joinTable;
			this.attributeType = oneToMany.attributeType;
			return this;
		}

		public OneToMany build() {
			OneToMany oto = new OneToMany();
			oto.joinColumn = joinColumn;
			oto.mappedBy = mappedBy;
			oto.fetchType = fetchType;
			oto.owningEntity = owningEntity;
			oto.owningAttribute = owningAttribute;
			oto.collectionClass = collectionClass;
			oto.targetEntityClass = targetEntityClass;
			oto.targetAttribute = targetAttribute;
			oto.joinTable = joinTable;
			oto.attributeType = attributeType;
			return oto;
		}
	}
}
