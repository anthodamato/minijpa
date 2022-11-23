package org.minijpa.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Optional;

public interface JdbcValueBuilder {
    public Optional<?> build(ResultSet rs, ResultSetMetaData metaData) throws Exception;

}
