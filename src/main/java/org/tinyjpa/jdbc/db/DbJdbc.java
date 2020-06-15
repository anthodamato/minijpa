package org.tinyjpa.jdbc.db;

import java.sql.Connection;
import java.util.List;

import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.Entity;
import org.tinyjpa.jdbc.SqlStatement;

public interface DbJdbc {
	public SqlStatement generateInsert(Connection connection, Object entityInstance, Entity entity,
			List<AttributeValue> attrValues) throws Exception;

	public SqlStatement generateSelectById(Entity entity, Object idValue) throws Exception;

	public SqlStatement generateUpdate(Object entityInstance, Entity entity, List<AttributeValue> attrValues)
			throws Exception;

}
