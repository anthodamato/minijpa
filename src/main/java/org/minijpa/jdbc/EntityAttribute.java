package org.minijpa.jdbc;

public class EntityAttribute {
	private MetaEntity metaEntity;
	private MetaAttribute metaAttribute;

	public EntityAttribute(MetaEntity metaEntity, MetaAttribute metaAttribute) {
		super();
		this.metaEntity = metaEntity;
		this.metaAttribute = metaAttribute;
	}

	public MetaEntity getMetaEntity() {
		return metaEntity;
	}

	public MetaAttribute getMetaAttribute() {
		return metaAttribute;
	}

}
