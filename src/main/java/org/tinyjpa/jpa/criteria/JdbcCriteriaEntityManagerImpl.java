package org.tinyjpa.jpa.criteria;

import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.AttributeValueConverter;
import org.tinyjpa.jdbc.ConnectionHolder;
import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.jdbc.db.DbConfiguration;
import org.tinyjpa.jdbc.db.EntityContainer;
import org.tinyjpa.jdbc.db.EntityInstanceBuilder;
import org.tinyjpa.jdbc.db.JdbcEntityManagerImpl;

public class JdbcCriteriaEntityManagerImpl extends JdbcEntityManagerImpl implements JdbcCriteriaEntityManager {
	private Logger LOG = LoggerFactory.getLogger(JdbcCriteriaEntityManagerImpl.class);

	public JdbcCriteriaEntityManagerImpl(DbConfiguration dbConfiguration, Map<String, MetaEntity> entities,
			EntityContainer entityContainer, EntityInstanceBuilder entityInstanceBuilder,
			AttributeValueConverter attributeValueConverter, ConnectionHolder connectionHolder) {
		super(dbConfiguration, entities, entityContainer, entityInstanceBuilder, attributeValueConverter,
				connectionHolder);
	}

	@Override
	public List<Object> select(CriteriaQuery<?> criteriaQuery) throws Exception {
		Class<?> entityClass = criteriaQuery.getResultType();
		MetaEntity entity = entities.get(entityClass.getName());
		if (entity == null)
			throw new IllegalArgumentException("Class '" + entityClass.getName() + "' is not an entity");

		DbJdbcCriteria dbJdbcCriteria = (DbJdbcCriteria) dbConfiguration.getDbJdbc();
		LOG.info("select: dbJdbcCriteria=" + dbJdbcCriteria);
		SqlStatement sqlStatement = dbJdbcCriteria.select(criteriaQuery, entities);
		LOG.info("select: sqlStatement.getSql()=" + sqlStatement.getSql());

		return jdbcRunner.findCollection(connectionHolder.getConnection(), sqlStatement, entity, this, null, null);
	}

}
