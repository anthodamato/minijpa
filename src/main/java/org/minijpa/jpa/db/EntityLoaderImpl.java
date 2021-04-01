/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.db;

import org.minijpa.jdbc.EntityLoader;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityNotFoundException;
import org.minijpa.jdbc.AbstractAttributeValue;
import org.minijpa.jdbc.AttributeUtil;
import org.minijpa.jdbc.ColumnNameValue;
import org.minijpa.jdbc.ColumnNameValueUtil;
import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.MetaEntityHelper;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.QueryResultValues;
import org.minijpa.jdbc.db.EntityInstanceBuilder;
import org.minijpa.jdbc.model.SqlSelect;
import org.minijpa.jdbc.relationship.FetchType;
import org.minijpa.jdbc.relationship.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author adamato
 */
public class EntityLoaderImpl implements EntityLoader {

    private final Logger LOG = LoggerFactory.getLogger(EntityLoaderImpl.class);
    private final Map<String, MetaEntity> entities;
    private final EntityInstanceBuilder entityInstanceBuilder;
    private final EntityContainer entityContainer;
    private final EntityQueryLevel entityQueryLevel;
    private final JoinTableCollectionQueryLevel joinTableCollectionQueryLevel;
    private final ForeignKeyCollectionQueryLevel foreignKeyCollectionQueryLevel;
    private final MetaEntityHelper metaEntityHelper = new MetaEntityHelper();

    public EntityLoaderImpl(Map<String, MetaEntity> entities, EntityInstanceBuilder entityInstanceBuilder,
	    EntityContainer entityContainer, EntityQueryLevel entityQueryLevel,
	    ForeignKeyCollectionQueryLevel foreignKeyCollectionQueryLevel,
	    JoinTableCollectionQueryLevel joinTableCollectionQueryLevel) {
	this.entities = entities;
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

	if (entityContainer.isNotFlushedRemove(metaEntity.getEntityClass(), primaryKey))
	    return null;

	SqlSelect sqlSelect = entityQueryLevel.createQuery(metaEntity, lockType);
	QueryResultValues queryResultValues = entityQueryLevel.run(metaEntity, primaryKey, sqlSelect);
	if (queryResultValues == null)
	    return null;

	entityInstance = entityQueryLevel.build(queryResultValues, metaEntity, primaryKey);

	List<ColumnNameValue> columnNameValues = ColumnNameValueUtil.createRelationshipAttrsList(
		queryResultValues.relationshipAttributes, queryResultValues.relationshipValues);
	loadRelationships(entityInstance, metaEntity, columnNameValues, lockType);
	entityContainer.addFlushedPersist(entityInstance, primaryKey);
	entityContainer.setLoadedFromDb(entityInstance);
	fillCircularRelationships(metaEntity, entityInstance);
	return entityInstance;
    }

    @Override
    public Object findByIdNo1StLevelCache(MetaEntity metaEntity, Object primaryKey, LockType lockType) throws Exception {
	SqlSelect sqlSelect = entityQueryLevel.createQuery(metaEntity, lockType);
	QueryResultValues queryResultValues = entityQueryLevel.run(metaEntity, primaryKey, sqlSelect);
	if (queryResultValues == null)
	    return null;

	Object entityInstance = entityQueryLevel.build(queryResultValues, metaEntity, primaryKey);
	return entityInstance;
    }

    @Override
    public void refresh(MetaEntity metaEntity, Object entityInstance, Object primaryKey, LockType lockType) throws Exception {
	SqlSelect sqlSelect = entityQueryLevel.createQuery(metaEntity, lockType);
	QueryResultValues queryResultValues = entityQueryLevel.run(metaEntity, primaryKey, sqlSelect);
	if (queryResultValues == null)
	    throw new EntityNotFoundException("Entity '" + entityInstance + "' not found: pk=" + primaryKey);

	entityInstanceBuilder.setAttributeValues(metaEntity, entityInstance, queryResultValues.attributes, queryResultValues.values);

	List<ColumnNameValue> columnNameValues = ColumnNameValueUtil.createRelationshipAttrsList(
		queryResultValues.relationshipAttributes, queryResultValues.relationshipValues);
	loadRelationships(entityInstance, metaEntity, columnNameValues, lockType);
	entityContainer.addFlushedPersist(entityInstance, primaryKey);
	entityContainer.setLoadedFromDb(entityInstance);
	fillCircularRelationships(metaEntity, entityInstance);
    }

    private void fillCircularRelationships(MetaEntity entity, Object entityInstance) throws Exception {
	LOG.info("fillCircularRelationships: entity=" + entity);
	for (MetaAttribute a : entity.getRelationshipAttributes()) {
	    if (!a.isEager())
		continue;

	    if (a.getRelationship().toOne() && a.getRelationship().isOwner()) {
		LOG.info("fillCircularRelationships: a=" + a);
		Object value = entityInstanceBuilder.getAttributeValue(entityInstance, a);
		LOG.info("fillCircularRelationships: value=" + value);
		if (value != null) {
		    MetaAttribute targetAttribute = a.getRelationship().getTargetAttribute();
		    LOG.info("fillCircularRelationships: targetAttribute=" + targetAttribute);
		    LOG.info("fillCircularRelationships: a.getRelationship().getAttributeType()=" + a.getRelationship().getAttributeType());
		    MetaEntity toEntity = a.getRelationship().getAttributeType();
		    if (toEntity != null) {
			MetaAttribute attribute = toEntity.findAttributeByMappedBy(a.getName());
			if (attribute != null) {
			    // it's bidirectional
			    if (attribute.getRelationship().toOne()) {
				Object v = entityInstanceBuilder.getAttributeValue(value, attribute);
				LOG.info("fillCircularRelationships: v=" + v);
				if (v == null)
				    entityInstanceBuilder.setAttributeValue(value, value.getClass(), attribute, entityInstance, toEntity);
			    }
			}
		    }
		}
	    }
	}
    }

    @Override
    public Object build(QueryResultValues queryResultValues, MetaEntity entity, LockType lockType) throws Exception {
	Object primaryKey = AttributeUtil.createPK(entity, queryResultValues);
	Object entityInstance = entityContainer.find(entity.getEntityClass(), primaryKey);
	if (entityInstance != null) {
	    entityContainer.setLoadedFromDb(entityInstance);
	    return entityInstance;
	}

	List<ColumnNameValue> columnNameValues = ColumnNameValueUtil.createRelationshipAttrsList(
		queryResultValues.relationshipAttributes, queryResultValues.relationshipValues);
	loadRelationships(entityInstance, entity, columnNameValues, lockType);
	return entityInstance;
    }

    private void loadRelationships(Object parentInstance, MetaEntity entity,
	    List<ColumnNameValue> columnNameValues, LockType lockType) throws Exception {
	// foreign key on the same table
	LOG.info("loadRelationships: parentInstance=" + parentInstance);
	for (ColumnNameValue c : columnNameValues) {
	    LOG.info("loadRelationships: c.getForeignKeyAttribute()=" + c.getForeignKeyAttribute() + "; c.getValue()=" + c.getValue());
	    if (c.getForeignKeyAttribute() == null)
		continue;

	    if (c.getForeignKeyAttribute().getRelationship() != null
		    && c.getForeignKeyAttribute().getRelationship().getFetchType() == FetchType.LAZY) {
		// save the foreign key for lazy attributes
		entityContainer.saveForeignKey(parentInstance, c.getForeignKeyAttribute(), c.getValue());
		LOG.info("loadRelationships: saved foreign key c.getValue()=" + c.getValue());
		continue;
	    }

	    loadRelationshipByForeignKey(parentInstance, entity, c.getForeignKeyAttribute(), c.getValue(), lockType);
	}

	LOG.info("loadRelationships: entity.getRelationshipAttributes()=" + entity.getRelationshipAttributes());
	// join table relationships
	for (MetaAttribute a : entity.getRelationshipAttributes()) {
	    if (!a.isEager())
		continue;

	    if (a.getRelationship().getJoinTable() == null)
		continue;

	    if (a.getRelationship().isOwner()) {
		MetaEntity e = a.getRelationship().getAttributeType();
		Object pk = AttributeUtil.getIdValue(entity, parentInstance);
		List<AbstractAttributeValue> attributeValues = joinTableCollectionQueryLevel.createAttributeValues(pk,
			entity.getId(), a.getRelationship());
		SqlSelect sqlSelect = joinTableCollectionQueryLevel.createQuery(e, pk, entity.getId(), a.getRelationship(), attributeValues);
		List<QueryParameter> parameters = metaEntityHelper.convertAbstractAVToQP(attributeValues);
		Object result = joinTableCollectionQueryLevel.run(this, a, parameters, sqlSelect);
		entityInstanceBuilder.setAttributeValue(parentInstance, parentInstance.getClass(), a, result, entity);
	    }
	}
    }

    private Object loadRelationshipByForeignKey(Object parentInstance, MetaEntity entity,
	    MetaAttribute foreignKeyAttribute, Object foreignKeyValue, LockType lockType) throws Exception {
	// foreign key on the same table
	LOG.info("loadRelationshipByForeignKey: foreignKeyAttribute=" + foreignKeyAttribute + "; foreignKeyValue=" + foreignKeyValue);
	MetaEntity e = entities.get(foreignKeyAttribute.getType().getName());
	LOG.info("loadRelationshipByForeignKey: e=" + e);
	Object foreignKeyInstance = findById(e, foreignKeyValue, lockType);
	LOG.info("loadRelationshipByForeignKey: foreignKeyInstance=" + foreignKeyInstance);
	if (foreignKeyInstance != null) {
//	    entityContainer.addFlushedPersist(foreignKeyInstance, foreignKeyValue);
//	    entityContainer.setLoadedFromDb(foreignKeyInstance);
//	    LOG.info("loadRelationshipByForeignKey: entity=" + entity);
//	    LOG.info("loadRelationshipByForeignKey: parentInstance=" + parentInstance);
	    Object parent = AttributeUtil.findParentInstance(parentInstance, entity.getAttributes(), foreignKeyAttribute, entityInstanceBuilder);
	    MetaEntity parentEntity = AttributeUtil.findParentEntity(parent.getClass().getName(), entity);
//	    LOG.info("loadRelationshipByForeignKey: parent=" + parent);
//	    LOG.info("loadRelationshipByForeignKey: e=" + e);
	    entityInstanceBuilder.setAttributeValue(parent, parent.getClass(),
		    foreignKeyAttribute, foreignKeyInstance, parentEntity);
	}

	return foreignKeyInstance;
    }

    @Override
    public Object loadAttribute(Object parentInstance, MetaAttribute a, Object value) throws Exception {
	MetaAttribute targetAttribute = null;
	Relationship relationship = a.getRelationship();
	LOG.info("loadAttribute: a=" + a);
	LOG.info("loadAttribute: value=" + value);
	LOG.info("loadAttribute: parentInstance=" + parentInstance);
	LOG.info("loadAttribute: relationship=" + relationship);
	if (relationship != null)
	    targetAttribute = relationship.getTargetAttribute();

	LOG.info("loadAttribute: targetAttribute=" + targetAttribute);
	if (relationship == null || !relationship.toMany()) {
	    MetaEntity entity = entities.get(parentInstance.getClass().getName());
	    Object foreignKey = entityContainer.getForeignKeyValue(parentInstance, a);
	    LOG.info("loadAttribute: foreignKey=" + foreignKey);
	    Object result = loadRelationshipByForeignKey(parentInstance, entity, a, foreignKey, LockType.NONE);
	    entityContainer.removeForeignKey(parentInstance, a);
	    return result;
	}

	LOG.info("loadAttribute: to Many targetAttribute=" + targetAttribute + "; relationship.getJoinTable()="
		+ relationship.getJoinTable());
	if (relationship.getJoinTable() == null) {
	    MetaEntity entity = entities.get(relationship.getTargetEntityClass().getName());
	    LOG.info("loadAttribute: entity=" + entity);
	    if (entity == null)
		throw new IllegalArgumentException("Class '" + relationship.getTargetEntityClass().getName() + "' is not an entity");

	    LOG.info("loadAttribute: relationship.getOwningAttribute()=" + relationship.getOwningAttribute());
	    List<QueryParameter> parameters = foreignKeyCollectionQueryLevel.createParameters(parentInstance,
		    relationship.getOwningAttribute());
	    foreignKeyCollectionQueryLevel.createQuery(entity, relationship.getOwningAttribute(), parameters);
	    return foreignKeyCollectionQueryLevel.run(this, a, parameters, LockType.NONE);
	}

	MetaEntity entity = relationship.getAttributeType();
	MetaEntity e = entities.get(parentInstance.getClass().getName());
	Object pk = AttributeUtil.getIdValue(e, parentInstance);
	LOG.info("loadAttribute: pk=" + pk);
	List<AbstractAttributeValue> attributeValues = joinTableCollectionQueryLevel.createAttributeValues(pk,
		e.getId(), relationship);
	LOG.info("loadAttribute: attributeValues=" + attributeValues);
	SqlSelect sqlSelect = joinTableCollectionQueryLevel.createQuery(entity, pk, e.getId(), relationship, attributeValues);
	List<QueryParameter> parameters = metaEntityHelper.convertAbstractAVToQP(attributeValues);
	return joinTableCollectionQueryLevel.run(this, a, parameters, sqlSelect);
    }
}
