package org.tinyjpa.jdbc.db;

import org.tinyjpa.jdbc.Entity;
import org.tinyjpa.jdbc.JdbcRunner;

public interface JdbcEntityManager {
	public Object createAndSaveEntityInstance(JdbcRunner.AttributeValues attributeValues, Entity entity)
			throws Exception;

}
