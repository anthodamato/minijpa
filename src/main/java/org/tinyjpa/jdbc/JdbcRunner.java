package org.tinyjpa.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.spi.PersistenceUnitInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.metadata.EntityHelper;

public class JdbcRunner {
	private Logger LOG = LoggerFactory.getLogger(JdbcRunner.class);
	private EntityHelper entityHelper = new EntityHelper();

	private void setPreparedStatementValue(PreparedStatement preparedStatement, int index, Attribute attribute,
			Object value) throws SQLException {
		LOG.info("setPreparedStatementValue: value=" + value + "; attribute: " + attribute);
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
		else if (attribute.getType() == Date.class)
			preparedStatement.setDate(index, (Date) value);
	}

	private void setPreparedStatementValues(PreparedStatement preparedStatement, SqlStatement sqlStatement)
			throws SQLException {
		LOG.info("setPreparedStatementValues: sqlStatement.getStartIndex()=" + sqlStatement.getStartIndex());
		int index = 1;
		for (int i = sqlStatement.getStartIndex(); i < sqlStatement.getAttrValues().size(); ++i) {
			AttributeValue attrValue = sqlStatement.getAttrValues().get(i);
			LOG.info("setPreparedStatementValues: columnName=" + attrValue.getAttribute().getColumnName() + "; type="
					+ attrValue.getAttribute().getType().getName() + "; value=" + attrValue.getValue());
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

	public void delete(SqlStatement sqlStatement, Connection connection) throws SQLException {
		LOG.info("delete: sqlStatement.sql=" + sqlStatement.getSql());
		LOG.info("delete: attrValues.size()=" + sqlStatement.getAttrValues().size());
		PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement.getSql());
		setPreparedStatementValues(preparedStatement, sqlStatement);
		preparedStatement.execute();
		preparedStatement.close();
	}

	public AttributeValues findById(Connection connection, SqlStatement sqlStatement, Entity entity,
			PersistenceUnitInfo persistenceUnitInfo) throws Exception {
		PreparedStatement preparedStatement = null;
		try {
			LOG.info("findById: sql=" + sqlStatement.getSql());
			preparedStatement = connection.prepareStatement(sqlStatement.getSql());
			setPreparedStatementValues(preparedStatement, sqlStatement);

			ResultSet rs = preparedStatement.executeQuery();
			boolean next = rs.next();
			if (!next)
				return null;

			AttributeValues attributeValues = new AttributeValues();

			int i = 1;
			for (Attribute attribute : sqlStatement.getAttributes()) {
				attributeValues.attributes.add(attribute);
				attributeValues.values.add(rs.getObject(i, attribute.getType()));
				++i;
			}

			return attributeValues;
		} finally {
			preparedStatement.close();
			connection.rollback();
		}
	}

	public class AttributeValues {
		public List<Object> values = new ArrayList<>();
		public List<Attribute> attributes = new ArrayList<>();
	}
}
