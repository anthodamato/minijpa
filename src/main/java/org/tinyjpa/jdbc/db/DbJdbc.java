package org.tinyjpa.jdbc.db;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.tinyjpa.jdbc.AttrValue;
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.jpa.pk.PkStrategy;
import org.tinyjpa.metadata.Entity;
import org.tinyjpa.metadata.GeneratedValue;

public interface DbJdbc {
	public SqlStatement generateInsert(Connection connection, Object entityInstance, Entity entity,
			List<AttrValue> attrValues)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException;

	public Class<? extends PkStrategy> getPkStrategy(GeneratedValue generatedValue);
}
