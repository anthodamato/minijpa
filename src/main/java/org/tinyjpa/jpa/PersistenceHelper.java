package org.tinyjpa.jpa;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.spi.PersistenceUnitInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.AttrValue;
import org.tinyjpa.jdbc.JdbcRunner;
import org.tinyjpa.jdbc.SqlCode;
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.jpa.db.DbConfiguration;
import org.tinyjpa.jpa.db.DbConfigurationList;
import org.tinyjpa.metadata.Entity;
import org.tinyjpa.metadata.EntityDelegate;
import org.tinyjpa.metadata.EntityHelper;

public class PersistenceHelper {
	private Logger LOG = LoggerFactory.getLogger(PersistenceHelper.class);
	private PersistenceContext persistenceContext;
	private EntityHelper entityHelper = new EntityHelper();

	public PersistenceHelper(PersistenceContext persistenceContext) {
		super();
		this.persistenceContext = persistenceContext;
	}

	private void persist(Entity entity, Object entityInstance, List<AttrValue> attrValues, Connection connection,
			DbConfiguration dbConfiguration)
			throws IllegalAccessException, InvocationTargetException, IllegalArgumentException, SQLException {
		if (persistenceContext.isPersistentOnDb(entityInstance)) {
			Object idValue = entityHelper.getIdValue(entity, entityInstance);
			LOG.info("persist: idValue=" + idValue);
			SqlStatement sqlStatement = new SqlCode().generateUpdate(entityInstance, entity, attrValues);
			new JdbcRunner().persist(sqlStatement, connection);
		} else {
			LOG.info("persist: dbConfiguration=" + dbConfiguration);

			SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateInsert(connection, entityInstance, entity,
					attrValues);
			Object pk = new JdbcRunner().persist(sqlStatement, connection);
			LOG.info("persist: pk=" + pk);

//			try {
//				EntityDelegate.getInstance().addIgnoreEntityInstance(entityInstance);
			entity.getId().getWriteMethod().invoke(entityInstance, pk);
//			} finally {
//				EntityDelegate.getInstance().removeIgnoreEntityInstance(entityInstance);
//			}
		}
	}

	public void persist(Connection connection, Map<Entity, Map<Object, List<AttrValue>>> changes,
			PersistenceUnitInfo persistenceUnitInfo)
			throws SQLException, IllegalAccessException, InvocationTargetException, IllegalArgumentException {
		LOG.info("persist: changes.size()=" + changes.size());
		for (Map.Entry<Entity, Map<Object, List<AttrValue>>> entry : changes.entrySet()) {
			Entity entity = entry.getKey();
			LOG.info("persist: entity.getTableName()=" + entity.getTableName());
			Map<Object, List<AttrValue>> map = entry.getValue();
			LOG.info("persist: map.size()=" + map.size());
			for (Map.Entry<Object, List<AttrValue>> e : map.entrySet()) {
				Object entityInstance = e.getKey();
				LOG.info("persist: entityInstance=" + entityInstance);
				List<AttrValue> attrValues = e.getValue();

				List<AttrValue> values = new ArrayList<>();
				for (AttrValue attrValue : attrValues) {
					LOG.info("persist: attrValue.getAttribute().getName()=" + attrValue.getAttribute().getName());
					LOG.info("persist: attrValue.getAttribute().isEmbedded()=" + attrValue.getAttribute().isEmbedded());
					if (attrValue.getAttribute().isEmbedded()) {
						values.addAll(expandEmbedded(attrValue));
					} else
						values.add(attrValue);
				}

				persist(entity, entityInstance, values, connection,
						DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitInfo));
			}
		}
	}

	private List<AttrValue> expandEmbedded(AttrValue attrValue) {
		List<AttrValue> attrValues = new ArrayList<>();
		if (!attrValue.getAttribute().isEmbedded()) {
			attrValues.add(attrValue);
			return attrValues;
		}

		Optional<List<AttrValue>> optional = EntityDelegate.getInstance().findEmbeddedAttrValues(attrValue.getValue());
		LOG.info("expandEmbedded: optional.isPresent()=" + optional.isPresent());
		if (optional.isPresent()) {
			List<AttrValue> list = optional.get();
			for (AttrValue av : list) {
				LOG.info("expandEmbedded: av.getAttribute().getName()=" + av.getAttribute().getName());
				List<AttrValue> attrValueList = expandEmbedded(av);
				attrValues.addAll(attrValueList);
			}
		}

		return attrValues;
	}
}
