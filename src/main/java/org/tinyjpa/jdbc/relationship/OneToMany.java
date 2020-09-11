package org.tinyjpa.jdbc.relationship;

import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.MetaEntity;

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
		private MetaEntity owningEntity;
		private MetaAttribute owningAttribute;
		private Class<?> collectionClass;
		private Class<?> targetEntityClass;
		private MetaAttribute targetAttribute;
		private RelationshipJoinTable joinTable;
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
