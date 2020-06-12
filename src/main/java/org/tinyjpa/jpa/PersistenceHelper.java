package org.tinyjpa.jpa;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import javax.persistence.spi.PersistenceUnitInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.AttributeValueConverter;
import org.tinyjpa.jdbc.Entity;
import org.tinyjpa.jdbc.JdbcRunner;
import org.tinyjpa.jdbc.SqlCode;
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
			SqlStatement sqlStatement = new SqlCode().generateUpdate(entityInstance, entity, attrValues);
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

	public void persist(Connection connection, Map<Entity, Map<Object, List<AttributeValue>>> changes,
			PersistenceUnitInfo persistenceUnitInfo) throws Exception {
		LOG.info("persist: changes.size()=" + changes.size());
		for (Map.Entry<Entity, Map<Object, List<AttributeValue>>> entry : changes.entrySet()) {
			Entity entity = entry.getKey();
			LOG.info("persist: entity.getTableName()=" + entity.getTableName());
			Map<Object, List<AttributeValue>> map = entry.getValue();
			LOG.info("persist: map.size()=" + map.size());
			for (Map.Entry<Object, List<AttributeValue>> e : map.entrySet()) {
				Object entityInstance = e.getKey();
				LOG.info("persist: entityInstance=" + entityInstance);
				List<AttributeValue> attrValues = e.getValue();

				List<AttributeValue> values = attributeValueConverter.convert(attrValues);
//				List<AttributeValue> values = new ArrayList<>();
//				for (AttributeValue attrValue : attrValues) {
//					LOG.info("persist: attrValue.getAttribute().getName()=" + attrValue.getAttribute().getName());
//					LOG.info("persist: attrValue.getAttribute().isEmbedded()=" + attrValue.getAttribute().isEmbedded());
//					if (attrValue.getAttribute().isEmbedded()) {
//						values.addAll(expandEmbedded(attrValue));
//					} else
//						values.add(attrValue);
//				}

				persist(entity, entityInstance, values, connection,
						DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitInfo));
			}
		}
	}

//	private List<AttributeValue> expandEmbedded(AttributeValue attrValue) {
//		List<AttributeValue> attrValues = new ArrayList<>();
//		if (!attrValue.getAttribute().isEmbedded()) {
//			attrValues.add(attrValue);
//			return attrValues;
//		}
//
//		Optional<List<AttributeValue>> optional = EntityDelegate.getInstance()
//				.findEmbeddedAttrValues(attrValue.getValue());
//		LOG.info("expandEmbedded: optional.isPresent()=" + optional.isPresent());
//		if (optional.isPresent()) {
//			List<AttributeValue> list = optional.get();
//			for (AttributeValue av : list) {
//				LOG.info("expandEmbedded: av.getAttribute().getName()=" + av.getAttribute().getName());
//				List<AttributeValue> attrValueList = expandEmbedded(av);
//				attrValues.addAll(attrValueList);
//			}
//		}
//
//		return attrValues;
//	}
}
