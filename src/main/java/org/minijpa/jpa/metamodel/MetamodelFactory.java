package org.minijpa.jpa.metamodel;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.Bindable.BindableType;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MappedSuperclassType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type.PersistenceType;

import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.Pk;

public class MetamodelFactory {
//	private Logger LOG = LoggerFactory.getLogger(MetamodelFactory.class);

    private final Map<String, MetaEntity> entities;

    public MetamodelFactory(Map<String, MetaEntity> entities) {
	super();
	this.entities = entities;
    }

    public Metamodel build() throws Exception {
	Set<MappedSuperclassType<?>> mappedSuperclassTypes = buildMappedSuperclassTypes(entities);
	Set<EntityType<?>> entityTypes = buildEntityTypes(entities);
	Set<EmbeddableType<?>> embeddableTypes = buildEmbeddableTypes(entities);

	Set<ManagedType<?>> managedTypes = new HashSet<>();
	managedTypes.addAll(mappedSuperclassTypes);
	managedTypes.addAll(embeddableTypes);
	managedTypes.addAll(entityTypes);

	return new MetamodelImpl(managedTypes, entityTypes, embeddableTypes);
    }

    private Set<EntityType<?>> buildEntityTypes(Map<String, MetaEntity> entities) throws Exception {
	Set<EntityType<?>> entityTypes = new HashSet<>();
	for (Map.Entry<String, MetaEntity> entry : entities.entrySet()) {
	    entityTypes.add(buildEntityType(entry.getValue()));
	}

	return entityTypes;
    }

    private Field findField(Class<?> c, String fieldName) throws Exception {
	Field field = null;
	try {
	    field = c.getDeclaredField(fieldName);
	} catch (NoSuchFieldException e) {
	    Class<?> sc = c.getSuperclass();
	    field = sc.getDeclaredField(fieldName);
	}

	return field;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private EntityType<?> buildEntityType(MetaEntity entity) throws Exception {
	EntityType<?> metamodelEntityType = new MetamodelEntityType<>();
	Pk id = entity.getId();
	Field fieldId = findField(entity.getEntityClass(), id.getName());
	SingularAttribute<?, ?> idSingularAttribute = new IdSingularAttribute(id.getName(), null, id.getType(), fieldId,
		id.getType(), new MetamodelType(PersistenceType.BASIC, id.getType()));

	Set<SingularAttribute> singularAttributes = buildSingularAttributes(entity);
	Set<Attribute> allAttributes = new HashSet<>();
	allAttributes.addAll(singularAttributes);
	Set<Attribute> attributes = Collections.unmodifiableSet(allAttributes);

	MetamodelEntityType.Builder builder = new MetamodelEntityType.Builder()
		.withBindableType(BindableType.ENTITY_TYPE).withJavaType(entity.getEntityClass())
		.withBindableJavaType(entity.getEntityClass()).withId(idSingularAttribute)
		.withSingularAttributes(singularAttributes).withPersistenceType(PersistenceType.ENTITY)
		.withName(entity.getName()).withAttributes(attributes);
	metamodelEntityType = builder.build();
	return metamodelEntityType;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private SingularAttribute buildSingularAttribute(MetaAttribute metaAttribute) {
	MetamodelSingularAttribute.Builder builder = new MetamodelSingularAttribute.Builder();
	MetamodelSingularAttribute metamodelSingularAttribute = builder.withName(metaAttribute.getName())
		.withBindableJavaType(metaAttribute.getType()).withBindableType(BindableType.SINGULAR_ATTRIBUTE)
		.withPersistentAttributeType(PersistentAttributeType.BASIC).withJavaType(metaAttribute.getType())
		.withType(new MetamodelType(PersistenceType.BASIC, metaAttribute.getType()))
		.withJavaMember(metaAttribute.getJavaMember()).build();
	return metamodelSingularAttribute;
    }

    @SuppressWarnings("rawtypes")
    private Set<SingularAttribute> buildSingularAttributes(MetaEntity entity) {
	Set<SingularAttribute> singularAttributes = new HashSet<>();
	for (MetaAttribute attribute : entity.getAttributes()) {
	    if (!attribute.isCollection()) {
		singularAttributes.add(buildSingularAttribute(attribute));
	    }
	}

	return Collections.unmodifiableSet(singularAttributes);
    }

    private Set<EmbeddableType<?>> buildEmbeddableTypes(Map<String, MetaEntity> entities) throws Exception {
	Set<EmbeddableType<?>> embeddableTypes = new HashSet<>();
	Set<MetaEntity> incEmbeddables = new HashSet<>();
	for (Map.Entry<String, MetaEntity> entry : entities.entrySet()) {
	    MetaEntity metaEntity = entry.getValue();
	    Set<MetaEntity> embeddables = metaEntity.findEmbeddables();
	    for (MetaEntity embeddable : embeddables) {
		if (!incEmbeddables.contains(embeddable)) {
		    embeddableTypes.add(buildEmbeddableType(embeddable));
		    incEmbeddables.add(embeddable);
		}
	    }
	}

	return embeddableTypes;
    }

    @SuppressWarnings("rawtypes")
    private EmbeddableType<?> buildEmbeddableType(MetaEntity entity) throws Exception {
	EmbeddableType<?> metamodelEntityType = new MetamodelEmbeddableType<>();
	Set<SingularAttribute> singularAttributes = buildSingularAttributes(entity);
	Set<Attribute> allAttributes = new HashSet<>();
	allAttributes.addAll(singularAttributes);
	Set<Attribute> attributes = Collections.unmodifiableSet(allAttributes);

	MetamodelEmbeddableType.Builder builder = new MetamodelEmbeddableType.Builder()
		.withJavaType(entity.getEntityClass()).withSingularAttributes(singularAttributes)
		.withPersistenceType(PersistenceType.EMBEDDABLE).withAttributes(attributes);
	metamodelEntityType = builder.build();
	return metamodelEntityType;
    }

    private Set<MappedSuperclassType<?>> buildMappedSuperclassTypes(Map<String, MetaEntity> entities) throws Exception {
	Set<MappedSuperclassType<?>> mappedSuperclassTypes = new HashSet<>();
	Set<MetaEntity> incMappedSuperclass = new HashSet<>();
	for (Map.Entry<String, MetaEntity> entry : entities.entrySet()) {
	    MetaEntity metaEntity = entry.getValue();
	    MetaEntity mappedSuperclass = metaEntity.getMappedSuperclassEntity();
	    if (mappedSuperclass == null)
		continue;

	    if (!incMappedSuperclass.contains(mappedSuperclass)) {
		mappedSuperclassTypes.add(buildMappedSuperclassType(mappedSuperclass));
		incMappedSuperclass.add(mappedSuperclass);
	    }
	}

	return mappedSuperclassTypes;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private MappedSuperclassType<?> buildMappedSuperclassType(MetaEntity entity) throws Exception {
	MappedSuperclassType<?> metamodelEntityType = new MetamodelMappedSuperclassType<>();
	Pk id = entity.getId();

	SingularAttribute<?, ?> idSingularAttribute = new IdSingularAttribute(id.getName(), null, id.getType(),
		entity.getEntityClass().getDeclaredField(id.getName()), id.getType(),
		new MetamodelType(PersistenceType.BASIC, id.getType()));

	Set<SingularAttribute> singularAttributes = buildSingularAttributes(entity);
	Set<Attribute> allAttributes = new HashSet<>();
	allAttributes.addAll(singularAttributes);
	Set<Attribute> attributes = Collections.unmodifiableSet(allAttributes);

	MetamodelMappedSuperclassType.Builder builder = new MetamodelMappedSuperclassType.Builder()
		.withJavaType(entity.getEntityClass()).withId(idSingularAttribute)
		.withSingularAttributes(singularAttributes).withPersistenceType(PersistenceType.MAPPED_SUPERCLASS)
		.withAttributes(attributes);
	metamodelEntityType = builder.build();
	return metamodelEntityType;
    }

}
