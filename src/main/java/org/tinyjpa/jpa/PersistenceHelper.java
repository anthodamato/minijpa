package org.tinyjpa.jpa;

import java.sql.Connection;
import java.util.List;

import javax.persistence.spi.PersistenceUnitInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.AttributeValueConverter;
import org.tinyjpa.jdbc.Entity;
import org.tinyjpa.jdbc.JdbcRunner;
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.jpa.db.DbConfiguration;
import org.tinyjpa.jpa.db.DbConfigurationList;
import org.tinyjpa.metadata.EmbeddedAttributeValueConverter;
import org.tinyjpa.metadata.EntityHelper;

public class PersistenceHelper {
	private Logger LOG = LoggerFactory.getLogger(PersistenceHelper.class);
	private PersistenceContext persistenceContext;
	private EntityHelper entityHelper = new EntityHelper();
	private AttributeValueConverter attributeValueConverter = new EmbeddedAttributeValueConverter();

	public PersistenceHelper(PersistenceContext persistenceContext) {
		super();
		this.persistenceContext = persistenceContext;
	}

	private void persist(Entity entity, Object entityInstance, List<AttributeValue> attrValues, Connection connection,
			DbConfiguration dbConfiguration) throws Exception {
		if (persistenceContext.isPersistentOnDb(entityInstance)) {
			Object idValue = entityHelper.getIdValue(entity, entityInstance);
			LOG.info("persist: idValue=" + idValue);
			SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateUpdate(entityInstance, entity, attrValues);
			new JdbcRunner().persist(sqlStatement, connection);
		} else {
			LOG.info("persist: dbConfiguration=" + dbConfiguration);

			SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateInsert(connection, entityInstance, entity,
					attrValues);
			Object pk = new JdbcRunner().persist(sqlStatement, connection);
			LOG.info("persist: pk=" + pk);

			entity.getId().getWriteMethod().invoke(entityInstance, pk);
		}
	}

//	public void persist(Connection connection, Map<Entity, Map<Object, List<AttributeValue>>> changes,
//			PersistenceUnitInfo persistenceUnitInfo) throws Exception {
//		LOG.info("persist: changes.size()=" + changes.size());
//		for (Map.Entry<Entity, Map<Object, List<AttributeValue>>> entry : changes.entrySet()) {
//			Entity entity = entry.getKey();
//			LOG.info("persist: entity.getTableName()=" + entity.getTableName());
//			Map<Object, List<AttributeValue>> map = entry.getValue();
//			LOG.info("persist: map.size()=" + map.size());
//			for (Map.Entry<Object, List<AttributeValue>> e : map.entrySet()) {
//				Object entityInstance = e.getKey();
//				LOG.info("persist: entityInstance=" + entityInstance);
//				List<AttributeValue> attrValues = e.getValue();
//
//				List<AttributeValue> values = attributeValueConverter.convert(attrValues);
//				persist(entity, entityInstance, values, connection,
//						DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitInfo));
//			}
//		}
//	}

	public void persist(Connection connection, Entity entity, Object entityInstance, List<AttributeValue> changes,
			PersistenceUnitInfo persistenceUnitInfo) throws Exception {
		LOG.info("persist: changes.size()=" + changes.size());
		LOG.info("persist: entityInstance=" + entityInstance);
		List<AttributeValue> values = attributeValueConverter.convert(changes);
		persist(entity, entityInstance, values, connection,
				DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitInfo));
	}

	public void remove(Connection connection, Object entityInstance, Entity e, PersistenceUnitInfo persistenceUnitInfo)
			throws Exception {
		Object idValue = new EntityHelper().getIdValue(e, entityInstance);
		LOG.info("remove: idValue=" + idValue);
		DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitInfo);
		SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateDeleteById(e, idValue);
		new JdbcRunner().delete(sqlStatement, connection);
	}
}
