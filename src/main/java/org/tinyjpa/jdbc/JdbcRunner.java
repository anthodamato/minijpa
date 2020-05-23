package org.tinyjpa.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.spi.PersistenceUnitInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.metadata.Entity;
import org.tinyjpa.metadata.EntityHelper;

public class JdbcRunner {
	private Logger LOG = LoggerFactory.getLogger(JdbcRunner.class);
	private EntityHelper entityHelper = new EntityHelper();

	private void setPreparedStatementValues(PreparedStatement preparedStatement, List<AttrValue> attrValues)
			throws SQLException {
		int index = 1;
		for (AttrValue attrValue : attrValues) {
			if (attrValue.getValue() == null) {
				preparedStatement.setNull(index, attrValue.getAttribute().getSqlType());
				continue;
			}

			if (attrValue.getAttribute().getType() == BigDecimal.class)
				preparedStatement.setBigDecimal(index, (BigDecimal) attrValue.getValue());
			else if (attrValue.getAttribute().getType() == Boolean.class)
				preparedStatement.setBoolean(index, (Boolean) attrValue.getValue());
			else if (attrValue.getAttribute().getType() == Double.class)
				preparedStatement.setDouble(index, (Double) attrValue.getValue());
			else if (attrValue.getAttribute().getType() == Float.class)
				preparedStatement.setFloat(index, (Float) attrValue.getValue());
			else if (attrValue.getAttribute().getType() == Integer.class)
				preparedStatement.setInt(index, (Integer) attrValue.getValue());
			else if (attrValue.getAttribute().getType() == Long.class)
				preparedStatement.setLong(index, (Long) attrValue.getValue());
			else if (attrValue.getAttribute().getType() == String.class)
				preparedStatement.setString(index, (String) attrValue.getValue());

			++index;
		}
	}

	public void persist(List<AttrValue> attrValues, SqlCode.SqlStatement sqlStatement, Connection connection)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException {
		LOG.info("persist: sqlStatement.sql=" + sqlStatement.sql);
		PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement.sql);
		setPreparedStatementValues(preparedStatement, attrValues);
		preparedStatement.execute();
		preparedStatement.close();
	}

	public Object findById(Entity entity, Object idValue, PersistenceUnitInfo persistenceUnitInfo)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException,
			InstantiationException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			SqlCode.SqlStatement sqlStatement = new SqlCode().generateSelectById(entity, idValue);
			connection = new ConnectionProvider().getConnection(persistenceUnitInfo);
			preparedStatement = connection.prepareStatement(sqlStatement.sql);
			if (idValue instanceof BigDecimal)
				preparedStatement.setBigDecimal(1, (BigDecimal) idValue);
			if (idValue instanceof Boolean)
				preparedStatement.setBoolean(1, (Boolean) idValue);
			else if (idValue instanceof Integer)
				preparedStatement.setInt(1, (Integer) idValue);
			else if (idValue instanceof Long)
				preparedStatement.setLong(1, (Long) idValue);
			else if (idValue instanceof String)
				preparedStatement.setString(1, (String) idValue);

			ResultSet rs = preparedStatement.executeQuery();
			Object entityInstance = null;
			if (rs.next()) {
				entityInstance = entity.getClazz().newInstance();
				int i = 1;
				for (Attribute attribute : sqlStatement.attributes) {
					attribute.getWriteMethod().invoke(entityInstance, rs.getObject(i));
					++i;
				}

				entity.getId().getWriteMethod().invoke(entityInstance, idValue);
			}

			return entityInstance;
		} finally {
			preparedStatement.close();
			connection.close();
		}
	}
}
