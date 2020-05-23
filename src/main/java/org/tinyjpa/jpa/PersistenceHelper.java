package org.tinyjpa.jpa;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.persistence.spi.PersistenceUnitInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.AttrValue;
import org.tinyjpa.jdbc.ConnectionProvider;
import org.tinyjpa.jdbc.JdbcRunner;
import org.tinyjpa.jdbc.SqlCode;
import org.tinyjpa.metadata.Entity;
import org.tinyjpa.metadata.EntityHelper;

public class PersistenceHelper {
	private Logger LOG = LoggerFactory.getLogger(PersistenceHelper.class);
	private PersistenceContext persistenceContext;
	private EntityHelper entityHelper = new EntityHelper();

	private void persist(Entity entity, Object entityInstance, List<AttrValue> attrValues, Connection connection)
			throws IllegalAccessException, InvocationTargetException, IllegalArgumentException, SQLException {
		if (persistenceContext.isPersistentOnDb(entityInstance)) {
			Object idValue = entityHelper.getIdValue(entity, entityInstance);
			LOG.info("persist: idValue=" + idValue);
			SqlCode.SqlStatement sqlStatement = new SqlCode().generateUpdate(entityInstance, entity, attrValues);
			new JdbcRunner().persist(attrValues, sqlStatement, connection);
		} else {
			SqlCode.SqlStatement sqlStatement = new SqlCode().generateInsert(entityInstance, entity, attrValues);
			new JdbcRunner().persist(attrValues, sqlStatement, connection);
		}
	}

	public boolean persist(Map<Entity, Map<Object, List<AttrValue>>> changes, PersistenceUnitInfo persistenceUnitInfo) {
		LOG.info("persist: changes.size()=" + changes.size());
		Connection connection = null;
		try {
			connection = new ConnectionProvider().getConnection(persistenceUnitInfo);
		} catch (SQLException e1) {
			LOG.error(e1.getMessage());
			return false;
		}

		try {
			for (Map.Entry<Entity, Map<Object, List<AttrValue>>> entry : changes.entrySet()) {
				Entity entity = entry.getKey();
				LOG.info("persist: entity.getTableName()=" + entity.getTableName());
				Map<Object, List<AttrValue>> map = entry.getValue();
				LOG.info("persist: map.size()=" + map.size());
				for (Map.Entry<Object, List<AttrValue>> e : map.entrySet()) {
					Object entityInstance = e.getKey();
					LOG.info("persist: entityInstance=" + entityInstance);
					List<AttrValue> attrValues = e.getValue();
					persist(entity, entityInstance, attrValues, connection);
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
			return false;
		} finally {
			try {
				connection.rollback();
				connection.close();
			} catch (SQLException e) {
				LOG.error(e.getMessage());
				return false;
			}
		}

		return true;
	}
}
