package org.tinyjpa.jpa;

import java.sql.Connection;
import java.util.List;

import javax.persistence.spi.PersistenceUnitInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.AttributeValueConverter;
import org.tinyjpa.jdbc.ColumnNameValue;
import org.tinyjpa.jdbc.Entity;
import org.tinyjpa.jdbc.JdbcRunner;
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.jdbc.db.DbConfiguration;
import org.tinyjpa.jdbc.db.EntityContainer;
import org.tinyjpa.jpa.db.DbConfigurationList;
import org.tinyjpa.metadata.EmbeddedAttributeValueConverter;
import org.tinyjpa.metadata.EntityHelper;

public class PersistenceHelper {
	private Logger LOG = LoggerFactory.getLogger(PersistenceHelper.class);
	private EntityContainer persistenceContext;
	private EntityHelper entityHelper = new EntityHelper();
	private AttributeValueConverter attributeValueConverter = new EmbeddedAttributeValueConverter();

	public PersistenceHelper(EntityContainer persistenceContext) {
		super();
		this.persistenceContext = persistenceContext;
	}

	private void persist(Entity entity, Object entityInstance, List<AttributeValue> attrValues, Connection connection,
			DbConfiguration dbConfiguration) throws Exception {
		if (persistenceContext.isSaved(entityInstance)) {
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
//			saveForeignKeys(sqlStatement, entityInstance);
		}
	}

	private void saveForeignKeys(SqlStatement sqlStatement, Object entityInstance) {
		for (ColumnNameValue columnNameValue : sqlStatement.getColumnNameValues()) {
			LOG.info("saveForeignKeys: columnNameValue.getForeignKeyAttribute()="
					+ columnNameValue.getForeignKeyAttribute() + "; columnNameValue.getValue()="
					+ columnNameValue.getValue());
			if (columnNameValue.getForeignKeyAttribute() != null) {
				persistenceContext.saveForeignKey(entityInstance, columnNameValue.getForeignKeyAttribute(),
						columnNameValue.getValue());
			}
		}
	}

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
