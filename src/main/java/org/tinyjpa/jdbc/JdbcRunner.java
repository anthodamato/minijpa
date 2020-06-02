package org.tinyjpa.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.spi.PersistenceUnitInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.metadata.Entity;
import org.tinyjpa.metadata.EntityHelper;

public class JdbcRunner {
	private Logger LOG = LoggerFactory.getLogger(JdbcRunner.class);
	private EntityHelper entityHelper = new EntityHelper();

	private void setPreparedStatementValue(PreparedStatement preparedStatement, int index, Attribute attribute,
			Object value) throws SQLException {
		if (value == null) {
			preparedStatement.setNull(index, attribute.getSqlType());
			return;
		}

		if (attribute.getType() == BigDecimal.class)
			preparedStatement.setBigDecimal(index, (BigDecimal) value);
		else if (attribute.getType() == Boolean.class)
			preparedStatement.setBoolean(index, (Boolean) value);
		else if (attribute.getType() == Double.class)
			preparedStatement.setDouble(index, (Double) value);
		else if (attribute.getType() == Float.class)
			preparedStatement.setFloat(index, (Float) value);
		else if (attribute.getType() == Integer.class)
			preparedStatement.setInt(index, (Integer) value);
		else if (attribute.getType() == Long.class)
			preparedStatement.setLong(index, (Long) value);
		else if (attribute.getType() == String.class)
			preparedStatement.setString(index, (String) value);
	}

	private void setPreparedStatementValues(PreparedStatement preparedStatement, SqlStatement sqlStatement)
			throws SQLException {
		int index = 1;
		for (int i = sqlStatement.getStartIndex(); i < sqlStatement.getAttrValues().size(); ++i) {
//		for (AttrValue attrValue : attrValues) {
			AttrValue attrValue = sqlStatement.getAttrValues().get(i);
			setPreparedStatementValue(preparedStatement, index, attrValue.getAttribute(), attrValue.getValue());
			++index;
		}
	}

	public Object persist(SqlStatement sqlStatement, Connection connection) throws SQLException {
		LOG.info("persist: sqlStatement.sql=" + sqlStatement.getSql());
		LOG.info("persist: attrValues.size()=" + sqlStatement.getAttrValues().size());
		PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement.getSql(),
				Statement.RETURN_GENERATED_KEYS);
		setPreparedStatementValues(preparedStatement, sqlStatement);
		preparedStatement.execute();
		if (sqlStatement.getIdValue() != null) {
			preparedStatement.close();
			return sqlStatement.getIdValue();
		}

		Object pk = null;
		ResultSet resultSet = preparedStatement.getGeneratedKeys();
		LOG.info("persist: getGeneratedKeys() resultSet=" + resultSet);
		if (resultSet != null && resultSet.next()) {
			pk = resultSet.getLong(1);
			LOG.info("persist: getGeneratedKeys() pk=" + pk);
		}

		preparedStatement.close();
		return pk;
	}

	public AttributeValues findById(Entity entity, Object idValue, PersistenceUnitInfo persistenceUnitInfo)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException,
			InstantiationException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			SqlStatement sqlStatement = new SqlCode().generateSelectById(entity, idValue);
			connection = new ConnectionProvider().getConnection(persistenceUnitInfo);
			preparedStatement = connection.prepareStatement(sqlStatement.getSql());
			setPreparedStatementValue(preparedStatement, 1, entity.getId(), idValue);

			ResultSet rs = preparedStatement.executeQuery();
			if (!rs.next())
				return null;

			AttributeValues attributeValues = new AttributeValues();
			Object entityInstance = entity.getClazz().newInstance();
			attributeValues.entityInstance = entityInstance;

			int i = 1;
			for (Attribute attribute : sqlStatement.getAttributes()) {
//				attribute.getWriteMethod().invoke(entityInstance, rs.getObject(i));
				attributeValues.attributes.add(attribute);
				attributeValues.values.add(rs.getObject(i));
				++i;
			}

//			entity.getId().getWriteMethod().invoke(entityInstance, idValue);
			return attributeValues;
		} finally {
			preparedStatement.close();
			connection.rollback();
			connection.close();
		}
	}

	public void callWriteMethods(Entity entity, AttributeValues attributeValues, Object idValue)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		int i = 0;
		for (Attribute attribute : attributeValues.attributes) {
			attribute.getWriteMethod().invoke(attributeValues.entityInstance, attributeValues.values.get(i));
			++i;
		}

		entity.getId().getWriteMethod().invoke(attributeValues.entityInstance, idValue);
	}

	public class AttributeValues {
		public Object entityInstance;
		public List<Object> values = new ArrayList<>();
		public List<Attribute> attributes = new ArrayList<>();
	}
}
