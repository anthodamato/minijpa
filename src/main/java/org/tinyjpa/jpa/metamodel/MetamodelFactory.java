package org.tinyjpa.jpa.metamodel;

import java.util.Map;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type.PersistenceType;

import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.MetaEntity;

public class MetamodelFactory {
	private Map<String, MetaEntity> entities;

	public MetamodelFactory(Map<String, MetaEntity> entities) {
		super();
		this.entities = entities;
	}

	public Metamodel build() {
		return null;
	}

	private EntityType<?> buildEntityType(MetaEntity entity) throws Exception {
		EntityType<?> metamodelEntityType = new MetamodelEntityType<Object>();
		MetaAttribute id = entity.getId();
		SingularAttribute<?, ?> idSingularAttribute = new IdSingularAttribute(id.getName(), null, id.getType(),
				entity.getClazz().getDeclaredField(id.getName()), id.getType(),
				new MetamodelType(PersistenceType.BASIC, id.getType()));

		MetamodelEntityType.Builder builder = new MetamodelEntityType.Builder().withId(idSingularAttribute);
		metamodelEntityType = builder.build();
		return metamodelEntityType;
	}
}
