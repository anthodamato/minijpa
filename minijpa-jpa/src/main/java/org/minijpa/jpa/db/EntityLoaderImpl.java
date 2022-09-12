/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.jpa.db;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.minijpa.jdbc.AttributeUtil;
import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.model.FromTable;
import org.minijpa.jdbc.relationship.JoinColumnMapping;
import org.minijpa.jdbc.relationship.Relationship;
import org.minijpa.jpa.MetaEntityHelper;
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
	private final EntityContainer entityContainer;
	private final EntityQueryLevel entityQueryLevel;
	private final JoinTableCollectionQueryLevel joinTableCollectionQueryLevel;
	private final ForeignKeyCollectionQueryLevel foreignKeyCollectionQueryLevel;

	public EntityLoaderImpl(PersistenceUnitContext persistenceUnitContext, EntityContainer entityContainer,
			EntityQueryLevel entityQueryLevel, ForeignKeyCollectionQueryLevel foreignKeyCollectionQueryLevel,
			JoinTableCollectionQueryLevel joinTableCollectionQueryLevel) {
		this.persistenceUnitContext = persistenceUnitContext;
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

//	LOG.debug("findById: metaEntity=" + metaEntity);
//	LOG.debug("findById: primaryKey=" + primaryKey);
		ModelValueArray<FetchParameter> modelValueArray = entityQueryLevel.run(metaEntity, primaryKey, lockType);
		if (modelValueArray == null)
			return null;

//	for (int i = 0; i < modelValueArray.size(); ++i) {
//	    LOG.debug("findById: modelValueArray.getModel(i).getAttribute()=" + modelValueArray.getModel(i).getAttribute());
//	    LOG.debug("findById: modelValueArray.getValue(i)=" + modelValueArray.getValue(i));
//	}
		entityInstance = MetaEntityHelper.build(metaEntity, primaryKey);
		buildAttributeValuesLoadFK(entityInstance, metaEntity, metaEntity.getAttributes(), modelValueArray, lockType);
		entityContainer.addManaged(entityInstance, primaryKey);
		MetaEntityHelper.setEntityStatus(metaEntity, entityInstance, EntityStatus.FLUSHED_LOADED_FROM_DB);
		fillCircularRelationships(metaEntity, entityInstance);
		return entityInstance;
	}

	@Override
	public Object queryVersionValue(MetaEntity metaEntity, Object primaryKey, LockType lockType) throws Exception {
		ModelValueArray<FetchParameter> attributeValueArray = entityQueryLevel.runVersionQuery(metaEntity, primaryKey,
				lockType);
		if (attributeValueArray == null)
			return null;

		return attributeValueArray.getValue(0);
	}

	@Override
	public void refresh(MetaEntity metaEntity, Object entityInstance, Object primaryKey, LockType lockType)
			throws Exception {
		ModelValueArray<FetchParameter> modelValueArray = entityQueryLevel.run(metaEntity, primaryKey, lockType);
		if (modelValueArray == null)
			throw new EntityNotFoundException("Entity '" + entityInstance + "' not found: pk=" + primaryKey);

		buildAttributeValuesLoadFK(entityInstance, metaEntity, metaEntity.getAttributes(), modelValueArray, lockType);
//	entityContainer.addManaged(entityInstance, primaryKey);
		MetaEntityHelper.setEntityStatus(metaEntity, entityInstance, EntityStatus.FLUSHED_LOADED_FROM_DB);
		fillCircularRelationships(metaEntity, entityInstance);
		MetaEntityHelper.removeChanges(metaEntity, entityInstance);
		MetaEntityHelper.clearLazyAttributeLoaded(metaEntity, entityInstance);
	}

	private void fillCircularRelationships(MetaEntity entity, Object entityInstance) throws Exception {
		LOG.debug("fillCircularRelationships: entity=" + entity);
		LOG.debug("fillCircularRelationships: entityInstance=" + entityInstance);
		for (MetaAttribute a : entity.getRelationshipAttributes()) {
			if (!a.isEager())
				continue;

			if (a.getRelationship().toOne() && a.getRelationship().isOwner()) {
				LOG.debug("fillCircularRelationships: a=" + a);
				Object value = MetaEntityHelper.getAttributeValue(entityInstance, a);
				LOG.debug("fillCircularRelationships: value=" + value);
				if (value == null)
					continue;

				MetaAttribute targetAttribute = a.getRelationship().getTargetAttribute();
				LOG.debug("fillCircularRelationships: targetAttribute=" + targetAttribute);
				LOG.debug("fillCircularRelationships: a.getRelationship().getAttributeType()="
						+ a.getRelationship().getAttributeType());
				MetaEntity toEntity = a.getRelationship().getAttributeType();
				if (toEntity == null)
					continue;

				MetaAttribute attribute = toEntity.findAttributeByMappedBy(a.getName());
				LOG.debug("fillCircularRelationships: attribute=" + attribute);
				if (attribute == null)
					continue;

				// it's bidirectional
				if (attribute.getRelationship().toOne()) {
					Object v = MetaEntityHelper.getAttributeValue(value, attribute);
					LOG.debug("fillCircularRelationships: v=" + v);
					if (v == null)
						MetaEntityHelper.writeMetaAttributeValue(value, value.getClass(), attribute, entityInstance,
								toEntity);
				}
			}
		}
	}

	@Override
	public Object build(ModelValueArray<FetchParameter> modelValueArray, FromTable fromTable, LockType lockType)
			throws Exception {
		Optional<MetaEntity> optionalEntity = persistenceUnitContext.findMetaEntityByTableName(fromTable.getName());
		MetaEntity entity = optionalEntity.get();
		LOG.debug("build: entity=" + entity);

		Object primaryKey = AttributeUtil.buildPK(entity.getId(), modelValueArray);
		LOG.debug("build: primaryKey=" + primaryKey);
		Object entityInstance = entityContainer.find(entity.getEntityClass(), primaryKey);
		if (entityInstance != null)
			return entityInstance;

		entityInstance = MetaEntityHelper.build(entity, primaryKey);
		LOG.debug("build: entityInstance=" + entityInstance);
		buildAttributeValuesLoadFK(entityInstance, entity, entity.getAttributes(), modelValueArray, lockType);
		entityContainer.addManaged(entityInstance, primaryKey);
		MetaEntityHelper.setEntityStatus(entity, entityInstance, EntityStatus.FLUSHED_LOADED_FROM_DB);
		fillCircularRelationships(entity, entityInstance);
		return entityInstance;
	}

	private void buildAttributeValuesLoadFK(Object parentInstance, MetaEntity metaEntity,
			List<MetaAttribute> attributes, ModelValueArray<FetchParameter> modelValueArray, LockType lockType)
			throws Exception {
		// basic attributes and embeddables
		for (MetaAttribute attribute : attributes) {
//			LOG.info("buildAttributeValuesLoadFK: attribute=" + attribute);
			if (attribute.getRelationship() != null) {
				loadJoinTableRelationships(parentInstance, metaEntity, attribute, lockType);
			} else {
				int index = modelValueArray.indexOfModel(AttributeUtil.fetchParameterToMetaAttribute, attribute);
//		LOG.debug("buildAttributeValuesLoadFK: index=" + index);
				if (index == -1)
					throw new IllegalArgumentException("Column '" + attribute.getColumnName() + "' is missing");

				MetaEntityHelper.writeMetaAttributeValue(parentInstance, parentInstance.getClass(), attribute,
						modelValueArray.getValue(index), metaEntity);
			}
		}

//	LOG.debug("buildAttributeValuesLoadFK: metaEntity.getEmbeddables()=" + metaEntity.getEmbeddables());
		for (MetaEntity embeddable : metaEntity.getEmbeddables()) {
			Object parent = embeddable.getEntityClass().getDeclaredConstructor().newInstance();
			buildAttributeValuesLoadFK(parent, embeddable, embeddable.getAttributes(), modelValueArray, lockType);
			MetaEntityHelper.writeEmbeddableValue(parentInstance, parentInstance.getClass(), embeddable, parent,
					metaEntity);
		}

//	LOG.debug("buildAttributeValuesLoadFK: metaEntity.getJoinColumnMappings()=" + metaEntity.getJoinColumnMappings());
		// join columns
		for (JoinColumnMapping joinColumnMapping : metaEntity.getJoinColumnMappings()) {
//			LOG.info("buildAttributeValuesLoadFK: joinColumnMapping.getAttribute()=" + joinColumnMapping.getAttribute());
//	    LOG.debug("buildAttributeValuesLoadFK: joinColumnMapping.getForeignKey()=" + joinColumnMapping.getForeignKey());
			Object fk = AttributeUtil.buildPK(joinColumnMapping.getForeignKey(), modelValueArray);
			if (joinColumnMapping.isLazy()) {
				MetaEntityHelper.setForeignKeyValue(joinColumnMapping.getAttribute(), parentInstance, fk);
				continue;
			}

			Object parent = loadRelationshipByForeignKey(parentInstance, metaEntity, joinColumnMapping.getAttribute(),
					fk, lockType);
			MetaEntityHelper.writeMetaAttributeValue(parentInstance, parentInstance.getClass(),
					joinColumnMapping.getAttribute(), parent, metaEntity);
		}
	}

	private void buildAttributeValues(Object parentInstance, MetaEntity metaEntity, List<MetaAttribute> attributes,
			ModelValueArray<FetchParameter> modelValueArray, LockType lockType) throws Exception {
		// basic attributes and embeddables
		for (MetaAttribute attribute : attributes) {
			if (attribute.getRelationship() != null) {
				loadJoinTableRelationships(parentInstance, metaEntity, attribute, lockType);
			} else {
//		LOG.debug("buildAttributeValues: attribute=" + attribute);
				int index = modelValueArray.indexOfModel(AttributeUtil.fetchParameterToMetaAttribute, attribute);
//		LOG.debug("buildAttributeValues: index=" + index);
				if (index == -1)
					throw new IllegalArgumentException("Column '" + attribute.getColumnName() + "' is missing");

				MetaEntityHelper.writeMetaAttributeValue(parentInstance, parentInstance.getClass(), attribute,
						modelValueArray.getValue(index), metaEntity);
			}
		}

//	LOG.debug("buildAttributeValues: metaEntity.getEmbeddables()=" + metaEntity.getEmbeddables());
		// load embeddables
		for (MetaEntity embeddable : metaEntity.getEmbeddables()) {
			Object parent = embeddable.getEntityClass().getDeclaredConstructor().newInstance();
			buildAttributeValues(parent, embeddable, embeddable.getAttributes(), modelValueArray, lockType);
			MetaEntityHelper.writeEmbeddableValue(parentInstance, parentInstance.getClass(), embeddable, parent,
					metaEntity);
		}

//	LOG.debug("buildAttributeValues: metaEntity.getJoinColumnMappings()=" + metaEntity.getJoinColumnMappings());
		// attributes with join columns
		for (JoinColumnMapping joinColumnMapping : metaEntity.getJoinColumnMappings()) {
//	    LOG.debug("buildAttributeValues: joinColumnMapping.getAttribute()=" + joinColumnMapping.getAttribute());
//	    LOG.debug("buildAttributeValues: joinColumnMapping.getForeignKey()=" + joinColumnMapping.getForeignKey());
			Object fk = AttributeUtil.buildPK(joinColumnMapping.getForeignKey(), modelValueArray);
			if (joinColumnMapping.isLazy()) {
				MetaEntityHelper.setForeignKeyValue(joinColumnMapping.getAttribute(), parentInstance, fk);
				continue;
			}

			MetaEntity toEntity = joinColumnMapping.getAttribute().getRelationship().getAttributeType();
//	    LOG.debug("buildAttributeValues: toEntity=" + toEntity);
			Object parent = buildEntityByValues(modelValueArray, toEntity, lockType);
//	    Object parent = loadRelationshipByForeignKey(parentInstance,
//		    metaEntity, joinColumnMapping.getAttribute(), fk, lockType);
			MetaEntityHelper.writeMetaAttributeValue(parentInstance, parentInstance.getClass(),
					joinColumnMapping.getAttribute(), parent, metaEntity);
		}
	}

	private Object buildEntityByValues(ModelValueArray<FetchParameter> modelValueArray, MetaEntity entity,
			LockType lockType) throws Exception {
		Object primaryKey = AttributeUtil.buildPK(entity.getId(), modelValueArray);
		LOG.debug("buildEntityByValues: primaryKey=" + primaryKey);
		LOG.debug("buildEntityByValues: entity=" + entity);
		Object entityInstance = entityContainer.find(entity.getEntityClass(), primaryKey);
		if (entityInstance != null)
			return entityInstance;

		entityInstance = MetaEntityHelper.build(entity, primaryKey);
		buildAttributeValues(entityInstance, entity, entity.getAttributes(), modelValueArray, lockType);
		entityContainer.addManaged(entityInstance, primaryKey);
		MetaEntityHelper.setEntityStatus(entity, entityInstance, EntityStatus.FLUSHED_LOADED_FROM_DB);
		fillCircularRelationships(entity, entityInstance);
		return entityInstance;
	}

	@Override
	public Object buildByValues(ModelValueArray<FetchParameter> modelValueArray, MetaEntity entity, LockType lockType)
			throws Exception {
		return buildEntityByValues(modelValueArray, entity, lockType);
	}

	private void loadJoinTableRelationships(Object parentInstance, MetaEntity entity, MetaAttribute attribute,
			LockType lockType) throws Exception {
		if (!attribute.isEager())
			return;

		if (attribute.getRelationship().getJoinTable() == null)
			return;

		if (attribute.getRelationship().isOwner()) {
			Object pk = AttributeUtil.getIdValue(entity, parentInstance);
			Object result = joinTableCollectionQueryLevel.run(pk, entity.getId(), attribute.getRelationship(),
					attribute, this);
			MetaEntityHelper.writeMetaAttributeValue(parentInstance, parentInstance.getClass(), attribute, result,
					entity);
		}
	}

//	private void loadJoinTableRelationships(Object parentInstance, MetaEntity entity,
//			LockType lockType) throws Exception {
//		LOG.debug("loadJoinTableRelationships: entity.getRelationshipAttributes()=" + entity.getRelationshipAttributes());
//		// join table relationships
//		for (MetaAttribute a : entity.getRelationshipAttributes()) {
//			loadJoinTableRelationships(parentInstance, entity, a, lockType);
//		}
//	}
	private Object loadRelationshipByForeignKey(Object parentInstance, MetaEntity entity,
			MetaAttribute foreignKeyAttribute, Object foreignKeyValue, LockType lockType) throws Exception {
		// foreign key on the same table
		LOG.debug("loadRelationshipByForeignKey: foreignKeyAttribute=" + foreignKeyAttribute + "; foreignKeyValue="
				+ foreignKeyValue);
		LOG.debug("loadRelationshipByForeignKey: parentInstance=" + parentInstance);
		MetaEntity e = persistenceUnitContext.getEntities().get(foreignKeyAttribute.getType().getName());
		LOG.debug("loadRelationshipByForeignKey: e=" + e);
		Object foreignKeyInstance = findById(e, foreignKeyValue, lockType);
		LOG.debug("loadRelationshipByForeignKey: foreignKeyInstance=" + foreignKeyInstance);
		LOG.debug("loadRelationshipByForeignKey: parentInstance=" + parentInstance);
		LOG.debug("loadRelationshipByForeignKey: foreignKeyAttribute=" + foreignKeyAttribute + "; foreignKeyValue="
				+ foreignKeyValue);
		if (foreignKeyInstance != null) {
			MetaEntityHelper.writeAttributeValue(entity, parentInstance, foreignKeyAttribute, foreignKeyInstance);
//	    entityInstanceBuilder.writeMetaAttributeValue(
//		    parentInstance, parentInstance.getClass(), foreignKeyAttribute, foreignKeyInstance, entity);

			MetaAttribute a = e.findAttributeByMappedBy(foreignKeyAttribute.getName());
			LOG.debug("loadRelationshipByForeignKey: a=" + a);
			if (a != null && a.getRelationship().toOne())
				MetaEntityHelper.writeMetaAttributeValue(foreignKeyInstance, foreignKeyInstance.getClass(), a,
						parentInstance, e);
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
				throw new IllegalArgumentException(
						"Class '" + relationship.getTargetEntityClass().getName() + "' is not an entity");

			LOG.debug("loadAttribute: relationship.getOwningAttribute()=" + relationship.getOwningAttribute());
			return foreignKeyCollectionQueryLevel.run(entity, relationship.getOwningAttribute(), parentInstance,
					LockType.NONE, this);
		}

		MetaEntity e = persistenceUnitContext.getEntities().get(parentInstance.getClass().getName());
		Object pk = AttributeUtil.getIdValue(e, parentInstance);
		LOG.debug("loadAttribute: pk=" + pk);
		return joinTableCollectionQueryLevel.run(pk, e.getId(), relationship, a, this);
	}
}
