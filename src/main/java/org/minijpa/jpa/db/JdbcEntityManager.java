package org.minijpa.jpa.db;

import java.util.List;

import javax.persistence.Query;

import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.MiniFlushMode;
import org.minijpa.jpa.DeleteQuery;
import org.minijpa.jpa.MiniNativeQuery;
import org.minijpa.jpa.UpdateQuery;

public interface JdbcEntityManager {

    public void persist(MetaEntity entity, Object entityInstance, MiniFlushMode tinyFlushMode) throws Exception;

    public void flush() throws Exception;

    public List<?> select(Query query) throws Exception;

    public List<?> selectNative(MiniNativeQuery query) throws Exception;

    public int update(String sqlString, Query query) throws Exception;

    public int update(UpdateQuery updateQuery) throws Exception;

    public int delete(DeleteQuery deleteQuery) throws Exception;

    public void remove(Object entity) throws Exception;

}
