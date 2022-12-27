package org.minijpa.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Optional;

public interface JdbcValueBuilder<T> {
    public Optional<T> build(ResultSet rs, ResultSetMetaData metaData) throws Exception;

}
