package org.tinyjpa.jpa.metamodel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.Bindable.BindableType;
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

	public Metamodel build() throws Exception {
		Set<EntityType<?>> entityTypes = buildEntityTypes(entities);
		return new MetamodelImpl(null, entityTypes, null);
	}

	private Set<EntityType<?>> buildEntityTypes(Map<String, MetaEntity> entities) throws Exception {
		Set<EntityType<?>> entityTypes = new HashSet<EntityType<?>>();
		for (Map.Entry<String, MetaEntity> entry : entities.entrySet()) {
			entityTypes.add(buildEntityType(entry.getValue()));
		}

		return entityTypes;
	}

	private EntityType<?> buildEntityType(MetaEntity entity) throws Exception {
		EntityType<?> metamodelEntityType = new MetamodelEntityType<Object>();
		MetaAttribute id = entity.getId();
		SingularAttribute<?, ?> idSingularAttribute = new IdSingularAttribute(id.getName(), null, id.getType(),
				entity.getClazz().getDeclaredField(id.getName()), id.getType(),
				new MetamodelType(PersistenceType.BASIC, id.getType()));

		Set<SingularAttribute> singularAttributes = buildSingularAttributes(entity);
		Set<Attribute> allAttributes = new HashSet<>();
		allAttributes.addAll(singularAttributes);
		Set<Attribute> attributes = Collections.unmodifiableSet(allAttributes);

		MetamodelEntityType.Builder builder = new MetamodelEntityType.Builder()
				.withBindableType(BindableType.ENTITY_TYPE).withJavaType(entity.getClazz())
				.withBindableJavaType(entity.getClazz()).withId(idSingularAttribute)
				.withSingularAttributes(singularAttributes).withPersistenceType(PersistenceType.ENTITY)
				.withName(entity.getTableName()).withAttributes(attributes);
		metamodelEntityType = builder.build();
		return metamodelEntityType;
	}

	private SingularAttribute buildSingularAttribute(MetaAttribute metaAttribute) {
		MetamodelSingularAttribute.Builder builder = new MetamodelSingularAttribute.Builder();
		MetamodelSingularAttribute metamodelSingularAttribute = builder.withName(metaAttribute.getName())
				.withBindableJavaType(metaAttribute.getType()).withBindableType(BindableType.SINGULAR_ATTRIBUTE)
				.withPersistentAttributeType(PersistentAttributeType.BASIC).withJavaType(metaAttribute.getType())
				.withType(new MetamodelType(PersistenceType.BASIC, metaAttribute.getType()))
				.withJavaMember(metaAttribute.getJavaMember()).build();
		return metamodelSingularAttribute;
	}

	private Set<SingularAttribute> buildSingularAttributes(MetaEntity entity) {
		Set<SingularAttribute> singularAttributes = new HashSet<SingularAttribute>();
		for (MetaAttribute attribute : entity.getAttributes()) {
			if (!attribute.isCollection() && !attribute.isEmbedded()) {
				singularAttributes.add(buildSingularAttribute(attribute));
			}
		}

		return Collections.unmodifiableSet(singularAttributes);
	}
}
