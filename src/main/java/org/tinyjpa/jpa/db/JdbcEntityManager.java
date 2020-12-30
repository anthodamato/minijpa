package org.tinyjpa.jpa.db;

import java.util.List;

import javax.persistence.Query;

import org.tinyjpa.jdbc.AbstractJdbcRunner;
import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jdbc.db.TinyFlushMode;

public interface JdbcEntityManager {
	public void persist(MetaEntity entity, Object entityInstance, TinyFlushMode tinyFlushMode) throws Exception;

	public void flush() throws Exception;

	public Object createAndSaveEntityInstance(AbstractJdbcRunner.AttributeValues attributeValues, MetaEntity entity,
			MetaAttribute childAttribute, Object childAttributeValue) throws Exception;

	public List<Object> select(Query query) throws Exception;

}
