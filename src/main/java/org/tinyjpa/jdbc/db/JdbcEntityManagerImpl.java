package org.tinyjpa.jdbc.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.AttributeUtil;
import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.AttributeValueConverter;
import org.tinyjpa.jdbc.ColumnNameValue;
import org.tinyjpa.jdbc.ColumnNameValueUtil;
import org.tinyjpa.jdbc.ConnectionHolder;
import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jdbc.JdbcRunner;
import org.tinyjpa.jdbc.JoinColumnAttribute;
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.jdbc.relationship.FetchType;
import org.tinyjpa.jdbc.relationship.Relationship;
import org.tinyjpa.jdbc.relationship.RelationshipJoinTable;

public class JdbcEntityManagerImpl implements AttributeLoader, JdbcEntityManager {
	private Logger LOG = LoggerFactory.getLogger(JdbcEntityManagerImpl.class);
	private DbConfiguration dbConfiguration;
	private Map<String, MetaEntity> entities;
	private EntityContainer entityContainer;
	private EntityInstanceBuilder entityInstanceBuilder;
	private AttributeValueConverter attributeValueConverter;
	private ConnectionHolder connectionHolder;
	private JdbcRunner jdbcRunner = new JdbcRunner();

	public JdbcEntityManagerImpl(DbConfiguration dbConfiguration, Map<String, MetaEntity> entities,
			EntityContainer entityContainer, EntityInstanceBuilder entityInstanceBuilder,
			AttributeValueConverter attributeValueConverter, ConnectionHolder connectionHolder) {
		super();
		this.dbConfiguration = dbConfiguration;
		this.entities = entities;
		this.entityContainer = entityContainer;
		this.entityInstanceBuilder = entityInstanceBuilder;
		this.attributeValueConverter = attributeValueConverter;
		this.connectionHolder = connectionHolder;
	}

	public Object findById(Class<?> entityClass, Object primaryKey) throws Exception {
		return findById(entityClass, primaryKey, null, null);
	}

	private Object findById(Class<?> entityClass, Object primaryKey, MetaAttribute childAttribute,
			Object childAttributeValue) throws Exception {
		Object entityInstance = entityContainer.find(entityClass, primaryKey);
		if (entityInstance != null)
			return entityInstance;

		MetaEntity entity = entities.get(entityClass.getName());
		if (entity == null)
			throw new IllegalArgumentException("Class '" + entityClass.getName() + "' is not an entity");

		SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateSelectById(entity, primaryKey);
		JdbcRunner.AttributeValues attributeValues = jdbcRunner.findById(connectionHolder.getConnection(), sqlStatement,
				entity);
		if (attributeValues == null)
			return null;

		return createAndSaveEntityInstance(attributeValues, entity, childAttribute, childAttributeValue, primaryKey);
	}

	private Object createAndSaveEntityInstance(JdbcRunner.AttributeValues attributeValues, MetaEntity entity,
			MetaAttribute childAttribute, Object childAttributeValue, Object primaryKey) throws Exception {
		LOG.info("createAndSaveEntityInstance: entity=" + entity);
		Object entityObject = entityInstanceBuilder.build(entity, attributeValues.attributes, attributeValues.values,
				primaryKey);
		LOG.info("createAndSaveEntityInstance: primaryKey=" + primaryKey + "; entityObject=" + entityObject);

		// saves the foreign key values. Foreign key values are stored on a db table but
		// they don't have a field in the entity instance, so they are retrieved from db
		// and saved in the persistence context. Later, those values can be used to
		// create the relationship instance.
		List<ColumnNameValue> columnNameValues = ColumnNameValueUtil.createRelationshipAttrsList(
				attributeValues.relationshipAttributes, attributeValues.relationshipValues);
		columnNameValues.stream().filter(c -> c.getForeignKeyAttribute() != null).forEach(c -> {
			LOG.info("createAndSaveEntityInstance: parentInstance=" + entityObject
					+ "; columnNameValue.getForeignKeyAttribute()=" + c.getForeignKeyAttribute()
					+ "; columnNameValue.getValue()=" + c.getValue());
			entityContainer.saveForeignKey(entityObject, c.getForeignKeyAttribute(), c.getValue());
		});

		loadRelationshipAttributes(entityObject, entity, childAttribute, childAttributeValue);
		entityContainer.save(entityObject, primaryKey);
		entityContainer.setLoadedFromDb(entityObject);
		return entityObject;
	}

	@Override
	public Object createAndSaveEntityInstance(JdbcRunner.AttributeValues attributeValues, MetaEntity entity,
			MetaAttribute childAttribute, Object childAttributeValue) throws Exception {
		Object primaryKey = AttributeUtil.createPK(entity, attributeValues);
		return createAndSaveEntityInstance(attributeValues, entity, childAttribute, childAttributeValue, primaryKey);
	}

	/**
	 * Executes a query like: 'select (Entity fields) from table where
	 * pk=foreignkey'
	 * 
	 * @param entityClass
	 * @param foreignKey
	 * @param childAttribute
	 * @param childAttributeValue
	 * @return
	 * @throws Exception
	 */
	private List<Object> findCollectionByForeignKey(Class<?> entityClass, Object foreignKey,
			MetaAttribute foreignKeyAttribute, MetaAttribute childAttribute, Object childAttributeValue) throws Exception {
//		Object entityInstance = entityContainer.find(entityClass, foreignKey);
//		if (entityInstance != null)
//			return entityInstance;

		MetaEntity entity = entities.get(entityClass.getName());
		if (entity == null)
			throw new IllegalArgumentException("Class '" + entityClass.getName() + "' is not an entity");

		SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateSelectByForeignKey(entity, foreignKeyAttribute,
				foreignKey);
		return jdbcRunner.findCollectionById(connectionHolder.getConnection(), sqlStatement, entity, this,
				childAttribute, childAttributeValue);
	}

	private void loadRelationshipAttributes(Object parentInstance, MetaEntity entity, MetaAttribute childAttribute,
			Object childAttributeValue) throws Exception {
		LOG.info("loadRelationshipAttributes: childAttribute=" + childAttribute);
		for (MetaAttribute a : entity.getRelationshipAttributes()) {
			LOG.info("loadRelationshipAttributes: a=" + a);
			if (childAttribute != null && a == childAttribute) {
				// the attribute value is already available
				entityInstanceBuilder.setAttributeValue(parentInstance, parentInstance.getClass(), childAttribute,
						childAttributeValue);
			} else {
				if (a.isEager()) {
					if (a.getRelationship().getJoinTable() != null) {
						MetaEntity e = a.getRelationship().getAttributeType();
						Object pk = AttributeUtil.getIdValue(entity, parentInstance);
						SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateSelectByJoinTable(e,
								entity.getId(), pk, a.getRelationship().getJoinTable());
						List<Object> objects = jdbcRunner.findCollectionById(connectionHolder.getConnection(),
								sqlStatement, entity, this, childAttribute, childAttributeValue);
						entityInstanceBuilder.setAttributeValue(parentInstance, parentInstance.getClass(), a, objects);
					} else {
						loadAttributeValueWithTableFK(parentInstance, a, null, null);
					}
				}
			}
		}
	}

	/**
	 * Loads an attribute that has the foreign key on the same entity table. The
	 * foreign key is saved in the persistence context.
	 * 
	 * @param parentInstance
	 * @param a
	 * @param childAttribute
	 * @param childAttributeValue
	 * @return
	 * @throws Exception
	 */
	private Object loadAttributeValueWithTableFK(Object parentInstance, MetaAttribute a, MetaAttribute childAttribute,
			Object childAttributeValue) throws Exception {
		LOG.info("loadAttributeValue: parentInstance=" + parentInstance);
		Object foreignKey = entityContainer.getForeignKeyValue(parentInstance, a);
//		LOG.info("loadAttributeValue: a=" + a + "; oneToOne=" + a.getOneToOne() + "; foreignKey=" + foreignKey);
		Object foreignKeyInstance = findById(a.getType(), foreignKey, childAttribute, childAttributeValue);
		LOG.info("loadAttributeValue: foreignKeyInstance=" + foreignKeyInstance);
		if (foreignKeyInstance != null) {
			entityContainer.save(foreignKeyInstance, foreignKey);
			entityInstanceBuilder.setAttributeValue(parentInstance, parentInstance.getClass(), a, foreignKeyInstance);
		}

		return foreignKeyInstance;
	}

	/**
	 * Loads the 'parentInstance's collection (or map) specified by the attribute
	 * 'a'. The attribute 'a' type can be one of 'java.util.Collection',
	 * 'java.util.List' or 'java.util.Map', etc. For example, in order to load the
	 * list of Employee for a given Department (foreign key) we have to pass:
	 * 
	 * - the department instance, so we can get the foreign key - the Employee class
	 * 
	 * @param parentInstance
	 * @param a
	 * @param childAttribute
	 * @param childAttributeValue
	 * @return
	 * @throws Exception
	 */
	private List<Object> loadAttributeValues(Object parentInstance, Class<?> targetEntity,
			MetaAttribute foreignKeyAttribute, MetaAttribute childAttribute, Object childAttributeValue) throws Exception {
		LOG.info("loadAttributeValues: parentInstance=" + parentInstance);
		List<Object> objects = findCollectionByForeignKey(targetEntity, parentInstance, foreignKeyAttribute,
				childAttribute, childAttributeValue);
		return objects;
	}

	/**
	 * Used for lazy loading attributes.
	 * 
	 * @param parentInstance the parent instance
	 * @param a              the attribute to load
	 * @return the instance loaded. It can be a collection
	 */
	@Override
	public Object load(Object parentInstance, MetaAttribute a) throws Exception {
		LOG.info("load (lazy): parentInstance=" + parentInstance + "; a=" + a);
		MetaAttribute targetAttribute = null;
		Relationship relationship = a.getRelationship();
		if (relationship != null)
			targetAttribute = relationship.getTargetAttribute();

		if (relationship != null && relationship.toMany()) {
			LOG.info("load (lazy): oneToMany targetAttribute=" + targetAttribute);
			if (relationship.getJoinTable() != null) {
				MetaEntity entity = a.getRelationship().getAttributeType();
				MetaEntity e = entities.get(parentInstance.getClass().getName());
				Object pk = AttributeUtil.getIdValue(e, parentInstance);
				SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateSelectByJoinTable(entity, e.getId(), pk,
						a.getRelationship().getJoinTable());
				List<Object> objects = jdbcRunner.findCollectionById(connectionHolder.getConnection(), sqlStatement,
						entity, this, null, null);
				LOG.info("load (lazy): oneToMany objects.size()=" + objects.size());
				return objects;
			}

			return loadAttributeValues(parentInstance, relationship.getTargetEntityClass(),
					relationship.getOwningAttribute(), targetAttribute, parentInstance);
		}

		LOG.info("load (lazy): owningAttribute=" + targetAttribute);
		return loadAttributeValueWithTableFK(parentInstance, a, targetAttribute, parentInstance);
	}

	/**
	 * Checks the entity instance. It returns true if entity data are enough to
	 * insert the instance on db. If the instance is not ready to be inserted on db
	 * then it should be marked as 'pending new' entity.
	 * 
	 * @param entityInstance
	 * @return
	 * @throws Exception
	 */
	protected boolean canPersistOnDb(Object entityInstance) throws Exception {
		MetaEntity entity = entities.get(entityInstance.getClass().getName());
		for (JoinColumnAttribute joinColumnAttribute : entity.getJoinColumnAttributes()) {
			MetaAttribute a = joinColumnAttribute.getForeignKeyAttribute();
			if (a.getRelationship().getFetchType() != FetchType.EAGER)
				continue;

			Object attributeInstance = entityInstanceBuilder.getAttributeValue(entityInstance, a);
			if (attributeInstance == null || !entityContainer.isSaved(attributeInstance))
				return false;
		}

		return true;
	}

	private void persist(MetaEntity entity, Object entityInstance, List<AttributeValue> attrValues) throws Exception {
		if (entityContainer.isSaved(entityInstance)) {
			Object idValue = AttributeUtil.getIdValue(entity, entityInstance);
			LOG.info("persist: idValue=" + idValue);
			SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateUpdate(entityInstance, entity, attrValues);
			jdbcRunner.persist(sqlStatement, connectionHolder.getConnection());
		} else {
			// checks specific relationship attributes ('one to many' with join table) even
			// if there are no notified changes. If they get changed then they'll be made
			// persistent
			Map<MetaAttribute, Object> joinTableAttrs = new HashMap<>();
			for (MetaAttribute a : entity.getAttributes()) {
				if (a.getRelationship() != null && a.getRelationship().getJoinTable() != null) {
					Object attributeInstance = entityInstanceBuilder.getAttributeValue(entityInstance, a);
					LOG.info("persist: attributeInstance=" + attributeInstance);
					LOG.info("persist: attributeInstance.getClass()=" + attributeInstance.getClass());
					if (AttributeUtil.isCollectionClass(attributeInstance.getClass())
							&& !AttributeUtil.isCollectionEmpty(attributeInstance)) {
						joinTableAttrs.put(a, attributeInstance);
					}
				}
			}

			SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateInsert(connectionHolder.getConnection(),
					entityInstance, entity, attrValues);
			Object pk = jdbcRunner.persist(sqlStatement, connectionHolder.getConnection());
			LOG.info("persist: pk=" + pk);
			entity.getId().getWriteMethod().invoke(entityInstance, pk);

			// persist join table attributes
			LOG.info("persist: joinTableAttrs.size()=" + joinTableAttrs.size());
			for (Map.Entry<MetaAttribute, Object> entry : joinTableAttrs.entrySet()) {
				MetaAttribute a = entry.getKey();
				List<Object> ees = AttributeUtil.getCollectionAsList(entry.getValue());
				if (entityContainer.isSaved(ees)) {
					persistJoinTableAttributes(ees, a, entityInstance);
				} else {
					// add to pending new attributes
					entityContainer.addToPendingNewAttributes(a, entityInstance, ees);
				}
			}
		}
	}

	public void persist(MetaEntity entity, Object entityInstance) throws Exception {
		Optional<List<AttributeValue>> optional = entityInstanceBuilder.getChanges(entity, entityInstance);
		if (!optional.isPresent())
			return;

		boolean stored = persistOnDb(entity, entityInstance, optional.get());
		if (stored) {
			// are there any pending attributes?
		} else {
			entityContainer.addToPendingNew(entityInstance);
			return;
		}

		savePendings();
	}

	private boolean persistOnDb(MetaEntity entity, Object entityInstance, List<AttributeValue> changes) throws Exception {
		boolean persistOnDb = canPersistOnDb(entityInstance);
		LOG.info("persistOnDb: persistOnDb=" + persistOnDb + "; entityInstance=" + entityInstance);
		if (!persistOnDb)
			return false;

		LOG.info("persist: changes.size()=" + changes.size());
		LOG.info("persist: entityInstance=" + entityInstance);
		List<AttributeValue> values = attributeValueConverter.convert(changes);
		persist(entity, entityInstance, values);
		entityContainer.save(entityInstance);
		entityInstanceBuilder.removeChanges(entityInstance);
		return true;
	}

	private void remove(Object entityInstance, MetaEntity e) throws Exception {
		Object idValue = AttributeUtil.getIdValue(e, entityInstance);
		LOG.info("remove: idValue=" + idValue);
		SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateDeleteById(e, idValue);
		jdbcRunner.delete(sqlStatement, connectionHolder.getConnection());
	}

	public void remove(Object entity) throws Exception {
		MetaEntity e = entities.get(entity.getClass().getName());
		if (entityContainer.isSaved(entity)) {
			LOG.info("Instance " + entity + " is in the persistence context");
			remove(entity, e);
			Object idValue = AttributeUtil.getIdValue(e, entity);
			entityContainer.remove(entity, idValue);
		} else {
			LOG.info("Instance " + entity + " not found in the persistence context");
			Object idValue = AttributeUtil.getIdValue(e, entity);
			if (idValue == null)
				return;

			remove(entity, e);
		}
	}

	private void savePendings() throws Exception {
		List<Object> list = entityContainer.getPendingNew();
		LOG.info("savePendings: list.size()=" + list.size());
		for (Object entityInstance : list) {
			MetaEntity e = entities.get(entityInstance.getClass().getName());
			Optional<List<AttributeValue>> optional = entityInstanceBuilder.getChanges(e, entityInstance);
			if (!optional.isPresent()) {
				entityContainer.removePendingNew(entityInstance);
				continue;
			}

			boolean stored = persistOnDb(e, entityInstance, optional.get());
			if (stored)
				entityContainer.removePendingNew(entityInstance);
		}

		List<MetaAttribute> attributes = entityContainer.getPendingNewAttributes();
		LOG.info("savePendings: attributes.size()=" + attributes.size());
		for (MetaAttribute a : attributes) {
			Map<Object, List<Object>> map = entityContainer.getPendingNewAttributeValue(a);
			for (Map.Entry<Object, List<Object>> entry : map.entrySet()) {
				List<Object> ees = entry.getValue();
				Object entityInstance = entry.getKey();
				if (entityContainer.isSaved(ees)) {
					persistJoinTableAttributes(ees, a, entityInstance);
					entityContainer.removePendingNewAttribute(a, entityInstance);
				}
			}
		}
	}

	private void persistJoinTableAttributes(List<Object> ees, MetaAttribute a, Object entityInstance) throws Exception {
		LOG.info("persistJoinTableAttributes: 1");
		// persist every entity instance
		RelationshipJoinTable relationshipJoinTable = a.getRelationship().getJoinTable();
		for (Object instance : ees) {
			SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateJoinTableInsert(relationshipJoinTable,
					entityInstance, instance);
			jdbcRunner.persist(sqlStatement, connectionHolder.getConnection());
		}
	}

}
