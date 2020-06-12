package org.tinyjpa.jdbc.db;

import java.sql.Connection;
import java.util.List;

import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.Entity;
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.jpa.pk.PkStrategy;
import org.tinyjpa.metadata.GeneratedValue;

public interface DbJdbc {
	public SqlStatement generateInsert(Connection connection, Object entityInstance, Entity entity,
			List<AttributeValue> attrValues) throws Exception;

	public Class<? extends PkStrategy> getPkStrategy(GeneratedValue generatedValue);
}
