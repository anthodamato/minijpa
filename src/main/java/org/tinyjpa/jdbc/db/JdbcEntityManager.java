package org.tinyjpa.jdbc.db;

import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jdbc.JdbcRunner;

public interface JdbcEntityManager {
	public Object createAndSaveEntityInstance(JdbcRunner.AttributeValues attributeValues, MetaEntity entity,
			MetaAttribute childAttribute, Object childAttributeValue) throws Exception;

}
