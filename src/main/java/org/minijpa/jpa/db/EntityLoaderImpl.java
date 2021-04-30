/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.db;

import java.util.List;
import org.minijpa.jdbc.EntityLoader;
import javax.persistence.EntityNotFoundException;
import org.minijpa.jdbc.AttributeUtil;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.JoinColumnAttribute;
import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.MetaEntityHelper;
import org.minijpa.jdbc.db.EntityInstanceBuilder;
import org.minijpa.jdbc.relationship.FetchType;
import org.minijpa.jdbc.relationship.Relationship;
import org.minijpa.metadata.PersistenceUnitContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author adamato
 */
public class EntityLoaderImpl implements EntityLoader {

    private final Logger LOG = LoggerFactory.getLogger(EntityLoaderImpl.class);
    private final PersistenceUnitContext persistenceUnitContext;
    private final EntityInstanceBuilder entityInstanceBuilder;
    private final EntityContainer entityContainer;
    private final EntityQueryLevel entityQueryLevel;
    private final JoinTableCollectionQueryLevel joinTableCollectionQueryLevel;
    private final ForeignKeyCollectionQueryLevel foreignKeyCollectionQueryLevel;

    public EntityLoaderImpl(PersistenceUnitContext persistenceUnitContext, EntityInstanceBuilder entityInstanceBuilder,
	    EntityContainer entityContainer, EntityQueryLevel entityQueryLevel,
	    ForeignKeyCollectionQueryLevel foreignKeyCollectionQueryLevel,
	    JoinTableCollectionQueryLevel joinTableCollectionQueryLevel) {
	this.persistenceUnitContext = persistenceUnitContext;
	this.entityInstanceBuilder = entityInstanceBuilder;
	this.entityContainer = entityContainer;
	this.entityQueryLevel = entityQueryLevel;
	this.foreignKeyCollectionQueryLevel = foreignKeyCollectionQueryLevel;
	this.joinTableCollectionQueryLevel = joinTableCollectionQueryLevel;
    }

    @Override
    public Object findById(MetaEntity metaEntity, Object primaryKey, LockType lockType) throws Exception {
	Object entityInstance = entityContainer.find(metaEntity.getEntityClass(), primaryKey);
	if (entityInstance != null)
	    return entityInstance;

	ModelValueArray<FetchParameter> modelValueArray = entityQueryLevel.run(metaEntity, primaryKey, lockType);
	if (modelValueArray == null)
	    return null;

	entityInstance = entityInstanceBuilder.build(metaEntity, primaryKey);
	buildEntity(entityInstance, modelValueArray, metaEntity, primaryKey, lockType);
	return entityInstance;
    }

    private void buildEntity(Object entityInstance, ModelValueArray<FetchParameter> modelValueArray,
	    MetaEntity metaEntity, Object primaryKey, LockType lockType)
	    throws Exception {
	for (int i = 0; i < modelValueArray.size(); ++i) {
	    FetchParameter fetchParameter = modelValueArray.getModel(i);
	    MetaAttribute attribute = fetchParameter.getAttribute();
	    Object value = modelValueArray.getValue(i);
	    if (fetchParameter.isJoinColumn()) {
		if (attribute.getRelationship().getFetchType() == FetchType.LAZY) {
		    // save the foreign key for lazy attributes
		    MetaEntityHelper.setForeignKeyValue(attribute, entityInstance, value);
		    LOG.debug("findById: saved foreign key value=" + value);
		} else
		    loadRelationshipByForeignKey(entityInstance, metaEntity, attribute, value, lockType);
	    } else
		entityInstanceBuilder.writeAttributeValue(metaEntity, entityInstance,
			attribute, value);
	}

	loadJoinTableRelationships(entityInstance, metaEntity, lockType);
	entityContainer.addManaged(entityInstance, primaryKey);
	MetaEntityHelper.setEntityStatus(metaEntity, entityInstance, EntityStatus.FLUSHED_LOADED_FROM_DB);
	fillCircularRelationships(metaEntity, entityInstance);
    }

    @Override
    public Object queryVersionValue(MetaEntity metaEntity, Object primaryKey, LockType lockType) throws Exception {
	ModelValueArray<FetchParameter> attributeValueArray = entityQueryLevel.runVersionQuery(metaEntity, primaryKey, lockType);
	if (attributeValueArray == null)
	    return null;

	return attributeValueArray.getValue(0);
    }

    @Override
    public void refresh(MetaEntity metaEntity, Object entityInstance, Object primaryKey, LockType lockType) throws Exception {
	ModelValueArray<FetchParameter> modelValueArray = entityQueryLevel.run(metaEntity, primaryKey, lockType);
	if (modelValueArray == null)
	    throw new EntityNotFoundException("Entity '" + entityInstance + "' not found: pk=" + primaryKey);

	buildEntity(entityInstance, modelValueArray, metaEntity, primaryKey, lockType);
    }

    private void fillCircularRelationships(MetaEntity entity, Object entityInstance) throws Exception {
	LOG.debug("fillCircularRelationships: entity=" + entity);
	LOG.debug("fillCircularRelationships: entityInstance=" + entityInstance);
	for (MetaAttribute a : entity.getRelationshipAttributes()) {
	    if (!a.isEager())
		continue;

	    if (a.getRelationship().toOne() && a.getRelationship().isOwner()) {
		LOG.debug("fillCircularRelationships: a=" + a);
		Object value = entityInstanceBuilder.getAttributeValue(entityInstance, a);
		LOG.debug("fillCircularRelationships: value=" + value);
		if (value == null)
		    continue;

		MetaAttribute targetAttribute = a.getRelationship().getTargetAttribute();
		LOG.debug("fillCircularRelationships: targetAttribute=" + targetAttribute);
		LOG.debug("fillCircularRelationships: a.getRelationship().getAttributeType()=" + a.getRelationship().getAttributeType());
		MetaEntity toEntity = a.getRelationship().getAttributeType();
		if (toEntity == null)
		    continue;

		MetaAttribute attribute = toEntity.findAttributeByMappedBy(a.getName());
		LOG.debug("fillCircularRelationships: attribute=" + attribute);
		if (attribute == null)
		    continue;

		// it's bidirectional
		if (attribute.getRelationship().toOne()) {
		    Object v = entityInstanceBuilder.getAttributeValue(value, attribute);
		    LOG.debug("fillCircularRelationships: v=" + v);
		    if (v == null)
			entityInstanceBuilder.writeMetaAttributeValue(value, value.getClass(), attribute, entityInstance, toEntity);
		}
	    }
	}
    }

    @Override
    public Object build(ModelValueArray<FetchParameter> modelValueArray,
	    MetaEntity entity, LockType lockType) throws Exception {
	LOG.debug("build: entity=" + entity);
	Object primaryKey = AttributeUtil.buildPK(entity, modelValueArray);
	LOG.debug("build: primaryKey=" + primaryKey);
	Object entityInstance = entityContainer.find(entity.getEntityClass(), primaryKey);
	if (entityInstance != null)
	    return entityInstance;

	entityInstance = entityInstanceBuilder.build(entity, primaryKey);
	buildEntity(entityInstance, modelValueArray, entity, primaryKey, lockType);
	return entityInstance;
    }

    private void buildAttributeValues(
	    Object parentInstance,
	    MetaEntity metaEntity,
	    List<MetaAttribute> attributes,
	    ModelValueArray<FetchParameter> modelValueArray,
	    LockType lockType) throws Exception {
	// basic attributes and embeddables
	for (MetaAttribute attribute : attributes) {
	    if (attribute.getRelationship() != null) {
		loadJoinTableRelationships(parentInstance, metaEntity, attribute, lockType);
	    } else {
		int index = modelValueArray.indexOfModel(AttributeUtil.fetchParameterToMetaAttribute, attribute);
		if (index == -1)
		    throw new IllegalArgumentException("Column '" + attribute.getColumnName() + "' is missing");

		entityInstanceBuilder.writeMetaAttributeValue(parentInstance,
			parentInstance.getClass(), attribute, modelValueArray.getValue(index), metaEntity);
	    }
	}

	for (MetaEntity embeddable : metaEntity.getEmbeddables()) {
	    Object parent = embeddable.getEntityClass().getDeclaredConstructor().newInstance();
	    buildAttributeValues(parent, embeddable,
		    embeddable.getAttributes(), modelValueArray, lockType);
	    entityInstanceBuilder.writeEmbeddableValue(parentInstance,
		    parentInstance.getClass(), embeddable, parent, metaEntity);
	}

	// join columns
	for (JoinColumnAttribute joinColumnAttribute : metaEntity.getJoinColumnAttributes()) {
	    MetaAttribute attribute = joinColumnAttribute.getForeignKeyAttribute();
	    if (attribute.getRelationship() != null && attribute.getRelationship().toOne()) {
		MetaEntity toEntity = attribute.getRelationship().getAttributeType();
		if (attribute.isLazy()) {
		    Object primaryKey = AttributeUtil.buildPK(toEntity, modelValueArray);
		    MetaEntityHelper.setForeignKeyValue(attribute, parentInstance, primaryKey);
		    continue;
		}

		Object parent = buildEntityByValues(modelValueArray, toEntity, LockType.NONE);
		entityInstanceBuilder.writeMetaAttributeValue(parentInstance,
			parentInstance.getClass(), attribute, parent, metaEntity);
	    }
	}
    }

    private Object buildEntityByValues(ModelValueArray<FetchParameter> modelValueArray,
	    MetaEntity entity, LockType lockType) throws Exception {
	Object primaryKey = AttributeUtil.buildPK(entity, modelValueArray);
	LOG.debug("buildEntityByValues: primaryKey=" + primaryKey);
	Object entityInstance = entityContainer.find(entity.getEntityClass(), primaryKey);
	if (entityInstance != null)
	    return entityInstance;

	entityInstance = entityInstanceBuilder.build(entity, primaryKey);
	buildAttributeValues(entityInstance, entity, entity.getAttributes(), modelValueArray, lockType);
	entityContainer.addManaged(entityInstance, primaryKey);
	MetaEntityHelper.setEntityStatus(entity, entityInstance, EntityStatus.FLUSHED_LOADED_FROM_DB);
	fillCircularRelationships(entity, entityInstance);
	return entityInstance;
    }

    @Override
    public Object buildByValues(ModelValueArray<FetchParameter> modelValueArray,
	    MetaEntity entity, LockType lockType) throws Exception {
	return buildEntityByValues(modelValueArray, entity, lockType);
    }

    private void loadJoinTableRelationships(Object parentInstance, MetaEntity entity, MetaAttribute attribute,
	    LockType lockType) throws Exception {
	if (!attribute.isEager())
	    return;

	if (attribute.getRelationship().getJoinTable() == null)
	    return;

	if (attribute.getRelationship().isOwner()) {
	    MetaEntity e = attribute.getRelationship().getAttributeType();
	    Object pk = AttributeUtil.getIdValue(entity, parentInstance);
	    Object result = joinTableCollectionQueryLevel.run(e, pk, entity.getId(), attribute.getRelationship(), attribute, this);
	    entityInstanceBuilder.writeMetaAttributeValue(parentInstance, parentInstance.getClass(), attribute, result, entity);
	}
    }

    private void loadJoinTableRelationships(Object parentInstance, MetaEntity entity,
	    LockType lockType) throws Exception {
	LOG.debug("loadJoinTableRelationships: entity.getRelationshipAttributes()=" + entity.getRelationshipAttributes());
	// join table relationships
	for (MetaAttribute a : entity.getRelationshipAttributes()) {
	    loadJoinTableRelationships(parentInstance, entity, a, lockType);
	}
    }

    private Object loadRelationshipByForeignKey(Object parentInstance, MetaEntity entity,
	    MetaAttribute foreignKeyAttribute, Object foreignKeyValue, LockType lockType) throws Exception {
	// foreign key on the same table
	LOG.debug("loadRelationshipByForeignKey: foreignKeyAttribute=" + foreignKeyAttribute
		+ "; foreignKeyValue=" + foreignKeyValue);
	MetaEntity e = persistenceUnitContext.getEntities().get(foreignKeyAttribute.getType().getName());
	LOG.debug("loadRelationshipByForeignKey: e=" + e);
	Object foreignKeyInstance = findById(e, foreignKeyValue, lockType);
	LOG.debug("loadRelationshipByForeignKey: foreignKeyInstance=" + foreignKeyInstance);
	if (foreignKeyInstance != null) {
	    entityInstanceBuilder.writeAttributeValue(entity, parentInstance,
		    foreignKeyAttribute, foreignKeyInstance);
	}

	return foreignKeyInstance;
    }

    @Override
    public Object loadAttribute(Object parentInstance, MetaAttribute a, Object value) throws Exception {
	MetaAttribute targetAttribute = null;
	Relationship relationship = a.getRelationship();
	LOG.debug("loadAttribute: a=" + a);
	LOG.debug("loadAttribute: value=" + value);
	LOG.debug("loadAttribute: parentInstance=" + parentInstance);
	LOG.debug("loadAttribute: relationship=" + relationship);
	if (relationship != null)
	    targetAttribute = relationship.getTargetAttribute();

	LOG.debug("loadAttribute: targetAttribute=" + targetAttribute);
	if (relationship == null || !relationship.toMany()) {
	    MetaEntity entity = persistenceUnitContext.getEntities().get(parentInstance.getClass().getName());
	    Object foreignKey = MetaEntityHelper.getForeignKeyValue(a, parentInstance);
	    LOG.debug("loadAttribute: foreignKey=" + foreignKey);
	    Object result = loadRelationshipByForeignKey(parentInstance, entity, a, foreignKey, LockType.NONE);
	    return result;
	}

	LOG.debug("loadAttribute: to Many targetAttribute=" + targetAttribute + "; relationship.getJoinTable()="
		+ relationship.getJoinTable());
	if (relationship.getJoinTable() == null) {
	    MetaEntity entity = persistenceUnitContext.getEntities().get(relationship.getTargetEntityClass().getName());
	    LOG.debug("loadAttribute: entity=" + entity);
	    if (entity == null)
		throw new IllegalArgumentException("Class '" + relationship.getTargetEntityClass().getName() + "' is not an entity");

	    LOG.debug("loadAttribute: relationship.getOwningAttribute()=" + relationship.getOwningAttribute());
	    return foreignKeyCollectionQueryLevel.run(entity, relationship.getOwningAttribute(), parentInstance,
		    LockType.NONE, this);
	}

	MetaEntity entity = relationship.getAttributeType();
	MetaEntity e = persistenceUnitContext.getEntities().get(parentInstance.getClass().getName());
	Object pk = AttributeUtil.getIdValue(e, parentInstance);
	LOG.debug("loadAttribute: pk=" + pk);
	return joinTableCollectionQueryLevel.run(entity, pk, e.getId(), relationship, a, this);
    }
}
