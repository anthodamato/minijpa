package org.tinyjpa.jdbc.db;

import java.util.List;

import org.tinyjpa.jdbc.JdbcRunner;
import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.MetaEntity;

public interface JdbcEntityManager {
	public void persist(MetaEntity entity, Object entityInstance, TinyFlushMode tinyFlushMode) throws Exception;

	public void flush() throws Exception;

	public Object createAndSaveEntityInstance(JdbcRunner.AttributeValues attributeValues, MetaEntity entity,
			MetaAttribute childAttribute, Object childAttributeValue) throws Exception;

	public List<Object> loadAllFields(Class<?> entityClass) throws Exception;

}
