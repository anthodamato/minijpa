package org.tinyjpa.jdbc.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.AttrValue;
import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.metadata.Entity;

public class ApacheDerbyJdbc extends AbstractDbJdbc {
	private Logger LOG = LoggerFactory.getLogger(ApacheDerbyJdbc.class);

//	@Override
//	public Class<? extends PkStrategy> getPkStrategy(GeneratedValue generatedValue) {
//		if (generatedValue.getStrategy() == GenerationType.SEQUENCE)
//			return PkSequenceStrategy.class;
//
//		return PkSequenceStrategy.class;
//	}

	@Override
	protected Long generateSequenceNextValue(Connection connection, Entity entity) throws SQLException {
		String sql = "VALUES (NEXT VALUE FOR " + entity.getTableName().toUpperCase() + "_PK_SEQ)";
		LOG.info("generateSequenceNextValue: sql=" + sql);
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		preparedStatement.execute();
		ResultSet rs = preparedStatement.getResultSet();
		Long value = null;
		if (rs.next()) {
			value = rs.getLong(1);
		}

		rs.close();
		return value;
	}

	@Override
	protected SqlStatement generateInsertSequenceStrategy(Connection connection, Object entityInstance, Entity entity,
			List<AttrValue> attrValues) throws SQLException {
		Long idValue = generateSequenceNextValue(connection, entity);
		LOG.info("generateInsertSequenceStrategy: idValue=" + idValue);
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ");
		sb.append(entity.getTableName());
		sb.append(" (");
		Attribute id = entity.getId();
		sb.append(id.getColumnName());
		sb.append(",");
		String cols = attrValues.stream().filter(a -> !a.getAttribute().isId())
				.map(a -> a.getAttribute().getColumnName()).collect(Collectors.joining(","));
		sb.append(cols);
		sb.append(") values (");

		int indexStart = 0;
		Object[] values;
		Optional<AttrValue> optional = attrValues.stream().filter(a -> a.getAttribute().isId()).findFirst();
		if (optional.isPresent()) {
			values = new Object[attrValues.size()];
			optional.get().setValue(idValue);
		} else {
			values = new Object[attrValues.size() + 1];
			values[0] = idValue;
			indexStart = 1;
			sb.append("?");
		}

		for (AttrValue attrValue : attrValues) {
			if (indexStart > 0)
				sb.append(",");

			sb.append("?");
			values[indexStart] = attrValue.getValue();
			++indexStart;
		}

		sb.append(")");
		String sql = sb.toString();
		return new SqlStatement(sql, values, attrValues, 0, idValue);
	}

}
