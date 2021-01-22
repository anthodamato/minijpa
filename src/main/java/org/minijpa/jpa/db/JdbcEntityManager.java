package org.minijpa.jpa.db;

import java.util.List;

import javax.persistence.Query;

import org.minijpa.jdbc.AbstractJdbcRunner;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.TinyFlushMode;
import org.minijpa.jpa.DeleteQuery;
import org.minijpa.jpa.UpdateQuery;

public interface JdbcEntityManager {
	public void persist(MetaEntity entity, Object entityInstance, TinyFlushMode tinyFlushMode) throws Exception;

	public void flush() throws Exception;

	public Object createAndSaveEntityInstance(AbstractJdbcRunner.AttributeValues attributeValues, MetaEntity entity,
			MetaAttribute childAttribute, Object childAttributeValue) throws Exception;

	public List<?> select(Query query) throws Exception;

	public int update(UpdateQuery updateQuery) throws Exception;

	public int delete(DeleteQuery deleteQuery) throws Exception;

}
