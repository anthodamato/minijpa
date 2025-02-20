package org.minijpa.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Optional;

public interface JdbcValueBuilder<T> {
    Optional<T> build(ResultSet rs, ResultSetMetaData metaData) throws Exception;

}
